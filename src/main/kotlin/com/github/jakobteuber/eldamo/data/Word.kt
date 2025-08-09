@file:Suppress("unused")
package com.github.jakobteuber.eldamo.data

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlTransient
import jakarta.xml.bind.annotation.XmlType
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter

private val logger = KotlinLogging.logger {}

class Word : DataNode() {
    @get:XmlAttribute(required = true, name = "l")
    @get:XmlJavaTypeAdapter(LanguageAdapter::class) var language = Language.Unknown
    @get:XmlAttribute(required = true, name = "v") var verbum = ""
    @get:XmlAttribute(required = true, name = "page-id") var pageId = ""
    @get:XmlAttribute(name = "speech")
    @get:XmlJavaTypeAdapter(PosAdapter::class) var partOfSpeech = mutableListOf<PartOfSpeech>()

    data class Key (val language: Language, val verbum: String) {
        override fun toString() = "{$language: $verbum}"
    }
    val key: Key get() = Key(language, verbum).also {
        if (partOfSpeech.isEmpty()) {
            println(pageId)
        }
    }

    override fun toString() = key.toString()

    @get:XmlTransient val eldamoLink get() = "https://eldamo.org/content/words/word-$pageId.html"
    @get:XmlTransient val githubLink get() = "https://pfstrack.github.io/eldamo/content/words/word-$pageId.html"

    fun isNormalWord() {
        val special = setOf(
            PartOfSpeech.Grammar, PartOfSpeech.Phrase, PartOfSpeech.Text,
            PartOfSpeech.PhoneticsOverview, PartOfSpeech.PhoneticGroup, PartOfSpeech.Phoneme,
            PartOfSpeech.PhoneticRule)
        partOfSpeech.all { it !in special }
    }

    @get:XmlAttribute(name = "mark")
    @get:XmlJavaTypeAdapter(MarkAdapter::class) var mark = mutableListOf<Mark>()
    @get:XmlAttribute(name = "gloss") var gloss: String? = null
    @get:XmlAttribute(name = "stem") var stem: String? = null
    @get:XmlAttribute(name = "tengwar") var spellingHint: String? = null
    @get:XmlAttribute(name = "cat") var semanticCategory: String? = null

    @get:XmlAttribute(name = "created") var createdBy: String? = null
    @get:XmlAttribute(name = "neo-gloss") var neoGloss: String? = null
    @get:XmlAttribute(name = "vetted") var vettedBy: String? = null
    @get:XmlAttribute(name = "neo-version") var neoVersion: String? = null
    val isNeo get() = neoVersion != null

    @get:XmlAttribute(name = "from") var phonRuleFrom: String? = null
    @get:XmlAttribute(name = "rule") var phonRuleTo: String? = null
    @get:XmlAttribute(name = "order") var phonRuleOrder: Int? = null

    @get:XmlAttribute(name = "orthography") var phonemOrthography: String? = null
    @get:XmlAttribute(name = "phon-col") var phonemColumn: Int? = null
    @get:XmlAttribute(name = "phon-row") var phonemRow: Int? = null

    @get:XmlElement(required = true, name = "word") var children = mutableListOf<Word>()
    @get:XmlElement(required = true, name = "ref") var references = mutableListOf<Ref>()

    @get:XmlTransient val relatedReferences get() = index.collectRefs(this)

    @get:XmlElement(required = true, name = "notes") var notes = mutableListOf<Html>()

    @XmlType(name = "wordRel")
    sealed class Rel : DataNode() {
        @get:XmlAttribute(required = true, name = "l") var language = Language.Unknown
        @get:XmlAttribute(required = true, name = "v") var verbum = ""
        val key get() = Key(language, verbum)
        val likedWord get() =  index.findWord(key)
    }

    @XmlType(name = "wordBefore")
    class Before : Rel()
    @get:XmlElement(required = true, name = "before")
    var before = mutableListOf<Before>()

    @XmlType(name = "wordCombine")
    class Combine : Rel()
    @get:XmlElement(required = true, name = "combine")
    var combine = mutableListOf<Combine>()

    class See : Rel()
    @get:XmlElement(required = true, name = "see")
    var see = mutableListOf<See>()
    @get:XmlElement(required = true, name = "see-also")
    var seeAlso = mutableListOf<See>()
    @get:XmlElement(required = true, name = "see-further")
    var seeFurther = mutableListOf<See>()
    @get:XmlElement(required = true, name = "see-notes")
    var seeNotes = mutableListOf<See>()
    val allSee get() = listOf(see, seeAlso, seeFurther, seeNotes).flatten()

    @XmlType(name = "wordWithMark")
    sealed class WithMark : Rel() {
        @get:XmlAttribute
        @get:XmlJavaTypeAdapter(MarkAdapter::class)
        var mark = mutableListOf<Mark>()
    }

    @XmlType(name = "wordCognate")
    class Cognate : WithMark()
    @get:XmlElement(required = true, name = "cognate")
    var cognates = mutableListOf<Cognate>()

    @XmlType(name = "wordDeriv")
    class Deriv : WithMark()
    @get:XmlElement(required = true, name = "deriv")
    var derivations = mutableListOf<Deriv>()

    @XmlType(name = "wordRelated")
    class Related : WithMark()
    @get:XmlElement(required = true, name = "related")
    var related = mutableListOf<Related>()

    @XmlType(name = "wordElement")
    class Element : WithMark() {
        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InflectionAdapter::class)
        var form = mutableListOf<Inflection>()

        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InfectionVariantAdapter::class)
        var variant = mutableListOf<InfectionVariant>()
    }

    @get:XmlElement(required = true, name = "element")
    var elements = mutableListOf<Element>()

    class WordClass : DataNode() {
        @get:XmlAttribute(required = true) var form = mutableListOf<WordClassForm>()
        @get:XmlAttribute var variant: WordClassVariant? = null
    }

    @get:XmlElement(required = true, name = "class")
    var wordClass = mutableListOf<WordClass>()

    class Deprecated : DataNode() {
        @get:XmlAttribute(name = "l") var language: Language? = null
        @get:XmlAttribute(name = "v") var verbum: String? = null
        @get:XmlTransient val key
            get() = if (language != null && verbum != null) {
                Key(language!!, verbum!!)
            } else { null }
     }

    @get:XmlElement(required = true, name = "deprecated")
    var deprecated = mutableListOf<Deprecated>()

    class Inflect : DataNode() {
        @get:XmlAttribute var source: String? = null
        @get:XmlAttribute(name = "v") var verbum: String? = null

        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InflectionAdapter::class)
        var form = mutableListOf<Inflection>()

        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InfectionVariantAdapter::class)
        var variant = mutableListOf<InfectionVariant>()
    }

    @get:XmlElement(required = true, name = "inflect")
    var inflections = mutableListOf<Inflect>()

    class RuleKey {
        @get:XmlAttribute(name = "l") var language = ""
        @get:XmlAttribute(name = "rule") var rule = ""
        @get:XmlAttribute(name = "from") var from = ""
    }

    @get:XmlElement(required = true, name = "rule")
    var ruleKeys = mutableListOf<RuleKey>()

    class InflectTable : DataNode() {
        @get:XmlAttribute(name = "exclude") var exclude: String? = ""
        @get:XmlAttribute(required = true) @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
        var form = mutableListOf<InflectTableForm>()
        @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFromAdapter::class)
        var from: InflectTableFrom? = null
        @get:XmlAttribute var hide: Boolean? = null
        @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableKeyAdapter::class)
        var key: InflectTableKey? = null
        @get:XmlAttribute(name = "l") var language: String? = ""
        @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
        var omit = mutableListOf<InflectTableForm>()
        @get:XmlAttribute(name = "show-element-of") var showElementOf: Boolean? = null
        @get:XmlAttribute(name = "show-form") var showForm: Boolean? = null
        @get:XmlAttribute(name = "show-glosses") var showGlosses: Boolean? = null
        @get:XmlAttribute(name = "show-variants") var showVariants: Boolean? = null
        @get:XmlAttribute @get:XmlJavaTypeAdapter(PosAdapter::class)
        var speech: PartOfSpeech? = null

        class Form {
            @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
            var exclude = mutableListOf<InflectTableForm>()
            @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
            var exclude2 = mutableListOf<InflectTableForm>()
            val allExcludes get() = listOf(exclude, exclude2).flatten()
            @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
            var form = mutableListOf<InflectTableForm>()
        }

        @get:XmlElement(required = true, name = "form") var forms = mutableListOf<Form>()
    }

    @get:XmlElement(name = "inflect-table") var inflectTable = mutableListOf<InflectTable>()
}
