package com.github.jakobteuber.eldamo.data

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlValue

private val logger = KotlinLogging.logger {}

class Html {
    @get:XmlValue var raw: String = ""

    fun convert(converter: Converter): String = raw
        .replace("""<a\s+l="([^"]*)"\s*v="([^"]*)"\s*/>""".toRegex()) {
            val l = Language.fromXml(it.groupValues[1])
            val v = it.groupValues[2]
            converter.wordLink(Word.Key(l, v))
        }
        .replace("""<a\s+l="([^"]*)"\s*v="([^"]*)"\s*>([^<]*)</a>""".toRegex()) {
            val l = Language.fromXml(it.groupValues[1])
            val v = it.groupValues[2]
            val content = it.groupValues[3]
            converter.wordLink(Word.Key(l, v), content)
        }
        .replace("""<a\s+ref="([^"]*)"\s*/>""".toRegex()) {
            val id = it.groupValues[1]
            converter.refLink(id)
        }
        .replace("""<a\s+ref="([^"]*)"\s*>([^<]*)</a>""".toRegex()) {
            val id = it.groupValues[1]
            val content = it.groupValues[2]
            converter.refLink(id, content)
        }
}

interface Converter {
    fun wordLink(key: Word.Key, content: CharSequence): CharSequence
    fun wordLink(key: Word.Key): CharSequence = wordLink(key, key.verbum)
    fun refLink(id: String, content: CharSequence): CharSequence
    fun refLink(id: String) = refLink(id, id)
}

