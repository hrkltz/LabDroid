package io.github.herrherklotz.chameleon.x.backend.system.project.component.sw

import com.fasterxml.jackson.annotation.JsonFilter


@JsonFilter("myFilter")
open class TextToSpeechData {
    val blueprints = mapOf<String, Map<String, Any>>(
            "text" to mapOf<String, Any>("type" to "String", "regex" to "^[0-9A-Za-z !.,?&]*\$")
    )

    var text: String = "Hello World!"
}