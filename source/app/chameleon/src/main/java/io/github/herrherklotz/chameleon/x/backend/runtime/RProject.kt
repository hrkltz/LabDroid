package io.github.herrherklotz.chameleon.x.backend.runtime

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.backend.runtime.project.node.RNode
import io.github.herrherklotz.chameleon.x.backend.system.Components
import io.github.herrherklotz.chameleon.x.backend.system.Project
import io.github.herrherklotz.chameleon.x.bridge.ProjectInterface
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass


class RProject(val projectName: String) {
    private val _objectWriter = ObjectMapper().writer()
    var project: Project = ProjectInterface.load("/Project:$projectName") as Project
    var oGroups: MutableMap<String, MutableMap<String, RNode>>? = null
    val changingState = AtomicBoolean(false)


    fun off(): Boolean {
        if (changingState.get())
            return false

        changingState.set(true)
        LogElement.info(projectName, "Off")

        Components.forEach {
            it.value.off()
        }

        oGroups!!.forEach { group ->
            group.value.forEach { node ->
                node.value.off()
            }
        }

        ProjectInterface.save("/Project:${projectName}", "{\"variables\": " +
                "${_objectWriter.writeValueAsString(project.variables)}}")

        changingState.set(false)

        return true
    }


    @ExperimentalCoroutinesApi
    fun on(): Boolean {
        Chameleon.LOG_D("Thread: " + Thread.currentThread().name + " (RProject.on)")
        if (changingState.get())
            return false

        changingState.set(true)

        LogElement.info(projectName, "On")

        project.configurations.forEach {
            Components[it.key]!!.on(it.value)
        }

        oGroups = mutableMapOf()

        project.nodes.forEach { it1 ->
            val groupName = it1.key.toLowerCase()
            oGroups!![it1.key] = mutableMapOf<String, RNode>()

            val javaClass: Class<*>

            try {
                javaClass = Class.forName("io.github.herrherklotz.chameleon.x.backend" +
                        ".runtime.project.node.${groupName}.R${it1.key}Node")
            } catch (e: ClassNotFoundException) {
                LogElement.error(projectName, e.message!!)
                changingState.set(false)
                return false
            }

            val kotlinClass: KClass<*> = javaClass.kotlin

            it1.value.forEach {
                oGroups!![it1.key]!![it.key] = kotlinClass.constructors.first().call(*arrayOf(
                        "${it1.key}/${it.key}", it.value)) as RNode
            }
        }

        changingState.set(false)

        return true
    }
}