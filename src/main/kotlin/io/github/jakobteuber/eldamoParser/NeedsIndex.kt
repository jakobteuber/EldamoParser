package io.github.jakobteuber.eldamoParser

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlTransient
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

private val logger = KotlinLogging.logger {}

class AssignOnce<T> {
    private var field: T? = null
    private var assigned = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!assigned) {
            val className = if (thisRef != null) thisRef::class.qualifiedName else ""
            throw IllegalStateException("Property $className.${property.name} not yet assigned.")
        }
        return field!!
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (assigned) {
            val className = if (thisRef != null) thisRef::class.qualifiedName else ""
            throw IllegalStateException("Property $className.${property.name} can only be assigned once.")
        }
        field = value
        assigned = true
    }
}

abstract class NeedsIndex {
    @get:XmlTransient internal var index by AssignOnce<Eldamo.Index>()

    internal fun visitAll(index: Eldamo.Index) {
        visit(this, index, HashSet())
    }
}

private val propertyCache: MutableMap<KClass<*>, List<KProperty1<Any, *>>> =
    ConcurrentHashMap()

@Suppress("UNCHECKED_CAST")
private fun getCachedProperties(klass: KClass<*>): List<KProperty1<Any, *>> {
    return propertyCache.computeIfAbsent(klass) {
        klass.memberProperties
            .map { it as KProperty1<Any, *> }
            .filter {
                it.findAnnotation<XmlTransient>() == null &&
                        it.getter.findAnnotation<XmlTransient>() == null
            }
    }
}

private fun visit(obj: Any?, index: Eldamo.Index, known: HashSet<NeedsIndex>) {
    if (obj == null || obj !is NeedsIndex) { return }
    if (obj in known) return
    known += obj
    obj.index = index

    val klass = obj::class
    for (member in getCachedProperties(klass)) {
        try {
            when (val value = member.get(obj)) {
                is Collection<*> -> value.forEach { visit(it, index, known) }
                is NeedsIndex -> visit(value, index, known)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failure to inject index into $obj" }
        }
    }
}

