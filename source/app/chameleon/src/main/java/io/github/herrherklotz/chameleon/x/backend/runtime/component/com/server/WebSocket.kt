package io.github.herrherklotz.chameleon.x.backend.runtime.component.com.server

import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import org.glassfish.tyrus.core.MaxSessions
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import javax.websocket.*
import javax.websocket.server.ServerEndpoint


@ServerEndpoint("/user")
@MaxSessions(1)
object WebSocket: IComponent {
    private var jobSend: Job = Job()
    private var mSession: Session? = null

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(mapOf("Status" to BroadcastChannel(1), "Receive" to
                    BroadcastChannel(1)))
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap(mapOf("Send" to
            Channel(1)))
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    fun delSession() {
        if (mSession != null && mSession!!.isOpen)
            mSession!!.close()

        mSession = null
    }


    fun setSession(session: Session) {
        if (!mRunning) {
            session.close(CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "A"))
            return
        }

        if (mSession != null) {
            session.close(CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "B"))
            return
        }

        mSession = session
    }


    @ExperimentalCoroutinesApi
    fun receive(message: String) {
        // TODO check performance
        // runBlocking { }
        GlobalScope.launch(Dispatchers.Default) {
            mBroadcastChannels["Receive"]!!.send(message)
        }
    }


    @ExperimentalCoroutinesApi
    private fun workerSend(): Job {
        return GlobalScope.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            val lChannel = mChannels["Send"]

            while (coroutineContext.isActive) {
                val lData = lChannel!!.receive()

                if (mSession == null)
                    continue

                when (lData) {
                    is String -> {
                        mSession!!.asyncRemote.sendText(lData)
                    }
                    is ByteArray -> {
                        mSession!!.asyncRemote.sendBinary(lData as ByteBuffer?)
                    }
                    else -> {
                        // TBD try to parse to JSON string?
                        LogElement.error("WebSocket", "Unsupported input data. ")
                    }
                }
            }
        }
    }


    override fun off() {
        if (!mRunning)
            return

        runBlocking {
            jobSend.cancelAndJoin()
        }

        runBlocking {
            jobSend.cancelAndJoin()
        }

        delSession()

        LogElement.info("Bluetooth", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        jobSend = workerSend()

        LogElement.info("WebSocket", "On")
    }
}