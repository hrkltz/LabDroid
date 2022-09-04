package io.github.herrherklotz.chameleon.x.backend.runtime.component.com.client

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import io.github.herrherklotz.chameleon.Chameleon.LOG_D
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.ev3.Code
import io.github.herrherklotz.chameleon.x.ev3.Ev3Command
import io.github.herrherklotz.chameleon.x.ev3.Ev3Motor
import io.github.herrherklotz.chameleon.x.ev3.Button
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.x.backend.system.project.component.com.client.Ev3Data
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.coroutines.CoroutineContext


// Test MAC: 00:06:66:43:06:05
object Ev3 : IComponent, Ev3Data() {
    private var baseConfig: Ev3Data = Ev3Data()
    private var jobConnection: Job = Job()
    private var jobReceive: Job = Job()
    private var jobMotor: Job = Job()
    private var mBTSocket: BluetoothSocket? = null
    private val BTMODULEUUID: UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB") // "random" unique identifier
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(mapOf("Receive" to BroadcastChannel(1))) // TODO "Status" to BroadcastChannel(1),
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap(mapOf("Motor" to
                Channel(1)))
    /* override */ val mRequests: List<String> = listOf("Button")
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false
    val mReplies = ConcurrentHashMap<Int, Array<Byte>>(mutableMapOf())


    private fun workerConnection(bluetoothAdapter: BluetoothAdapter): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            try {
                while (coroutineContext.isActive) {
                    val lBluetoothDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(mac)

                    try {
                        mBTSocket = lBluetoothDevice.createRfcommSocketToServiceRecord(BTMODULEUUID)
                    } catch (ignore: IOException) {
                        LogElement.error("Ev3", "Couldn't establish the Socket.")
                        continue
                    }

                    bluetoothAdapter.cancelDiscovery()

                    try {
                        mBTSocket!!.connect()
                    } catch (ignore: IOException) {
                        LogElement.error("Ev3", ignore.localizedMessage)

                        try {
                            mBTSocket!!.close()
                        } catch (ignore2: IOException) {}

                        delay(100)

                        continue
                    }

                    try {
                        mOutputStream = mBTSocket!!.outputStream
                        mInputStream = mBTSocket!!.inputStream
                    } catch (ignore: IOException) {
                        LogElement.error("Ev3", "Couldn't extract Sockets.")
                        continue
                    }

                    jobReceive = workerReceive()
                    jobMotor = workerMotor()

                    // DoIt
                    while (mBTSocket!!.isConnected)
                        delay(200)

                    // TODO lock until BluetoothBroadcastReceiver gots ACL_DISCONNECT or OFF
                    LogElement.error("Ev3", "lost connection")

                    close()
                }
            } finally {
                close()
            }
        }
    }


    private fun close() {
        runBlocking {
            jobReceive.cancelAndJoin()
            jobReceive = Job()
            jobMotor.cancelAndJoin()
            jobMotor = Job()
        }

        if (mInputStream != null)
            mInputStream!!.close()
        mInputStream = null

        if (mOutputStream != null)
            mOutputStream!!.close()
        mOutputStream = null

        if (mBTSocket != null)
            mBTSocket!!.close()
        mBTSocket = null
    }


    @ExperimentalCoroutinesApi
    private suspend fun workerReceive(): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            var lByteBuffer: ByteArray // lByteBuffer store for the stream

            var lStringBuilder = StringBuilder()
            var lByteCounter: Int

            while (coroutineContext.isActive) {
                lByteCounter = try {
                    mInputStream!!.available()
                } catch (ignore: IOException) {
                    break
                }

                if (lByteCounter == 0) {
                    delay(100)
                    continue
                }

                lByteBuffer = ByteArray(lByteCounter)

                try {
                    mInputStream!!.read(lByteBuffer, 0, lByteBuffer.size) // record how many
                    // bytes we actually read
                } catch (ignore: IOException) {
                    continue
                }

                val sequence = (lByteBuffer[3].toInt().shl(8).or(lByteBuffer[2].toInt()))

                mReplies[sequence] = lByteBuffer.toTypedArray()
            }
        }
    }


    private fun sendCommandWithReply(command: Ev3Command): Map<String, Any>? {
        when (command.commandType) {
            Code._DIRECT_COMMAND_REPLY,
            Code._SYSTEM_COMMAND_REPLY -> {
                mReplies[command.mySequence] = emptyArray()
            }
        }

        sendCommand(command)

        return when (command.commandType) {
            Code._DIRECT_COMMAND_REPLY,
            Code._SYSTEM_COMMAND_REPLY -> {
                while (mReplies[command.mySequence]!!.isEmpty())
                    Thread.sleep(100)

                val reply = mReplies.remove(command.mySequence) ?: return null

                return mapOf<String, Any>(
                    "success" to (reply[4].toInt() == 0x02),
                    "value" to reply[5].toInt()
                )
            }
            else -> null
        }
    }


    private fun sendCommand(command: Ev3Command) {
        mOutputStream!!.write(command.byteCode())
    }


    fun Button(buttonId: Any): Any? {
        if (buttonId !is Int)
            return null

        val command = Button.buttonPressed(buttonId) ?: return null

        return sendCommandWithReply(command)
    }


    @ExperimentalCoroutinesApi
    private suspend fun workerMotor(): Job {
        return coroutineScope.launch {
            val lChannel = mChannels["Motor"]

            while (coroutineContext.isActive) {
                when (val lData = lChannel!!.receive()) {
                    is Map<*,*> -> {
                        if (!(lData.containsKey("function") && lData.containsKey("port")))
                            continue

                        val portId = (lData["port"] as Int).toByte()
                        val command: Ev3Command

                        when (lData["function"] as String) {
                            "setPolarity" -> {
                                if (!lData.containsKey("polarity"))
                                    continue

                                val polarity = lData["polarity"] as Int

                                command = Ev3Motor.setPolarity(portId, polarity)
                            }
                            "stepAtPower" -> {
                                if (!(lData.containsKey("power") && lData.containsKey("rampUp") &&
                                lData.containsKey("continueRun") && lData.containsKey("rampDown") &&
                                lData.containsKey("brake")))
                                    continue

                                val power = lData["power"] as Int
                                val rampUp = lData["rampUp"] as Int
                                val continueRun = lData["continueRun"] as Int
                                val rampDown = lData["rampDown"] as Int
                                val brake = lData["brake"] as Boolean

                                command = Ev3Motor.stepAtPower(portId, power, rampUp, continueRun,
                                rampDown, brake)
                            }
                            "stepAtSpeed" -> {
                                if (!(lData.containsKey("speed") && lData.containsKey("rampUp") &&
                                lData.containsKey("continueRun") && lData.containsKey("rampDown") &&
                                lData.containsKey("brake")))
                                    continue

                                val speed = lData["speed"] as Int
                                val rampUp = lData["rampUp"] as Int
                                val continueRun = lData["continueRun"] as Int
                                val rampDown = lData["rampDown"] as Int
                                val brake = lData["brake"] as Boolean

                                command = Ev3Motor.stepAtSpeed(portId, speed, rampUp, continueRun,
                                        rampDown, brake)
                            }
                            "stepSync" -> {
                                if (!(lData.containsKey("speed") && lData.containsKey("turn") &&
                                lData.containsKey("step") && lData.containsKey("brake")))
                                    continue

                                val speed = lData["speed"] as Int
                                val turn = lData["turn"] as Int
                                val step = lData["step"] as Int
                                val brake = lData["brake"] as Boolean

                                command = Ev3Motor.stepSync(portId, speed, turn, step, brake)
                            }
                            "stopMotor" -> {
                                if (!lData.containsKey("brake"))
                                    continue

                                val brake = lData["brake"] as Boolean

                                command = Ev3Motor.stopMotor(portId, brake)
                            }
                            "timeAtPower" -> {
                                if (!(lData.containsKey("power") && lData.containsKey("msRampUp") &&
                                lData.containsKey("msContinueRun") && lData
                                .containsKey("msRampDown") && lData.containsKey("brake")))
                                    continue

                                val power = lData["power"] as Int
                                val msRampUp = lData["msRampUp"] as Int
                                val msContinueRun = lData["msContinueRun"] as Int
                                val msRampDown = lData["msRampDown"] as Int
                                val brake = lData["brake"] as Boolean

                                command = Ev3Motor.timeAtPower(portId, power, msRampUp,
                                        msContinueRun, msRampDown, brake)
                            }
                            "timeAtSpeed" -> {
                                if (!(lData.containsKey("speed") && lData.containsKey("msRampUp") &&
                                lData.containsKey("msContinueRun") && lData
                                .containsKey("msRampDown") && lData.containsKey("brake")))
                                    continue

                                val speed = lData["speed"] as Int
                                val msRampUp = lData["msRampUp"] as Int
                                val msContinueRun = lData["msContinueRun"] as Int
                                val msRampDown = lData["msRampDown"] as Int
                                val brake = lData["brake"] as Boolean

                                command = Ev3Motor.timeAtSpeed(portId, speed, msRampUp,
                                msContinueRun, msRampDown, brake)
                            }
                            "timeSync" -> {
                                if (!(lData.containsKey("speed") && lData.containsKey("turn") &&
                                lData.containsKey("msTime") && lData.containsKey("brake")))
                                    continue

                                val speed = lData["speed"] as Int
                                val turn = lData["turn"] as Int
                                val msTime = lData["msTime"] as Int
                                val brake = lData["brake"] as Boolean

                                command = Ev3Motor.timeSync(portId, speed, turn, msTime, brake)
                            }
                            "turnAtPower" -> {
                                if (!lData.containsKey("power"))
                                    continue

                                if (lData["power"] !is Int)
                                    continue

                                val power = lData["power"] as Int

                                command = Ev3Motor.turnAtPower(portId, power)
                            }
                            "turnAtSpeed" -> {
                                if (!lData.containsKey("speed"))
                                    continue

                                val speed = lData["speed"] as Int

                                command = Ev3Motor.turnAtSpeed(portId, speed)
                            }
                            else -> continue
                        }

                        sendCommand(command)
                    }
                }
            }
        }
    }


    override fun off() {
        if (!mRunning)
            return

        runBlocking {
            jobConnection.cancelAndJoin()
        }

        LogElement.info("Ev3", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        mac = baseConfig.mac

        when (data) {
            is Ev3Data -> {
                    mac = data.mac
            }
            is Map<*,*> -> {
                if (data.containsKey("mac"))
                    mac = data["mac"] as String
            }
        }

        val lBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio

        jobConnection = workerConnection(lBTAdapter)

        LogElement.info("Bluetooth", "On")
    }
}