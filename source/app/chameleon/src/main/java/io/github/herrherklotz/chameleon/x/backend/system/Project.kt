package io.github.herrherklotz.chameleon.x.backend.system

import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.herrherklotz.chameleon.MainService
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper
import io.github.herrherklotz.chameleon.x.backend.system.project.Node
import io.github.herrherklotz.chameleon.x.backend.system.project.node.script.ScriptNode
import io.github.herrherklotz.chameleon.x.bridge.Project.ComponentInterface
import io.github.herrherklotz.chameleon.x.bridge.Project.Node.ScriptInterface
import io.github.herrherklotz.chameleon.x.utils.Storage
import io.github.herrherklotz.chameleon.x.utils.Storage.listComponentsRecursive
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


@JsonFilter("myFilter")
open class Project(val projectName: String) {
    @JsonIgnore
    protected val objectMapper = MyObjectMapper()

    var screen = "black"
    var connections: ArrayList<Array<String>> = ArrayList<Array<String>>()
    var variables: ConcurrentMap<String, Variable> = ConcurrentHashMap<String, Variable>()
        set(value) {
            val deleted = variables - value.keys

            for ((key, value) in deleted) {
                val inputLink = arrayOf("component", "variables", key, "poll")
                val outputLink = arrayOf("component", "variables", key, "stream")

                for ((nodeGroupKey, nodeGroupValue) in nodes) {
                    for ((nodeKey, nodeValue) in nodeGroupValue) {
                        nodeValue.inputs.forEachIndexed { i, it ->
                            if ((it != null) && (it contentEquals inputLink)) {
                                nodeValue.inputs[i] = null
                                ScriptInterface.save("/Project:${projectName}/Node/Script:${nodeKey}", "{\"inputs\": ${objectMapper.writeValueAsString(nodeValue.inputs)}}")
                            }
                        }

                        nodeValue.outputs.forEachIndexed { i, it ->
                            if ((it != null) && (it contentEquals outputLink)) {
                                nodeValue.outputs[i] = null
                                ScriptInterface.save("/Project:${projectName}/Node/Script:${nodeKey}", "{\"outputs\": ${objectMapper.writeValueAsString(nodeValue.outputs)}}")
                            }
                        }
                    }
                }
            }

            field = value
        }


    @JsonSetter("connect")
    fun connect(connection: Array<String>) {
        connections.forEach {
            if (it[0] == connection[0] && (it[1] == connection[1]))
                disconnect(arrayOf(it[0], it[1], null, null))
            else if (it[2] == connection[2] && (it[3] == connection[3]))
                disconnect(arrayOf(null, null, it[2], it[3]))
        }

        connections.add(connection)
    }


    @JsonSetter("disconnect")
    fun disconnect(disconnect: Array<String?>) {
        if ((disconnect[0] != null) && (disconnect[1] != null))
            connections = ArrayList(connections.filter {
                (it[0] != disconnect[0]) || (it[1] != disconnect[1])
            })
        else if ((disconnect[2] != null) && (disconnect[3] != null))
            connections = ArrayList(connections.filter {
                (it[2] != disconnect[2] || it[3] != disconnect[3])
            })
    }


    val nodes: Map<String, Map<String, Node>>
        get() {
            val _nodes = HashMap<String, Map<String, Node>>()

            val nodesPath = Paths.get(MainService.context!!.filesDir.absolutePath, "Project",
                    projectName, "Node")

            if (!Files.exists(nodesPath))
                return _nodes

            Files.list(nodesPath)
                    .filter { Files.isDirectory(it) }
                    .forEach { folder ->
                        val map = HashMap<String, Node>()

                        Files.list(folder)
                                .filter { Files.isRegularFile(it) }
                                .forEach {
                                    val nodeObject = Storage.readFile(it)

                                    if (nodeObject.first)
                                        map[it.fileName.toString().removeSuffix(".json")] =
                                                objectMapper.readValue<ScriptNode>(nodeObject.second)
                                }

                        _nodes[folder.fileName.toString()] = map
                    }

            return _nodes
        }


    val configurations: Map<String, Any>
        get() {
            val result = mutableMapOf<String, Any>()

            val components = listComponentsRecursive(projectName)
            components.forEach {
                val component = ComponentInterface.load("/Project:${projectName}/Component:${it}")
                result[it] = component
            }

            return result
        }


    val files: List<String>
        get() {
            val _files = mutableListOf<String>()

            val nodesPath = Paths.get(MainService.context!!.filesDir.absolutePath, "Project",
                    projectName, "File")

            if (!Files.exists(nodesPath))
                return _files

            Files.list(nodesPath)
                    .filter { Files.isRegularFile(it) }
                    .forEach {
                        val nodeObject = Storage.readFile(it)

                        if (nodeObject.first)
                            _files.add(it.fileName.toString())
                    }

            return _files
        }
}