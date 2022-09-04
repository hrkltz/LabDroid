package io.github.herrherklotz.chameleon.x.backend.system.project.component.com.client


import com.fasterxml.jackson.annotation.JsonFilter


@JsonFilter("myFilter")
open class BluetoothData {
    val blueprints = mapOf<String, Map<String, Any>>(
            "mac" to mapOf<String, Any>("type" to "String", "regex" to "^([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2})\$"),
            "terminator" to mapOf<String, Any>("type" to "Select", "options" to listOf("", "\n", "\r\n")),
            "bufferSize" to mapOf<String, Any>("type" to "Integer", "min" to 8, "max" to 512)
    )

    var mac: String = "AA:BB:CC:DD:EE:FF"
    var terminator: String = "\n"
    var bufferSize: Int = 128
}