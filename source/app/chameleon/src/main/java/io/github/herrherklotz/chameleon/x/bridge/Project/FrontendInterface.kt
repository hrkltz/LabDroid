package io.github.herrherklotz.chameleon.x.bridge.Project

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.herrherklotz.chameleon.x.bridge.elementToPath
import io.github.herrherklotz.chameleon.x.extensions.IInstanceable
import io.github.herrherklotz.chameleon.x.extensions.IObject
import io.github.herrherklotz.chameleon.x.utils.Storage

// TODO use the FileInterface as a base class and just inherit
// object FrontendInterface : FileInterface() {
//   val regex = Regex("^/Project:(.*)/Frontend:([^/]*)$")
// }
object FrontendInterface : IObject, IInstanceable {
    val regex = Regex("^/Project:(.*)/Frontend:([^/]*)$")


    override fun load(element: String): Any {
        // val matchResult = regex.find(element) ?: return false
        // val (projectName, fileName) = matchResult.destructured

        val result = Storage.readFile(elementToPath(element))

        if (!result.first)
            return false // TODO shouldn't return another type.. empty node or any or null?

        return result.second
    }


    override fun create(element: String, content: String): Boolean {
        // val matchResult = regex.find(element) ?: return false
        // val (projectName, fileName) = matchResult.destructured

        return Storage.save(elementToPath(element), jacksonObjectMapper().readValue(content))
    }


    override fun delete(element: String): Boolean {
        return false
    }


    override fun rename(element: String, name: String): Boolean {
        return false
    }


    override fun save(element: String, value: String): Boolean {
        return create(element, value)
    }
}