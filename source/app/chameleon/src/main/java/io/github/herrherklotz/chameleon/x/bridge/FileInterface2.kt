package io.github.herrherklotz.chameleon.x.bridge

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.extensions.IInstanceable
import io.github.herrherklotz.chameleon.x.extensions.IObject
import io.github.herrherklotz.chameleon.x.utils.Storage


object FileInterface2 : IObject, IInstanceable {
    val regex = Regex("^/Project//(.*)///(.*)$")


    override fun load(element: String): Any {
        val matchResult = regex.find(element) ?: return false
        val (projectName, fileName) = matchResult.destructured

        val result: Pair<Boolean, Any> = if (element.endsWith("/"))
            Storage.readDirectory(element)
        else
            Storage.readFile(element)

        if (!result.first)
            return false // TODO shouldn't return another type.. empty node or any or null?

        return result.second
    }


    override fun create(element: String, value: String): Boolean {
        val matchResult = regex.find(element) ?: return false
        val (projectName, fileName) = matchResult.destructured

        val result = Storage.save(elementToPath(element), jacksonObjectMapper().readValue<String>(value))

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }


    fun createFolder(element: String): Boolean {
        val matchResult = regex.find(element) ?: return false
        val (projectName, fileName) = matchResult.destructured


        val result = Storage.saveFolder(elementToPath(element))

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