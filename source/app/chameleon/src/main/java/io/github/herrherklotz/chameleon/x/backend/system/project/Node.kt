package io.github.herrherklotz.chameleon.x.backend.system.project


import android.graphics.Point
import com.fasterxml.jackson.annotation.*


open class Node @JsonCreator constructor(var typeName: String)  {
    var label: String? = null
    lateinit var position: Point

    var inputs: ArrayList<Array<String>?> = arrayListOf()

    var outputs: ArrayList<Array<String>?> = arrayListOf()


    @JsonSetter("inCount")
    fun inCount(i: Int) {
        var counter = i

        if (counter > 0) {
            inputs = ArrayList()

            while (counter-- > 0) {
                inputs.add(null)
            }
        }
    }


    @JsonSetter("outCount")
    fun outCount(i: Int) {
        var counter = i

        if (counter > 0) {
            outputs = ArrayList()

            while (counter-- > 0) {
                outputs.add(null)
            }
        }
    }
}