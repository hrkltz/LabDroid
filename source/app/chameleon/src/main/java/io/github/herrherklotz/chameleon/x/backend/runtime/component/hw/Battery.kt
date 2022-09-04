package io.github.herrherklotz.chameleon.x.backend.runtime.component.hw

import io.github.herrherklotz.chameleon.eventBatteryTechnology
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.eventReadBattery
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.backend.system.project.component.hw.BatteryData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors


object Battery: IComponent, BatteryData() {
    private var baseConfig: BatteryData = BatteryData()
    private var jobWorker: Job = Job()

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(mapOf("Data" to BroadcastChannel(1)))
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap(emptyMap())
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(mapOf(
            "Technology" to "")) // ConcurrentMap doesn't support null for key & value
    override var mRunning: Boolean = false


    private fun worker(): Job {
        return GlobalScope.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            while (coroutineContext.isActive) {
                eventReadBattery.onNext(Unit)
                delay(Battery.interval)
            }
        }
    }


    override fun off() {
        if (!mRunning)
            return

        runBlocking {
            jobWorker.cancelAndJoin()
        }

        LogElement.info("Battery", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        // TBD If running do a restart? eg. if the config changed
        if (mRunning)
            return

        mRunning = true

        eventBatteryTechnology.onNext(Unit)

        interval = baseConfig.interval

        when (data) {
            is Map<*,*> -> {
                if (data.containsKey("interval"))
                    interval = (data["interval"] as Int).toLong()
            }
            is BatteryData -> {
                interval = data.interval
            }
        }

        jobWorker = worker()

        LogElement.info("Battery", "On")
    }
}