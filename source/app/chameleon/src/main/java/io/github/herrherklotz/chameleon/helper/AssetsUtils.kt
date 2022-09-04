package io.github.herrherklotz.chameleon.helper

import io.github.herrherklotz.chameleon.MainService.Companion.context

fun load(pPath: String): String {
    return context!!.assets.open(pPath).bufferedReader().use{
        it.readText()
    }
}