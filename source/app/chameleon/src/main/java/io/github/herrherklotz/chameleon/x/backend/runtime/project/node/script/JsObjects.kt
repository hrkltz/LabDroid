package io.github.herrherklotz.chameleon.x.backend.runtime.project.node.script

import com.eclipsesource.v8.V8
import com.eclipsesource.v8.utils.MemoryManager
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.websocket.endpoint.AppEndpoint
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem.projectName
import io.github.herrherklotz.chameleon.x.utils.Storage
import java.lang.Exception


class JsObjects(var mScriptName: String, private val mRuntime: V8) {
    private val mMemoryManager: MemoryManager


    init {
        mRuntime.registerJavaMethod(this, "log", "\$log",
                arrayOf<Class<*>>(String::class.java))
        mRuntime.registerJavaMethod(this, "sleep", "\$sleep",
                arrayOf<Class<*>?>(Int::class.javaPrimitiveType))
        mRuntime.registerJavaMethod(this, "now", "\$now",
                null) // TODO time.now(); time.utc(); ...
        this.mRuntime.registerJavaMethod(this, "append", "\$append",
                arrayOf<Class<*>>(String::class.java, String::class.java))
        this.mRuntime.registerJavaMethod(this, "check", "\$check",
                arrayOf<Class<*>>(String::class.java))
        this.mRuntime.registerJavaMethod(this, "save", "\$save",
                arrayOf<Class<*>>(String::class.java, String::class.java))
        this.mRuntime.registerJavaMethod(this, "load", "\$load",
                arrayOf<Class<*>>(String::class.java))
        this.mRuntime.registerJavaMethod(this, "delete",
                "\$delete", arrayOf<Class<*>>(String::class.java))

        mMemoryManager = MemoryManager(mRuntime)
        mMemoryManager.release()
    }


    fun close() {
        mRuntime.terminateExecution()
        mRuntime.release()
    }


    fun now(): Double {
        return System.nanoTime().toDouble()
    }


    fun log(pMessage: String?) {
        val message = pMessage ?: ""

        LogElement.info("${projectName}/${mScriptName}", message)
    }


    @Throws(InterruptedException::class)
    fun sleep(pTime: Int) {
        Thread.sleep(pTime.toLong())
    }


    // File
    fun append(path: String, content: String) {
        Storage.save("/Project/$projectName/File/$path", content, true)
    }


    fun  check(path: String): Boolean {
        // TODO Move FileInterface regex to a centrail place and use it here?
        return Storage.exists("/Project/$projectName/File/$path")
    }


    fun delete(path: String) {
        Storage.deleteFile("/Project/$projectName/File/$path")
        AppEndpoint.broadcastProject(projectName!!)
    }


    fun  load(path: String): String? {
        // TODO Move FileInterface regex to a centrail place and use it here?
        val result = Storage.readFile("/Project/$projectName/File/$path")

        if (!result.first)
            throw Exception()

        return result.second
    }


    fun save(path: String, content: String) {
        Storage.save("/Project/$projectName/File/$path", content, false)
        AppEndpoint.broadcastProject(projectName!!)
    }
}