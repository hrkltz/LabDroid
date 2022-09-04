package io.github.herrherklotz.chameleon.x.bridge.Project.Node

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper
import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.backend.system.project.node.script.ScriptNode
import io.github.herrherklotz.chameleon.x.bridge.ProjectInterface
import io.github.herrherklotz.chameleon.x.bridge.elementToFilePath
import io.github.herrherklotz.chameleon.x.extensions.IInstanceable
import io.github.herrherklotz.chameleon.x.extensions.IObject
import io.github.herrherklotz.chameleon.x.utils.Storage
import org.apache.commons.lang3.RandomStringUtils
import java.nio.file.Paths


object ScriptInterface : IObject, IInstanceable {
    val regex = Regex("^/Project:(.*)/Node/Script:([^/]*)")
    private val objectMapper: ObjectMapper = MyObjectMapper()


    override fun create(element: String, content: String): Boolean {
        val matchResult = regex.find(element) ?: return false

        val (projectName, nodeName) = matchResult.destructured

        val elementParent = Paths.get(elementToFilePath(element)).parent.toString()

        var randomId: String

        do {
            randomId = RandomStringUtils.random(8, true, false) + ".json"
        } while (Storage.readFile(Paths.get(elementParent, randomId)).first)

        val node = ScriptNode()

        objectMapper.readerForUpdating(node).readValue<ScriptNode>(content)

        val elementPath = Paths.get(elementParent, randomId).toString()
        val result = Storage.save(elementPath, objectMapper.writeValueAsString(node))

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }


    override fun delete(element: String): Boolean {
        val matchResult = regex.find(element) ?: return false

        val (projectName, nodeName) = matchResult.destructured

        val node: ScriptNode = load(element) as ScriptNode

        node.inputs?.withIndex()?.forEach {
            ProjectInterface.save("/Project:$projectName", "{\"disconnect\": [\"Script/$nodeName\", \"${it.index}\", null, null]}")
        }
        node.outputs?.withIndex()?.forEach {
            ProjectInterface.save("/Project:$projectName", "{\"disconnect\": [null, null, \"Script/$nodeName\", \"${it.index}\"]}")
        }

        val result = Storage.deleteFile(elementToFilePath(element))

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }


    override fun rename(path: String, name: String): Boolean {
        return false
    }


    override fun load(element: String): Any {
        val matchResult = regex.find(element) ?: return false

        val (projectName, nodeName) = matchResult.destructured
        val filePath = elementToFilePath(element)

        val result = Storage.readFile(filePath)

        if (!result.first)
            return false // TODO shouldn't return another type.. empty node or any or null?

        val node = ScriptNode()

        return objectMapper.readerForUpdating(node).readValue<ScriptNode>(result.second)
    }

    // TBD: We could merge save and create. if file exists: load, update otherwise new ScriptNode()
    // but hten we lost a little bit of logic. eg. where wil lwe object created and some objects
    // cannot be created (eg. System)
    override fun save(element: String, value: String): Boolean {
        val matchResult = regex.find(element) ?: return false

        val (projectName, nodeName) = matchResult.destructured

        var node = load(element)

        objectMapper.readerForUpdating(node).readValue<ScriptNode>(value)

        val result = Storage.save(elementToFilePath(element), objectMapper.writeValueAsString(node))

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }
}