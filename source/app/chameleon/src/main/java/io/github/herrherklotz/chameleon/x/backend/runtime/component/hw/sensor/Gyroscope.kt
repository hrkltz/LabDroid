package io.github.herrherklotz.chameleon.x.backend.runtime.component.hw.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import io.github.herrherklotz.chameleon.MainService.Companion.context
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.x.backend.system.project.component.hw.sensor.GyroscopeData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object Gyroscope : IComponent, GyroscopeData() {
    private var mSensorManager: SensorManager? = null
    private var mSensorEventListener: SensorEventListener? = null
    private var mSensor: Sensor? = null
    private var baseConfig: GyroscopeData = GyroscopeData()

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> = ConcurrentHashMap(mapOf("Data"
            to BroadcastChannel<Any>(1)))
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap()
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    override fun off() {
        if (!mRunning)
            return

        if (mSensorManager != null && mSensorEventListener != null && mSensor != null)
            mSensorManager!!.unregisterListener(mSensorEventListener, mSensor)

        mSensorEventListener = null
        mSensorManager = null
        mSensor = null

        LogElement.info("Gyroscope", "Off")

        mRunning = false
    }


    @ExperimentalCoroutinesApi
    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        type = baseConfig.type

        when (data) {
            is Map<*,*> -> {
                if (data.containsKey("type"))
                    type = data["type"] as String
            }
            is GyroscopeData -> {
                type = data.type
            }
        }

        mSensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager // TODO check if same way (via interface) as setting GPS should be used...

        mSensorManager ?: return

        mSensor = when (type) {
            "raw" -> mSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)
            "calibrated" -> mSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            else -> return
        }

        if (mSensor == null) {
            LogElement.error("Gyroscope", "Your device doesn't support the type \"${type}\".")
            return
        }

        val lBroadcastChannel = mBroadcastChannels["Data"]!!
        val lData: MutableMap<String, Any> = HashMap()

        mSensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) { // TODO simply call out((HashMap) myData) to avoid double loop
                lData["x"] = event.values[0]
                lData["y"] = event.values[1]
                lData["z"] = event.values[2]

                runBlocking {
                    lBroadcastChannel.send(lData);
                }
            }
        }

        mSensorManager!!.registerListener(mSensorEventListener, mSensor, SENSOR_DELAY_FASTEST)

        LogElement.info("Gyroscope", "On")
    }
}