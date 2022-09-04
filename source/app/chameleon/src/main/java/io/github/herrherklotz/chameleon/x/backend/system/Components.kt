package io.github.herrherklotz.chameleon.x.backend.system

import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.x.backend.runtime.component.com.client.*
import io.github.herrherklotz.chameleon.x.backend.runtime.component.com.server.WebSocket
import io.github.herrherklotz.chameleon.x.backend.runtime.component.hw.*
import io.github.herrherklotz.chameleon.x.backend.runtime.component.hw.sensor.*
import io.github.herrherklotz.chameleon.x.backend.runtime.component.sw.TextToSpeech
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap


object Components: ConcurrentHashMap<String, IComponent>() {
    init {
        put("com_client_Bluetooth", Bluetooth)
        put("com_client_ev3", Ev3)
        put("com_server_WebSocket", WebSocket)
        put("hw_Battery", Battery)
        put("hw_Gps", Gps)
        put("hw_Nfc", Nfc)
        put("hw_sensor_Accelerometer", Accelerometer)
        put("hw_sensor_AmbientTemperature", AmbientTemperature)
        put("hw_sensor_Gyroscope", Gyroscope)
        put("hw_sensor_Magnetometer", Magnetometer)
        put("hw_sensor_Light", Light)
        put("hw_sensor_Pressure", Pressure)
        put("hw_sensor_Proximity", Proximity)
        put("hw_sensor_RelativeHumidity", RelativeHumidity)
        put("hw_Vibrator", Vibrator)
        //put("hw_Torch", Torch)
        put("sw_TextToSpeech", TextToSpeech)

        this.forEach { key: String, value: IComponent ->
            value.mChannels.putAll(mapOf("Status" to Channel(1)))
        }
    }
}