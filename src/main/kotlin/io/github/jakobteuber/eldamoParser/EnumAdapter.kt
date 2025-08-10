package io.github.jakobteuber.eldamoParser

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlEnumValue
import jakarta.xml.bind.annotation.adapters.XmlAdapter
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

open class EnumAdapter<E : Enum<E>>(
    private val enumType: KClass<E>,
    private val default: E,
    private val chainable: Boolean = false,
) : XmlAdapter<String, E>() {

    private fun getXmlName(e: E): String {
        val field = enumType.java.getField(e.name)
        val annot = field.getAnnotation(XmlEnumValue::class.java)
        return annot?.value ?: e.name
    }

    private val strToE: Map<String, E> = enumType.java.enumConstants
        .associateBy(this::getXmlName)
    private val eToStr: Map<E, String> = enumType.java.enumConstants
        .associateWith(this::getXmlName)

    override fun unmarshal(str: String?): E? {
        if (str == null) return null
        val v = strToE[str]
        return v
            ?: if (!chainable) {
                logger.error { "Unknown enum value `$str` for ${enumType.simpleName} " +
                        "in ${this@EnumAdapter::class.qualifiedName}" }
                default
            } else {
                null
            }
    }

    override fun marshal(e: E?): String? {
        if (e == null) return null
        return eToStr[e]!!
    }

}