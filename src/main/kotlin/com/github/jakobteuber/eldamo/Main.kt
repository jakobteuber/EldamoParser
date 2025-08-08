package com.github.jakobteuber.eldamo

fun main() {
    Eldamo.local()
}

fun generateEnum(name: String, string: String) {
    val regex = "<xs:enumeration value=\"([^\"]*)\"/>".toRegex()
    val entries = regex.findAll(string).map { it.groupValues[1] }

    println("class ${name}Adapter : EnumAdapter<$name>($name::class, $name.Unknown)\n")
    println("enum class $name {")
    for (entry in entries) {
        val enumName = entry
            .replace("-", " ")
            .replace("?", "Unknown")
            .replace("1st", "first")
            .replace("2nd", "second")
            .replace("3rd", "third")
            .split(" ")
            .joinToString("") { it.replaceFirstChar(Char::uppercase) }

        println("""    @XmlEnumValue("$entry") $enumName,""")
    }
    println("}\n")
}