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


object Pressure : IComponent {
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

        LogElement.info("Pressure", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        mSensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager // TODO check if same way (via interface) as setting GPS should be used...

        mSensorManager ?: return

        mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)

        val lBroadcastChannel = mBroadcastChannels["Data"]

        mSensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) { // TODO simply call out((HashMap) myData) to avoid double loop
                // TODO check performance
                // runBlocking { }
                GlobalScope.launch(Dispatchers.Default) {
                    lBroadcastChannel!!.send(event.values[0]);
                }
            }
        }

        mSensorManager!!.registerListener(mSensorEventListener, mSensor, SENSOR_DELAY_FASTEST)

        LogElement.info("Pressure", "On")
    }
}