package io.github.herrherklotz.chameleon.x.backend.system.project.node.script

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonSetter
import io.github.herrherklotz.chameleon.x.backend.system.project.Node
import io.github.herrherklotz.chameleon.x.backend.system.project.Port


open class ScriptNode @JsonCreator constructor() : Node("node/script") {
    var code : String = ""
}