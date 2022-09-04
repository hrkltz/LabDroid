package io.github.herrherklotz.chameleon.x.extensions

interface IObject {
    fun load(path: String): Any
    fun save(path: String, value: String): Boolean
}