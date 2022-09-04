package io.github.herrherklotz.chameleon.websocket

import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.websocket.endpoint.LogEndpoint
import io.github.herrherklotz.chameleon.websocket.endpoint.UserEndpoint
import org.glassfish.tyrus.server.TyrusServerContainer
import org.glassfish.tyrus.spi.ServerContainerFactory


class WebSocketServer {
    private val mServer: TyrusServerContainer?


    fun stop() {
        mServer?.stop()
    }


    init {
        mServer = ServerContainerFactory.createServerContainer(null) as
                TyrusServerContainer

        try {
            mServer.start("/", 8081)
            mServer.webSocketEngine.register(LogEndpoint::class.java, "/")
            mServer.webSocketEngine.register(UserEndpoint::class.java, "/")
            mServer.webSocketEngine.register(AppEndpoint::class.java, "/api")
        } catch (ignore: Exception) {
            // Empty
        }
    }
}