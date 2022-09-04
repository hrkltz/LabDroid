package io.github.herrherklotz.chameleon.websocket.endpoint

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper
import io.github.herrherklotz.chameleon.websocket.ApiMessage
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem
import io.github.herrherklotz.chameleon.x.bridge.Bridge.route2
import io.github.herrherklotz.chameleon.x.bridge.ProjectInterface.load
import org.apache.commons.lang3.RandomStringUtils
import org.glassfish.tyrus.core.MaxSessions
import java.io.IOException
import java.util.*
import javax.websocket.*
import javax.websocket.server.PathParam
import javax.websocket.server.ServerEndpoint


@ServerEndpoint("/{sessionId}")
@MaxSessions(1)
class AppEndpoint {
    companion object {
        private val sObjectMapper: ObjectMapper = MyObjectMapper()
        private val threadList: MutableList<Thread> = ArrayList()

        var sSession: Session? = null


        private fun broadcast(pApiMessage: ApiMessage) {
            // At the moment LabDroid only accepts one connection to the AppEndpoint / API.
            try {
                sSession!!.basicRemote.sendText(sObjectMapper.writeValueAsString(pApiMessage))
            } catch (ignore: Exception) {
                // Empty
            }
        }


        fun broadcastSystem() {
            val lApiMessage = ApiMessage("/System", "load", null)

            lApiMessage.Data = try {
                sObjectMapper.writeValueAsString(ChameleonSystem)
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
                ""
            }

            broadcast(lApiMessage)
        }


        fun broadcastProject(projectName: String) {
            val lElement = "/Project:$projectName"
            val lApiMessage = ApiMessage(lElement, "load", null)


            lApiMessage.Data = try {
                sObjectMapper.writeValueAsString(load(lElement))
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
                ""
            }

            broadcast(lApiMessage)
        }
    }


    @OnClose
    fun onClose(@PathParam("sessionId") pSessionId: String?, pSession: Session?) {
        // TODO delete mSession or project?
    }


    @OnError
    fun onError(pThrowable: Throwable) {
        Chameleon.LOG_D("AppEndpoint.onError()", pThrowable.localizedMessage)
    }


    @OnMessage
    fun onMessage(@PathParam("sessionId") pSessionId: String?, pMessage: String?) {
        val itr = threadList.iterator()

        while (itr.hasNext()) {
            if (!itr.next().isAlive)
                itr.remove()
        }

        val lApiCallThread = Thread {
            val lApiCall = try {
                MyObjectMapper().readValue(pMessage, ApiMessage::class.java)
            } catch (e: IOException) {
                ApiMessage("Error", "Parsing", e.message)
            }

            if (lApiCall.Element != "Error") {
                val result = route2(lApiCall.Element, lApiCall.Action, lApiCall.Data)
                lApiCall.Data = sObjectMapper.writeValueAsString(result)
            }

            try {
                sSession!!.basicRemote.sendText(sObjectMapper.writeValueAsString(lApiCall))
            } catch (ignore: Exception) {
                // Empty
            }
        }

        lApiCallThread.name = "OnMessage#" + RandomStringUtils.random(3, true,
                true)
        lApiCallThread.start()
        threadList.add(lApiCallThread)
    }


    @OnOpen
    fun onOpen(@PathParam("sessionId") pSessionId: String?, pSession: Session?) {
        // TODO avoid reopen the same project
        sSession = pSession
    }
}