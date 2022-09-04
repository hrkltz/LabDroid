package io.github.herrherklotz.chameleon.x.bridge.Project

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper
import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.bridge.elementToClassPath2
import io.github.herrherklotz.chameleon.x.bridge.elementToFilePath
import io.github.herrherklotz.chameleon.x.extensions.IInstanceable
import io.github.herrherklotz.chameleon.x.extensions.IObject
import io.github.herrherklotz.chameleon.x.utils.Storage


object ComponentInterface : IObject, IInstanceable {
    val regex = Regex("^/Project:(.*)/Component:([^/]*)")
    private val objectMapper: ObjectMapper = MyObjectMapper()


    private fun loadClass(element: String, configurationName: String): Class<*>? {
        var classPath = elementToClassPath2("backend.system", element)
        classPath += ".${configurationName.replace("_", ".")}Data"

        return try {
            Class.forName(classPath)
        } catch (e: ClassNotFoundException) {
            e.stackTrace
            null
        }
    }


    override fun load(element: String): Any {
        val matchResult = regex.find(element) ?: return false

        val (projectName, configurationName) = matchResult.destructured

        val javaClass = loadClass(element, configurationName) ?: return false

        val result = Storage.readFile(elementToFilePath(element))

        if (!result.first)
            return false // TODO shouldn't return another type.. empty node or any or null?

        //val component = javaClass.newInstance()
        //objectMapper.readerForUpdating(component).readValue(result.second, javaClass)
        return objectMapper.readValue(result.second, javaClass)
    }


    override fun create(element: String, content: String): Boolean {
        val matchResult = regex.find(element) ?: return false

        val (projectName, configurationName) = matchResult.destructured

        val javaClass = loadClass(element, configurationName)
        var componentJson = ""

        if (javaClass != null) {
            val component = objectMapper.readValue(content, javaClass)

            var filter = SimpleBeanPropertyFilter.serializeAllExcept("blueprints")
            objectMapper.setFilterProvider(SimpleFilterProvider().addFilter("myFilter", filter))

            componentJson = objectMapper.writeValueAsString(component)

            filter = SimpleBeanPropertyFilter.serializeAll()
            objectMapper.setFilterProvider(SimpleFilterProvider().addFilter("myFilter", filter))
        }

        val result = Storage.save(elementToFilePath(element), componentJson)

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }

    override fun delete(element: String): Boolean {
        val matchResult = regex.find(element) ?: return false

        val (projectName, configurationName) = matchResult.destructured

        val result = Storage.deleteFile(elementToFilePath(element))

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }

    override fun rename(path: String, name: String): Boolean {
        TODO("Not yet implemented")
    }



    override fun save(element: String, value: String): Boolean {
        val matchResult = regex.find(element) ?: return false

        val (projectName, configurationName) = matchResult.destructured

        // val javaClass = loadClass(element, configurationName) ?: return false

        val component = load(element) // if we return null -> ?: return false

        objectMapper.readerForUpdating(component).readValue<Any>(value)

        var filter = SimpleBeanPropertyFilter.serializeAllExcept("blueprints")
        objectMapper.setFilterProvider(SimpleFilterProvider().addFilter("myFilter", filter))

        val componentJson = objectMapper.writeValueAsString(component)

        filter = SimpleBeanPropertyFilter.serializeAll()
        objectMapper.setFilterProvider(SimpleFilterProvider().addFilter("myFilter", filter))

        val result = Storage.save(elementToFilePath(element), componentJson)

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }
}