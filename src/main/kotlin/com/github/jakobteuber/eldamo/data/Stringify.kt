package com.github.jakobteuber.eldamo.data

import java.io.BufferedReader
import java.io.InputStreamReader

internal open class Stringify<T: Enum<T>>(name: String) {
    private val names: List<String> = javaClass.classLoader.getResourceAsStream(name).use {
        BufferedReader(InputStreamReader(it!!)).lines().toList()
    }

    fun toString(thisRef: Enum<T>): String = names[thisRef.ordinal]
}