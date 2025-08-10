package com.github.jakobteuber.eldamo

import java.util.Locale.getDefault

fun main() {
    val eldamo = Eldamo.local()
    eldamo.index.allRefs
        .flatMap { it.derivatives }
        .flatMap { it.ruleExamples }
        .map { it.rule }
        .distinctBy { it?.pageId }
        .onEach { println(it) }
}

fun generateEnum(name: String, string: String) {
    val regex = "@XmlEnumValue\\(\"([^\"]*)\"\\)".toRegex()
    val entries = regex.findAll(string).map { it.groupValues[1] }

    for (entry in entries) {
        println("${entry.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }}.")
    }
    println("}\n")
}