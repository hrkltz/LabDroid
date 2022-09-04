package io.github.herrherklotz.chameleon.x.backend.system.project.component.hw.sensor

import com.fasterxml.jackson.annotation.JsonFilter


@JsonFilter("myFilter")
open class GyroscopeData {
    val blueprints = mapOf(
            "type" to mapOf("type" to "Select", "options" to listOf("raw", "calibrated"))
    )

    var type: String = "raw"
}