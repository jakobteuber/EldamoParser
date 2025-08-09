package com.github.jakobteuber.eldamo.data

import com.github.jakobteuber.eldamo.Eldamo
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlTransient
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter

private val logger = KotlinLogging.logger {}

class Ref : DataNode() {
    @get:XmlAttribute(required = true) var source = ""
    val publication get() = source.substringBefore('/')
    val page get() = source.substringAfter('/').substringBefore('.')
    val location get() = source.substringAfter('.')

    @get:XmlAttribute( name = "l")
    @get:XmlJavaTypeAdapter(LanguageAdapter::class) var language: Language? = null
    @get:XmlAttribute(required = true, name = "v") var verbum = ""

    override fun toString() = "Ref{ $source : $verbum }"

    @get:XmlAttribute(name = "mark")
    @get:XmlJavaTypeAdapter(MarkAdapter::class) var mark = mutableListOf<Mark>()

    @get:XmlTransient val parent get() = index.getParent(this)

    sealed class Rel : DataNode() {
        @get:XmlAttribute(required = true) var source = ""
        @get:XmlAttribute(required = true, name = "v") var verbum = ""
        @get:XmlElement var note: String? = ""
        @get:XmlTransient val likedRef get() = index.findRef(source)
    }

    class Example : Rel() {
        @get:XmlAttribute var type: String = ""
    }

    @get:XmlElement(required = true, name = "example")
    var examples = mutableListOf<Example>()

    class Change : Rel() {
        @get:XmlAttribute(name = "i1") var intermediate: String? = null
    }

    @get:XmlElement(required = true, name = "change")
    var changes = mutableListOf<Change>()

    class Correction : Rel()

    @get:XmlElement(required = true, name = "correction")
    var corrections = mutableListOf<Correction>()

    sealed class WithMark : Rel() {
        @get:XmlAttribute
        @get:XmlJavaTypeAdapter(MarkAdapter::class)
        var mark = mutableListOf<Mark>()
    }

    class Cognate : WithMark()

    @get:XmlElement(required = true, name = "cognate")
    var cognates = mutableListOf<Cognate>()

    class Deriv : WithMark() {
        @get:XmlAttribute(name = "i1") var i1: String? = null
        @get:XmlAttribute(name = "i2") var i2: String? = null
        @get:XmlAttribute(name = "i3") var i3: String? = null
        val intermediate get() = listOfNotNull(i1, i2, i3)

        class OrderStart {
            @get:XmlAttribute(name = "l")
            @get:XmlJavaTypeAdapter(LanguageAdapter::class)
            var language: Language? = null
            @get:XmlAttribute(required = true)
            var stage = ""
        }

        @get:XmlElement(name = "rule-start")
        var ruleStart: OrderStart? = null

        class RuleExample {
            @get:XmlAttribute(required = true, name = "l")
            @get:XmlJavaTypeAdapter(LanguageAdapter::class)
            var language: Language = Language.Unknown
            @get:XmlAttribute(required = true) var rule = ""
            @get:XmlAttribute(required = true) var from = ""
            @get:XmlAttribute(required = true) var stage = ""
        }

        @get:XmlElement(required = true, name = "rule-example")
        var ruleExamples = mutableListOf<RuleExample>()
    }

    @get:XmlElement(required = true, name = "deriv")
    var derivatives = mutableListOf<Deriv>()

    class Related : WithMark()

    @get:XmlElement(required = true, name = "related")
    var generalRelations = mutableListOf<Related>()

    sealed class WithFormInfo : WithMark() {
        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InflectionAdapter::class)
        var form = mutableListOf<Inflection>()

        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InfectionVariantAdapter::class)
        var variant = mutableListOf<InfectionVariant>()
    }

    class Element : WithFormInfo()

    @get:XmlElement(required = true, name = "element")
    var elements = mutableListOf<Element>()

    class Inflected : WithFormInfo()

    @get:XmlElement(required = true, name = "inflect")
    var inflections = mutableListOf<Inflected>()

    val relationships get(): List<Rel> =
        listOf(examples, changes, corrections, cognates, derivatives,
            generalRelations, elements, inflections)
            .flatten()
}