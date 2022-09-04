package io.github.herrherklotz.chameleon.x.extensions

interface IEditable {
    fun edit(elementId: String, property: String, value: Map<String, Any?>): Boolean
}