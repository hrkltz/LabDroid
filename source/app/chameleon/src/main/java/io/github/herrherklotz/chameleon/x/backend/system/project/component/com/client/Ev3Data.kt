package io.github.herrherklotz.chameleon.x.backend.system.project.component.com.client


import com.fasterxml.jackson.annotation.JsonFilter


@JsonFilter("myFilter")
open class Ev3Data {
    val blueprints = mapOf<String, Map<String, Any>>(
            "mac" to mapOf<String, Any>("type" to "String", "regex" to "^([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2})\$")
    )

    var mac: String = "AA:BB:CC:DD:EE:FF"
}