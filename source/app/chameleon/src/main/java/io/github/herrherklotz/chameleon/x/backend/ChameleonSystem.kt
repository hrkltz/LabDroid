package io.github.herrherklotz.chameleon.x.backend

import com.fasterxml.jackson.annotation.JsonSetter
import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.x.backend.system.Components
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.eventStart
import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.backend.runtime.RProject
import io.github.herrherklotz.chameleon.x.utils.Assets
import io.github.herrherklotz.chameleon.x.utils.Storage
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


object ChameleonSystem {
    val changingState = AtomicBoolean(false)

    var projectName: String? = null
        set(_projectName) {
            if (changingState.get())
                return

            changingState.set(true)
            AppEndpoint.broadcastSystem()

            Chameleon.LOG_D("Thread: " + Thread.currentThread().name + " (ChameleonSystem.projectName)")

            if (_projectName != null) {
                if (ChameleonSystem2.rProject != null) {
                    ChameleonSystem2.rProject!!.off()
                    ChameleonSystem2.rProject = null
                    field = null
                }

                field = _projectName
                ChameleonSystem2.rProject = RProject(field!!)

                if (!ChameleonSystem2.rProject!!.on()) {
                    ChameleonSystem2.rProject!!.off()
                    ChameleonSystem2.rProject = null
                    field = null
                } else
                    eventStart.onNext(field!!)
            } else {
                if (ChameleonSystem2.rProject != null) {
                    ChameleonSystem2.rProject!!.off()
                    ChameleonSystem2.rProject = null
                }

                field = null
            }

            changingState.set(false)
            AppEndpoint.broadcastSystem()
        }

    val nodes = mapOf("scripts" to 99)

    val components: Map<String, Map<String, Any?>>
        get() {
            val components: MutableMap<String, Map<String, Any?>> = HashMap()

            Components.forEach { key: String, value: IComponent ->
                val map: MutableMap<String, MutableSet<String>> = HashMap()

                if (value.mChannels.isNotEmpty())
                    map["channels"] = value.mChannels.keys

                if (value.mBroadcastChannels.isNotEmpty())
                    map["broadcasts"] = value.mBroadcastChannels.keys

                if (value.mPoolChannels.isNotEmpty())
                    map["polls"] = value.mPoolChannels.keys

                components[key] = map
            }

            return components
    }

    val projects: List<String>
        get() = Storage.listFolders("/Project")

    val examples: List<String>
        get() = Assets.listFolders("examples")


    @JsonSetter("copyExample")
    fun copyProjectFromAssets(data: Array<String>) {
        Assets.copyProjectFromAssets(data[0], "examples/${data[1]}")
    }
}