package io.github.herrherklotz.chameleon.x.backend.system.project.component.hw

import com.fasterxml.jackson.annotation.JsonFilter


@JsonFilter("myFilter")
open class GpsData {
    val blueprints = mapOf<String, Map<String, Any>>(
            "minTime" to mapOf<String, Any>("type" to "Long", "min" to 0, "max" to 60),
            "minDistance" to mapOf<String, Any>("type" to "Float", "min" to 0.0f, "max" to 1000.0f)
    )

    var minTime: Long = 0
    var minDistance: Float = 0.0f
}