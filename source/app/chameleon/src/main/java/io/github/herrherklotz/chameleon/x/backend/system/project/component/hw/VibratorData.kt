package io.github.herrherklotz.chameleon.x.backend.system.project.component.hw

import com.fasterxml.jackson.annotation.JsonFilter


@JsonFilter("myFilter")
open class VibratorData {
    val blueprints = mapOf<String, Map<String, Any>>(
            "amplitude" to mapOf<String, Any>("type" to "Double", "min" to 0.1, "max" to 1.0),
            "duration" to mapOf<String, Any>("type" to "Integer", "min" to 1, "max" to 1000)
    )

    var amplitude: Double = 1.0
    var duration: Int = 1000
}