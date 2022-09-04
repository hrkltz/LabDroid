package io.github.herrherklotz.chameleon.x.extensions

interface IInstanceable {
    fun create(path: String, content: String = ""): Boolean
    fun delete(path: String): Boolean
    fun rename(path: String, name: String): Boolean
}