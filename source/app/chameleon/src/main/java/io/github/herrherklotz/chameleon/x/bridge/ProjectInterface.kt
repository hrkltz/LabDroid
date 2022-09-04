package io.github.herrherklotz.chameleon.x.bridge

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper
import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.backend.system.Project
import io.github.herrherklotz.chameleon.x.extensions.IInstanceable
import io.github.herrherklotz.chameleon.x.extensions.IObject
import io.github.herrherklotz.chameleon.x.utils.Assets
import io.github.herrherklotz.chameleon.x.utils.Storage


object ProjectInterface : IObject, IInstanceable {
    val regex = Regex("^/Project:([^/]*)")
    private val objectMapper: ObjectMapper = MyObjectMapper()


    override fun load(element: String): Any {
        val matchResult = regex.find(element) ?: return false
        val (projectName) = matchResult.destructured

        val result = Storage.readFile("${elementToPath(element)}/Project.json")

        if (!result.first)
            return false // TODO shouldn't return another type.. empty node or any or null?

        val project = Project(projectName)

        return objectMapper.readerForUpdating(project).readValue<Project>(result.second)
    }


    override fun create(element: String, content: String): Boolean {
        val matchResult = regex.find(element) ?: return false
        val (projectName) = matchResult.destructured

        Assets.copyProjectFromAssets(projectName, "templates/Empty")

        AppEndpoint.broadcastSystem()

        return true
    }

    override fun delete(element: String): Boolean {
        val result = Storage.deleteFolder(elementToPath(element))

        if (result)
            AppEndpoint.broadcastSystem()

        return result
    }

    override fun rename(element: String, name: String): Boolean {
        return false //Storage.move(path, name)
    }


    override fun save(element: String, value: String): Boolean {
        val matchResult = regex.find(element) ?: return false
        val (projectName) = matchResult.destructured

        val project = load(element)

        objectMapper.readerForUpdating(project).readValue<Project>(value)

        var filter = SimpleBeanPropertyFilter.serializeAllExcept("nodes",
                "configurations", "files", "projectName")
        objectMapper.setFilterProvider(SimpleFilterProvider().addFilter("myFilter", filter))

        val content2 = objectMapper.writeValueAsString(project)

        filter = SimpleBeanPropertyFilter.serializeAll()
        objectMapper.setFilterProvider(SimpleFilterProvider().addFilter("myFilter", filter))

        val result = Storage.save("${elementToPath(element)}/Project.json", content2)

        if (result)
            AppEndpoint.broadcastProject(projectName)

        return result
    }
}