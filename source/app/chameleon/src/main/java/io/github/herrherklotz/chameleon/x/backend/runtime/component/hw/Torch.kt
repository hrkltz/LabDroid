package io.github.herrherklotz.chameleon.x.backend.runtime.component.hw

import io.github.herrherklotz.chameleon.eventTorch
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object Torch: IComponent {
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> = ConcurrentHashMap(emptyMap())
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap()
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    override fun off() {
        if (!mRunning)
            return

        eventTorch.onNext(false)

        LogElement.info("Torch", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        eventTorch.onNext(true)

        LogElement.info("Torch", "On")
    }
}