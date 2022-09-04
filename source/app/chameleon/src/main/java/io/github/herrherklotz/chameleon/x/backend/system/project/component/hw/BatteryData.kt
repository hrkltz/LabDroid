package io.github.herrherklotz.chameleon.x.backend.system.project.component.hw

import com.fasterxml.jackson.annotation.JsonFilter


@JsonFilter("myFilter")
open class BatteryData {
    val blueprints = mapOf<String, Map<String, Any>>(
            "interval" to mapOf<String, Any>("type" to "Long", "min" to 1, "max" to 60000) // 10[ms] to 60[s]
    )

    var interval: Long = 1000
}