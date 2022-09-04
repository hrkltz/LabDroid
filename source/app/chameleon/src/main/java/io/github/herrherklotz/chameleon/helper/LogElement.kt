package io.github.herrherklotz.chameleon.helper

import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.websocket.endpoint.LogEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


enum class eLogElementType(val value: Byte) {
    INFO(0),
    ERROR(1);
}


class LogElement internal constructor(val type: Byte, val source: String, val message: String) {
    var timestamp: Long
    var time: String


    companion object {
        private var counter = 0L
            get() {
                return field++
            }


        @JvmStatic
        fun info(source: String, message: String) {
            // TODO How to avoid to high message frequency? or message queuing
            // Option A: sleep for 1 ms?
            LogElement(eLogElementType.INFO.value, source, message)
        }


        @JvmStatic
        fun error(source: String, message: String) {
            // TODO How to avoid to high message frequency? or message queuing
            LogElement(eLogElementType.ERROR.value, source, message)
        }
    }


    init {
        timestamp = counter
        time = Chameleon.sTIMEFORMAT_MS.format(Date())

        val logElement = this
        GlobalScope.launch(Dispatchers.IO) {
            LogEndpoint.broadcast(logElement)
        }
    }
}