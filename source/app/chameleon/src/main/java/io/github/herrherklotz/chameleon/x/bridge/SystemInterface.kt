package io.github.herrherklotz.chameleon.x.bridge

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper
import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem
import io.github.herrherklotz.chameleon.x.extensions.IObject


object SystemInterface : IObject {
    private val objectMapper: ObjectMapper = MyObjectMapper()


    override fun load(path: String): Any {
        return ChameleonSystem
    }


    override fun save(path: String, value: String): Boolean {
        Chameleon.LOG_D("Thread: " + Thread.currentThread().name + " (SystemInterface.save)")
        // TODO catch JsonProcessingException, JsonMappingException
        objectMapper.readerForUpdating(ChameleonSystem).readValue<Any>(value) // TBD readValue<Chameleon>() leads to endless loop

        AppEndpoint.broadcastSystem()
        return true
    }
}