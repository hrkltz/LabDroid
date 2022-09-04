package io.github.herrherklotz.chameleon.x.bridge

import io.github.herrherklotz.chameleon.Chameleon
import java.lang.Exception
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KClass


fun elementToClassPath(element: String): String {
    if (element.contains("//")) {
        return if (element.matches(Regex("^([^/]*)//\$"))) {
            element.replace(Regex("//(.*)//"), "")
                    .replace("/", ".")
                    .removeSuffix(".") + "Interface"
        } else {
            ".FileInterface2"
        }
    } else {
        var path = element.replace(Regex("(:[^/]*)"), "")
                .replace("/", ".")
                .removeSuffix(".")

        path += "Interface"

        return path
    }
}


fun elementToClassPath2(type: String, element: String): String {
    var path = "io.github.herrherklotz.chameleon.x."
    path += type
    path += element.replace(Regex("(:[^/]*)"), "")
            .replace("/", ".")

    return path.toLowerCase()
}


fun elementToFilePath(element: String): String {
    return element.replace(":", "/").replace("_", "/") +
            ".json"
}


fun elementToPath(element: String): String {
    return element.replace(":", "/").replace("_", "/")
}


object Bridge {
    fun route2(element: String, action: String, data: String?): Any {
        Chameleon.LOG_D("Thread: " + Thread.currentThread().name + " (Bridge.route2)")
        val path = elementToClassPath(element)

        val javaClass: Class<*>

        try {
            javaClass = Class.forName("io.github.herrherklotz.chameleon.x.bridge${path}")
        } catch (e: ClassNotFoundException) {
            return e.localizedMessage
        }

        val kotlinClass: KClass<*> = javaClass.kotlin
        val method: Method

        try {
            if (data == null) {
                method = javaClass.getMethod(action, String::class.java)

                return try {
                    method.invoke(kotlinClass.objectInstance, element) as Any
                } catch (e: Exception) {
                    e.localizedMessage!!
                }
            } else {
                method = javaClass.getMethod(action, String::class.java, String::class.java)

                return try {
                    method.invoke(kotlinClass.objectInstance, element, data) as Any
                } catch (e: InvocationTargetException) {
                    "InvocationTargetException"
                } catch (e: Exception) {
                    "Exception: " + e.message
                }
            }
        } catch (e: NoSuchMethodException) {
            return "The requested action isn't supported by this class."
        } catch (e: SecurityException) {
            return "The requested action isn't supported by this class."
        }
    }
}