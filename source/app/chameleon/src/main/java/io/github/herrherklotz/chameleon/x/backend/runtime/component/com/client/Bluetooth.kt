package io.github.herrherklotz.chameleon.x.backend.runtime.component.com.client

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.x.backend.system.project.component.com.client.BluetoothData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/* TODO
BroadcastChannel capacity
string || byte stream
 */

// Test MAC: 00:06:66:43:06:05
object Bluetooth : IComponent, BluetoothData() {
    private var baseConfig: BluetoothData = BluetoothData()
    private var jobConnection: Job = Job()
    private var jobReceive: Job = Job()
    private var jobSend: Job = Job()
    private var mBTSocket: BluetoothSocket? = null
    private val BTMODULEUUID: UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB") // "random" unique identifier
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(mapOf("Receive" to BroadcastChannel(1))) // TODO "Status" to BroadcastChannel(1),
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap(mapOf("Send" to
                Channel(1)))
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    private fun workerConnection(bluetoothAdapter: BluetoothAdapter): Job {
        return coroutineScope.launch {
            try {
                while (coroutineContext.isActive) {
                    val lBluetoothDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(mac)

                    try {
                        mBTSocket = lBluetoothDevice.createRfcommSocketToServiceRecord(BTMODULEUUID)
                    } catch (ignore: IOException) {
                        LogElement.error("Bluetooth", "Couldn't establish the Socket.")
                        continue
                    }

                    bluetoothAdapter.cancelDiscovery()

                    try {
                        mBTSocket!!.connect()
                    } catch (ignore: IOException) {
                        LogElement.error("Bluetooth", ignore.localizedMessage)

                        try {
                            mBTSocket!!.close()
                        } catch (ignore2: IOException) {}

                        delay(100)

                        continue
                    }

                    try {
                        mOutputStream = mBTSocket!!.getOutputStream()
                        mInputStream = mBTSocket!!.getInputStream()
                    } catch (ignore: IOException) {
                        LogElement.error("Bluetooth", "Couldn't extract Sockets.")
                        continue
                    }

                    jobReceive = workerReceive()
                    jobSend = workerSend()

                    // TODO mChannelStatus.send(1)

                    // DoIt
                    while (mBTSocket!!.isConnected)
                        delay(200)

                    // TODO lock until BluetoothBroadcastReceiver gots ACL_DISCONNECT or OFF
                    LogElement.error("Bluetooth", "lost connection")

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
            jobSend.cancelAndJoin()
            jobSend = Job()
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
        return coroutineScope.launch {
            val lBroadcastReceive = mBroadcastChannels["Receive"]

            var lByteBuffer: ByteArray // lByteBuffer store for the stream

            var lStringBuilder = StringBuilder()
            var lByteCounter: Int

            while (coroutineContext.isActive) {
                lByteCounter = try {
                    mInputStream!!.available()
                } catch (ignore: IOException) {
                    break;
                }

                if (lByteCounter == 0) {
                    delay(100)
                    continue
                }

                if (lByteCounter > bufferSize)
                    lByteCounter = bufferSize

                lByteBuffer = ByteArray(lByteCounter)

                try {
                    mInputStream!!.read(lByteBuffer, 0, lByteBuffer.size) // record how many bytes we actually read
                } catch (ignore: IOException) {
                    continue
                }

                val lInputData = String(lByteBuffer)

                if (terminator.isNotEmpty()) {
                    if (lInputData.contains(terminator)) {
                        var lInputDataSplitted = lInputData.split(terminator)

                        if (lInputDataSplitted.last().isEmpty())
                            lInputDataSplitted = lInputDataSplitted.dropLast(1)

                        lInputDataSplitted.forEachIndexed { index, it ->
                            lStringBuilder.append(it)

                            if ((index < lInputDataSplitted.size-1) || (lInputData.endsWith(
                                            terminator))) {
                                lBroadcastReceive!!.send(lStringBuilder.toString())
                                lStringBuilder = StringBuilder()
                            }
                        }
                    } else
                        lStringBuilder.append(lInputData)
                } else
                    lBroadcastReceive!!.send(lInputData)
            }
        }
    }


    @ExperimentalCoroutinesApi
    private suspend fun workerSend(): Job {
        return coroutineScope.launch {
            val lChannel = mChannels["Send"]

            while (coroutineContext.isActive) {
                when (val lData = lChannel!!.receive()) {
                    is String -> {
                        try {
                            mOutputStream!!.write((lData).toByteArray(charset("UTF-8")))
                        } catch (ignore: IOException) { }
                    }
                    is ByteArray -> {
                        try {
                            mOutputStream!!.write(lData)
                        } catch (ignore: IOException) { }
                    }
                    else -> {
                        // TBD try to parse to JSON string?
                        LogElement.error("Bluetooth", "Unsupported input data. ")
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

        LogElement.info("Bluetooth", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        mac = baseConfig.mac
        terminator = baseConfig.terminator
        bufferSize = baseConfig.bufferSize

        when (data) {
            is BluetoothData -> {
                    mac = data.mac
                    terminator = data.terminator
                    bufferSize = data.bufferSize
            }
            is Map<*,*> -> {
                if (data.containsKey("mac"))
                    mac = data["mac"] as String
                if (data.containsKey("terminator"))
                    terminator = data["terminator"] as String
                if (data.containsKey("bufferSize"))
                    bufferSize = data["bufferSize"] as Int
            }
        }

        val lBTAdapter = BluetoothAdapter.getDefaultAdapter() // get a handle on the bluetooth radio

        /* Recheck
        if (lBTAdapter == null) {
            stop()
            return
        }*/

        jobConnection = workerConnection(lBTAdapter)

        LogElement.info("Bluetooth", "On")
    }
}