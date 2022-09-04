package io.github.herrherklotz.chameleon.x.backend.runtime.component.hw

import android.content.Context.VIBRATOR_SERVICE
import android.os.VibrationEffect
import android.os.Vibrator
import io.github.herrherklotz.chameleon.MainService.Companion.context
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.backend.system.project.component.hw.VibratorData
import io.github.herrherklotz.chameleon.x.backend.system.project.component.hw.BatteryData
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object Vibrator: IComponent, VibratorData() {
    private var baseConfig: VibratorData = VibratorData()
    private var mVibrator: Vibrator? = null

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(emptyMap())
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap(mapOf())
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    override fun off() {
        if (!mRunning)
            return

        if (mVibrator != null)
            mVibrator!!.cancel()

        mVibrator = null

        LogElement.info("Vibrator", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        amplitude = baseConfig.amplitude
        duration = baseConfig.duration

        when (data) {
            is Map<*,*> -> {
                if (data.containsKey("amplitude"))
                    amplitude = data["amplitude"] as Double
                if (data.containsKey("duration"))
                    duration = data["duration"] as Int
            }
            is BatteryData -> {
                Battery.interval = data.interval
            }
        }

        mVibrator = context!!.getSystemService(VIBRATOR_SERVICE) as Vibrator
        mVibrator!!.vibrate(VibrationEffect.createOneShot(duration.toLong(), (amplitude*255).toInt()))

        LogElement.info("Vibrator", "On")
    }
}