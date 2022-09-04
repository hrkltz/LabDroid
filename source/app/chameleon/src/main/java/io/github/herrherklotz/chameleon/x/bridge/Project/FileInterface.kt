package io.github.herrherklotz.chameleon.x.bridge.Project

import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.bridge.elementToPath
import io.github.herrherklotz.chameleon.x.extensions.IInstanceable
import io.github.herrherklotz.chameleon.x.extensions.IObject
import io.github.herrherklotz.chameleon.x.utils.Storage


object FileInterface : IObject, IInstanceable {
    val regex = Regex("^/Project:(.*)/File:([^/]*)$")


    override fun load(element: String): Any {
        val matchResult = regex.find(element) ?: return false
        val (projectName, fileName) = matchResult.destructured

        val result: Pair<Boolean, String>
        val path = elementToPath(element)

        result = Storage.readFile(path)


        if (!result.first)
            return false // TODO shouldn't return another type.. empty node or any or null?

        return result.second
    }


    override fun create(element: String, value: String): Boolean {
        val matchResult = regex.find(element) ?: return false
        val (projectName, fileName) = matchResult.destructured

        val result = Storage.save(elementToPath(element), value)

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }


    override fun delete(element: String): Boolean {
        val matchResult = regex.find(element) ?: return false
        val (projectName, fileName) = matchResult.destructured

        val result = Storage.deleteFile(elementToPath(element))

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }


    override fun rename(element: String, name: String): Boolean {
        return false
    }


    override fun save(element: String, value: String): Boolean {
        return create(element, value)
    }
}