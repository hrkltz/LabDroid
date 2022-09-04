package io.github.herrherklotz.chameleon.x.extensions

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentMap


interface IComponent {
    val mRunning: Boolean
    val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>>
    val mPoolChannels: ConcurrentMap<String, Any?>
    val mChannels: ConcurrentMap<String, Channel<Any>>

    fun on(data: Any?)
    fun off()
}