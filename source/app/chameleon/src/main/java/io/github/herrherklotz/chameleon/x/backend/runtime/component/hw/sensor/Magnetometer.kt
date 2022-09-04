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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object Magnetometer : IComponent {
    private var mSensorManager: SensorManager? = null
    private var mSensorEventListener: SensorEventListener? = null
    private var mSensor: Sensor? = null

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

        LogElement.info("Magnetometer", "Off")

        mRunning = false
    }


    @ExperimentalCoroutinesApi
    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        mSensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager // TODO check if same way (via interface) as setting GPS should be used...

        mSensorManager ?: return

        mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

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

        LogElement.info("Magnetometer", "On")
    }
}