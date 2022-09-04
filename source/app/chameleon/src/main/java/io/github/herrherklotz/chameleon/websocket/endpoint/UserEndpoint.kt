package io.github.herrherklotz.chameleon.websocket.endpoint

import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.x.backend.runtime.component.com.server.WebSocket
import org.glassfish.tyrus.core.MaxSessions
import javax.websocket.*
import javax.websocket.server.ServerEndpoint


@ServerEndpoint("/user")
@MaxSessions(1)
class UserEndpoint {
    @OnClose
    fun onClose(pSession: Session?) {
        WebSocket.delSession()
    }


    @OnError
    fun onError(t: Throwable?) {
        Chameleon.LOG_D("EnvironmentSocket.onError()")
    }


    @OnMessage
    fun onMessage(message: String?) {
        WebSocket.receive(message!!)
    }


    @OnOpen
    fun onOpen(pSession: Session?) {
        WebSocket.setSession(pSession!!)
    }
}