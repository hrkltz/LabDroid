package io.github.herrherklotz.chameleon.websocket.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrherklotz.chameleon.BuildConfig
import io.github.herrherklotz.chameleon.MainService
import io.github.herrherklotz.chameleon.R
import io.github.herrherklotz.chameleon.helper.LogElement
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.*
import javax.websocket.server.ServerEndpoint


@ServerEndpoint("/log")
class LogEndpoint {
    companion object {
        private val sessions: MutableMap<String, Session> = ConcurrentHashMap() //ConcurrentHashMap?
        private val objectWriter = ObjectMapper().writer()


        fun broadcast(logElement: LogElement) {
            // TODO We could add the logElement to a FIFO and start a Thread (if not running) which
            // TODO empty the FIFO.
            val message = objectWriter.writeValueAsString(logElement)

            sessions.forEach { (eSessionId: String?, eSession: Session) ->
                try {
                    eSession.basicRemote.sendText(message)
                } catch (ignore: IOException) {
                    // Empty
            }}
        }
    }


    @OnClose
    fun onClose(pSession: Session) {
        sessions.remove(pSession.id)
    }


    @OnError
    fun onError(t: Throwable?) {
        // Empty
    }


    @OnMessage
    fun onMessage(message: String?) {
        // Empty
    }


    @OnOpen
    @Throws(IOException::class)
    fun onOpen(pSession: Session) {
        sessions[pSession.id] = pSession

        MainService.context ?: return

        val appName = MainService.context!!.resources.getString(R.string.app_name)
        LogElement.info(appName, "Welcome to $appName v${BuildConfig.VERSION_NAME}.")
    }
}