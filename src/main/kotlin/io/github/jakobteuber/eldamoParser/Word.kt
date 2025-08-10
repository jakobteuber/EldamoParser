@file:Suppress("unused")

package io.github.jakobteuber.eldamoParser

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlTransient
import jakarta.xml.bind.annotation.XmlType
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import java.net.URLEncoder

private val logger = KotlinLogging.logger {}

/**
 * A word in the data modle.
 *
 * Calling this a “word” is a bit of a misnomer. A “word” is every Eldamo page that has
 * a link of the form `https://eldamo.org/content/words/word-$pageId.html`, so it includes
 * all entries for words, but also grammar and text pages. You can use [isNormalWord]
 * to filter for actual words.
 */
class Word : NeedsIndex() {
    /** The language the word belongs to */
    @get:XmlAttribute(required = true, name = "l")
    @get:XmlJavaTypeAdapter(LanguageAdapter::class) var language = Language.Unknown
    /** The exact form of the word. */
    @get:XmlAttribute(required = true, name = "v") var verbum = ""
    /** The page id. This is used for the [eldamoLink]. */
    @get:XmlAttribute(required = true, name = "page-id") var pageId = ""
    /** The part of speech. Some words can belong in multiple categories, e.g. Sindarin
     * *morn* can be both the adjective “dark” and the noun “night”. */
    @get:XmlAttribute(name = "speech")
    @get:XmlJavaTypeAdapter(PosAdapter::class)
    var partOfSpeech: MutableList<PartOfSpeech> = mutableListOf()

    /** A unique key for a word, consisting of its language and its word form */
    data class Key (val language: Language, val verbum: String) {
        override fun toString() = "$language $verbum"
    }
    /** A unique key for a word, consisting of its language and its word form */
    @get:XmlTransient val key: Key get() = Key(language, verbum)

    override fun toString() = buildString {
        append(language.abbreviation).append(' ')
        mark.forEach { append(it.abbreviation) }
        append(verbum)
        append(" (")
        partOfSpeech.joinToString { it.abbreviation }.also { append(it) }
        if (stem != null) append(", ").append(stem)
        if (spellingHint != null) append(", ").append(spellingHint)
        append(") ")
        if (isNeo && neoGloss != null) append('“').append(neoGloss).append("”")
        else if (gloss != null) append('“').append(gloss).append("”")
        append("\t[").append(eldamoLink).append(']')
    }

    /** A link to the word’s page on `eldamo.org` */
    @get:XmlTransient val eldamoLink get() = "https://eldamo.org/content/words/word-$pageId.html"
    /** A link to the word’s page on `pfstrack.github.io` */
    @get:XmlTransient val githubLink get() = "https://pfstrack.github.io/eldamo/content/words/word-$pageId.html"
    /** A link to the word’s page on `elfdict.com` */
    @get:XmlTransient val elfdictLink get() = URLEncoder.encode("https://www.elfdict.com/w/$verbum", Charsets.UTF_8)

    /** Check if this is a normal entry for a word, not a page for a phrase or
     * a page analysing some grammar or phonetics feature */
    fun isNormalWord() {
        val special = setOf(
            PartOfSpeech.Grammar, PartOfSpeech.Phrase, PartOfSpeech.Text,
            PartOfSpeech.PhoneticsOverview, PartOfSpeech.PhoneticGroup, PartOfSpeech.Phoneme,
            PartOfSpeech.PhoneticRule)
        partOfSpeech.all { it !in special }
    }

    /** Marks indicating the reliability of the entry. For more information, see
     * https://eldamo.org/general/terminology-and-notations.html */
    @get:XmlAttribute(name = "mark")
    @get:XmlJavaTypeAdapter(MarkAdapter::class) var mark: MutableList<Mark> = mutableListOf()
    /** The gloss, i.e. translation of the word. */
    @get:XmlAttribute(name = "gloss") var gloss: String? = null
    /** The stem of the word if it is different from the citation form [verbum] */
    @get:XmlAttribute(name = "stem") var stem: String? = null
    /** Hints for spelling the word in Tengwar or mutating the word.
     *
     * **Examples:**
     * - Q. *[serinde](https://eldamo.org/content/words/word-178290009.html)*
     *    “needlewoman” is spelled with *þúle*, so it has a spelling hint `"þ"`
     * - S. *[dôr](https://eldamo.org/content/words/word-3950740861.html)*
     *    “land” soft mutates to *nôr* instead of *dhôr*, so it has a spelling hint `"nd-"` */
    @get:XmlAttribute(name = "tengwar") var spellingHint: String? = null
    /** The id of this words semantic category */
    @get:XmlAttribute(name = "cat") var semanticCategory: String? = null

    /** If the word is a neologism ([isNeo]), this indicates its creator */
    @get:XmlAttribute(name = "created") var createdBy: String? = null
    /** If the word is a neologism ([isNeo]), this indicates its creator */
    @get:XmlAttribute(name = "neo-gloss") var neoGloss: String? = null
    /** If the word is a neologism ([isNeo]), this indicates its creator */
    @get:XmlAttribute(name = "vetted") var vettedBy: String? = null
    /** If the word is a neologism ([isNeo]), this indicates the version of
     * Eldamo when it was includes */
    @get:XmlAttribute(name = "neo-version") var neoVersion: String? = null
    /** Checks if this word is a [neologism](https://eldamo.org/general/motivations-and-methodology.html) */
    @get:XmlTransient val isNeo get() = neoVersion != null

    /** If this page describes a phonetic rule ([partOfSpeech] == [PartOfSpeech.PhoneticRule]),
     * this indicates the sound that this rule is acting upon (e.g. for *þ > s* would indicate *þ*). */
    @get:XmlAttribute(name = "from") var phonRuleFrom: String? = null
    /** If this page describes a phonetic rule([partOfSpeech] == [PartOfSpeech.PhoneticRule]),
     * this indicates the sound that this rule is acting upon (e.g. for *þ > s* would indicate *s*). */
    @get:XmlAttribute(name = "rule") var phonRuleTo: String? = null
    /** If this page describes a phonetic rule ([partOfSpeech] == [PartOfSpeech.PhoneticRule]),
     * this indicates the order of this rule relative to others in the language. */
    @get:XmlAttribute(name = "order") var phonRuleOrder: Int? = null

    /** If this page describes a phoneme ([partOfSpeech] == [PartOfSpeech.Phoneme]),
     * this indicates the usual romanisation, e.g. in Sindarin, *ð* is usally spelled *dh*. */
    @get:XmlAttribute(name = "orthography") var phonemOrthography: String? = null
    /** If this page describes a phoneme ([partOfSpeech] == [PartOfSpeech.Phoneme]),
     * this indicates the column in which the phonem should be displayed in a chart. */
    @get:XmlAttribute(name = "phon-col") var phonemColumn: Int? = null
    /** If this page describes a phoneme ([partOfSpeech] == [PartOfSpeech.Phoneme]),
     * this indicates the row in which the phonem should be displayed in a chart. */
    @get:XmlAttribute(name = "phon-row") var phonemRow: Int? = null

    /** The words defined inside of this word. Those words represent conceptual
     * predecessors of the word. */
    @get:XmlElement(required = true, name = "word")
    var children: MutableList<Word> = mutableListOf()
    /** The references to sources attesting this word. See also [relatedReferences] */
    @get:XmlElement(required = true, name = "ref")
    var references: MutableList<Ref> = mutableListOf()

    /** All references that concern a word. Those are both the references
     * are listed inside the word itself, and the references that are inside
     * other Words but show a connection to this word (e.g. cognates, derivations, etc.) */
    @get:XmlTransient val relatedReferences get() = index.collectRefs(this)

    /** A common type for relationships between words */
    @XmlType(name = "wordRel")
    sealed class Rel : NeedsIndex() {
        /** The language of the word linked to */
        @get:XmlAttribute(required = true, name = "l") var language = Language.Unknown
        /** The form of the word linked to */
        @get:XmlAttribute(required = true, name = "v") var verbum = ""
        /** A key to the word linked to */
        @get:XmlTransient val linkedKey get() = Key(language, verbum)
        /** The word to which this relationship links */
        @get:XmlTransient val likedWord get() =  index.findWord(linkedKey)
    }

    /** If this page describes a phonetic rule ([partOfSpeech] == [PartOfSpeech.PhoneticRule]),
     * this is a list of other rules that must come before it.  */
    @XmlType(name = "wordBefore")
    class Before : Rel() {
        class OrderExample: NeedsIndex() {
            @get:XmlAttribute var source: String = ""
            @get:XmlAttribute var verbum: String = ""
            @get:XmlTransient val linkedRef get() = index.findRef(source)
        }
        @get:XmlElement(required = true, name = "order-example")
        var orderExamples: MutableList<OrderExample> = mutableListOf()
    }
    /** If this page describes a phonetic rule ([partOfSpeech] == [PartOfSpeech.PhoneticRule]),
     * this is a list of other rules that must come before it.  */
    @get:XmlElement(required = true, name = "before")
    var before: MutableList<Before> = mutableListOf()

    /** This links to a similar word with which it should be combined when generating entries */
    @XmlType(name = "wordCombine")
    class Combine : Rel()
    /** This links to a similar word with which it should be combined when generating entries */
    @get:XmlElement(required = true, name = "combine")
    var combine: MutableList<Combine> = mutableListOf()

    /** This links to another word providing more context */
    class See : Rel()
    /** This links to another word providing more context for this word */
    @get:XmlElement(required = true, name = "see")
    var see: MutableList<See> = mutableListOf()
    /** This links to another word providing more context */
    @get:XmlElement(required = true, name = "see-also")
    var seeAlso: MutableList<See> = mutableListOf()
    /** This links to another word providing more context */
    @get:XmlElement(required = true, name = "see-further")
    var seeFurther: MutableList<See> = mutableListOf()
    /** This links to another word providing more context */
    @get:XmlElement(required = true, name = "see-notes")
    var seeNotes: MutableList<See> = mutableListOf()
    /** Eldamo splits links to other words into [see], [seeAlso], [seeFurther], [seeNotes].
     * This provides a view of all of these combined. */
    @get:XmlTransient val allSee: List<See> get() = listOf(see, seeAlso, seeFurther, seeNotes).flatten()

    /** A common type for relationships between words that include a relaibility marker */
    @XmlType(name = "wordWithMark")
    sealed class WithMark : Rel() {
        /** [Marks](https://eldamo.org/general/terminology-and-notations.html)
         * indicating the reliability of the entry. */
        @get:XmlAttribute
        @get:XmlJavaTypeAdapter(MarkAdapter::class)
        var mark: MutableList<Mark> = mutableListOf()
    }

    /** Links to a cognate of a given word */
    @XmlType(name = "wordCognate")
    class Cognate : WithMark()
    /** Links to a cognate of this word */
    @get:XmlElement(required = true, name = "cognate")
    var cognates: MutableList<Cognate> = mutableListOf()

    /** Links to a word, from which a given word has been derived */
    @XmlType(name = "wordDeriv")
    class Deriv : WithMark()
    /** Links to words, from which this word has been derived */
    @get:XmlElement(required = true, name = "deriv")
    var derivations: MutableList<Deriv> = mutableListOf()

    /** Links to a word in some general relationship with a given word, e.g.
     * words with related meanings. */
    @XmlType(name = "wordRelated")
    class Related : WithMark()
    /** Links to words in some general relationship with this word, e.g.
     * words with related meanings. */
    @get:XmlElement(required = true, name = "related")
    var related: MutableList<Related> = mutableListOf()

    /** Links to a word that is an element of a give word. */
    @XmlType(name = "wordElement")
    class Element : WithMark() {
        /** The grammatical form of the element */
        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InflectionAdapter::class)
        var form: MutableList<Inflection> = mutableListOf()
        /** The variant of the grammatical form of the element */
        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InfectionVariantAdapter::class)
        var variant: MutableList<InfectionVariant> = mutableListOf()
    }
    /** Links to word that are an element of this word. */
    @get:XmlElement(required = true, name = "element")
    var elements: MutableList<Element> = mutableListOf()

    /** The word class of a given word */
    class WordClass : NeedsIndex() {
        /** The word class of a given word */
        @get:XmlAttribute(required = true) var form: MutableList<WordClassForm> = mutableListOf()
        /** The word class variant of a given word */
        @get:XmlAttribute var variant: WordClassVariant? = null
    }
    /** The word class of a given word */
    @get:XmlElement(required = true, name = "class")
    var wordClass: WordClass? = null

    data class Deprecated(
        @get:XmlAttribute(name = "l") var language: Language? = null,
        @get:XmlAttribute(name = "v") var verbum: String? = null,
    ) {
        @get:XmlTransient val key
            get() = if (language != null && verbum != null) {
                Key(language!!, verbum!!)
            } else { null }
     }

    @get:XmlElement(required = true, name = "deprecated")
    var deprecated: MutableList<Deprecated> = mutableListOf()

    class Inflect : NeedsIndex() {
        @get:XmlAttribute var source: String? = null
        @get:XmlAttribute(name = "v") var verbum: String? = null
        @get:XmlTransient val linkedRef: Ref?
            get() = source?.let { index.findRef(it) }

        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InflectionAdapter::class)
        var form: MutableList<Inflection> = mutableListOf()

        @get:XmlAttribute(required = true)
        @get:XmlJavaTypeAdapter(InfectionVariantAdapter::class)
        var variant: MutableList<InfectionVariant> = mutableListOf()
    }

    @get:XmlElement(required = true, name = "inflect")
    var inflections: MutableList<Inflect> = mutableListOf()

    data class RuleKey(
        @get:XmlJavaTypeAdapter(LanguageAdapter::class)
        @get:XmlAttribute(name = "l") var language: Language,
        @get:XmlAttribute(name = "rule") var rule: String,
        @get:XmlAttribute(name = "from") var from: String,
    ) {
        constructor() : this(Language.Unknown, "", "")
    }

    @get:XmlElement(required = true, name = "rule")
    var ruleKeys: MutableList<RuleKey> = mutableListOf()

    class InflectTable {
        @get:XmlAttribute(name = "exclude") var exclude: String? = ""
        @get:XmlAttribute(required = true) @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
        var form: MutableList<InflectTableForm> = mutableListOf()
        @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFromAdapter::class)
        var from: InflectTableFrom? = null
        @get:XmlAttribute var hide: Boolean? = null
        @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableKeyAdapter::class)
        var key: InflectTableKey? = null
        @get:XmlAttribute(name = "l") var language: String? = ""
        @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
        var omit: MutableList<InflectTableForm> = mutableListOf()
        @get:XmlAttribute(name = "show-element-of") var showElementOf: Boolean? = null
        @get:XmlAttribute(name = "show-form") var showForm: Boolean? = null
        @get:XmlAttribute(name = "show-glosses") var showGlosses: Boolean? = null
        @get:XmlAttribute(name = "show-variants") var showVariants: Boolean? = null
        @get:XmlAttribute @get:XmlJavaTypeAdapter(PosAdapter::class)
        var speech: PartOfSpeech? = null

        class Form {
            @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
            var exclude: MutableList<InflectTableForm> = mutableListOf()
            @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
            var exclude2: MutableList<InflectTableForm> = mutableListOf()
            @get:XmlTransient val allExcludes: List<InflectTableForm>
                get() = listOf(exclude, exclude2).flatten()
            @get:XmlAttribute @get:XmlJavaTypeAdapter(InflectTableFormAdapter::class)
            var form: MutableList<InflectTableForm> = mutableListOf()
        }

        @get:XmlElement(required = true, name = "form")
        var forms: MutableList<Form> = mutableListOf()
    }

    @get:XmlElement(name = "inflect-table")
    var inflectTable: MutableList<InflectTable> = mutableListOf()

    /** Notes on the word */
    @get:XmlElement(required = true, name = "notes")
    var notes: MutableList<Html> = mutableListOf()
}
