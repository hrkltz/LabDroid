package io.github.herrherklotz.chameleon.x.backend.runtime.component.hw

import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.Tag
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.eventNfcOff
import io.github.herrherklotz.chameleon.eventNfcOn
import io.github.herrherklotz.chameleon.helper.LogElement
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import okhttp3.internal.and
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object Nfc: IComponent {
    private var mReaderCallback: ReaderCallback? = null

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(mapOf("Data" to BroadcastChannel(1)))
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap()
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    override fun off() {
        if (!mRunning)
            return


        if (mReaderCallback != null)
            eventNfcOff.onNext(mReaderCallback!!)

        mReaderCallback = null

        LogElement.info("NFC", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        val lBroadcastChannelData = mBroadcastChannels["Data"]

        mReaderCallback = ReaderCallback { tag: Tag ->
            val buffer = StringBuffer()

            for (i in tag.id.indices) {
                buffer.append(Character.forDigit((tag.id[i].toInt().shr(4)) and 0x0F, 16))
                buffer.append(Character.forDigit(tag.id[i] and 0x0F, 16))
            }

            GlobalScope.launch(Dispatchers.Default) {
                lBroadcastChannelData!!.send(buffer.toString())
            }
        }

        eventNfcOn.onNext(mReaderCallback!!)

        LogElement.info("NFC", "On")
    }
}