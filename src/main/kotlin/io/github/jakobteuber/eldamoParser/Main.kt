package io.github.jakobteuber.eldamoParser

import java.util.Locale.getDefault

fun main() {
    val eldamo = Eldamo.local()

}

fun generateEnum(name: String, string: String) {
    val regex = "@XmlEnumValue\\(\"([^\"]*)\"\\)".toRegex()
    val entries = regex.findAll(string).map { it.groupValues[1] }

    for (entry in entries) {
        println("${entry.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }}.")
    }
    println("}\n")
}