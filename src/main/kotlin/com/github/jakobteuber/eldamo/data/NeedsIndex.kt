package com.github.jakobteuber.eldamo.data

import com.github.jakobteuber.eldamo.Eldamo
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlTransient
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

private val logger = KotlinLogging.logger {}

class AssignOnce<T> {
    private var field: T? = null
    private var assigned = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!assigned) {
            throw IllegalStateException("Property '${property.name}' not yet assigned.")
        }
        return field!!
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (assigned) {
            throw IllegalStateException("Property '${property.name}' can only be assigned once.")
        }
        field = value
        assigned = true
    }
}

abstract class DataNode {
    @get:XmlTransient internal var index by AssignOnce<Eldamo.Index>()

    internal fun visitAll(index: Eldamo.Index) {
        visit(this, index, null)
    }
}

private val propertyCache: MutableMap<KClass<*>, List<KProperty1<Any, *>>> =
    ConcurrentHashMap()

@Suppress("UNCHECKED_CAST")
private fun getCachedProperties(klass: KClass<*>): List<KProperty1<Any, *>> {
    return propertyCache.computeIfAbsent(klass) {
        klass.memberProperties
            .filter { it.isAccessible }
            .map { it as KProperty1<Any, *> }
    }
}

private fun visit(obj: Any?, index: Eldamo.Index, parent: DataNode?) {
    if (obj !is DataNode) return
    obj.index = index

    val klass = obj::class
    for (member in getCachedProperties(klass)) {
        try {
            val value = member.get(obj)
            when (value) {
                is Collection<*> -> value.forEach { visit(it, index, obj) }
                else -> visit(value, index, obj)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failure to inject index into $obj" }
        }
    }
}

