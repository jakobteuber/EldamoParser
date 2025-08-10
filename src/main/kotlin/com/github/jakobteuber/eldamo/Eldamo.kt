package com.github.jakobteuber.eldamo

import com.github.jakobteuber.eldamo.data.Ref
import com.github.jakobteuber.eldamo.data.Word
import com.github.jakobteuber.eldamo.data.WordData
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.JAXBContext
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

/**
 * Entry point for the library. Load in the data with [Eldamo.fromFile] or [Eldamo.current].
 * You can access the data with [data] and [index].
 *
 * Every time you access this top level object, it can optionally check if the backing
 * XML file has changed and then reload the data.
 */
class Eldamo(
    private val queryLastModified: () -> Long,
    private val reload: () -> InputStream,
) {
    private val context = JAXBContext.newInstance(WordData::class.java)
    private val unmarshaller = context.createUnmarshaller()

    private var lastModified: Long = 0
    private var myData: WordData? = null
    private var myIndex = Index()

    /**
     * This object provides access to the data as it is laid out in the XML data model.
     * If you want to look up a word or reference, use [index] instead.
     */
    val data: WordData get() { maybeUpdate(); return myData!! }

    /**
     * This object provides several ways to find a word or a reference by some key.
     * If you want to traverse the data in the way it’s laid out in the XML, use [data].
     */
    val index: Index get() { maybeUpdate(); return myIndex }

    init { init() }

    private fun init() {
        val time = measureTime {
            val xml = reload()
            myIndex = Index()
            lastModified = queryLastModified()
            myData = unmarshaller.unmarshal(xml)!! as WordData
            data.topLevelWords.forEach { w: Word -> index.putWord(w) }
            data.visitAll(index)
        }
        logger.info {
            "Reloaded Eldamo ${data.version} " +
                    "(${index.allWords.size} words, ${index.allRefs.size} references) " +
                    "in ${time.inWholeMilliseconds} ms" }
    }

    /**
     * Objects of this class can be used to find words or references by various keys.
     */
    class Index internal constructor() {
        private val wordsByKey: MutableMap<Word.Key, Word> = HashMap()
        private val wordsById: MutableMap<String, Word> = HashMap()
        private val rules: MutableMap<Word.RuleKey, Word> = HashMap()
        private val refParent: MutableMap<String, Word> = HashMap()
        private val refsBySource: MutableMap<String, Ref> = HashMap()
        private val relatedRefs: MutableMap<String, MutableList<Ref>> = HashMap()

        internal fun putWord(w: Word) {
            wordsByKey[w.key] = w
            wordsById[w.pageId] = w
            w.ruleKeys.forEach { rules[it] = w }
            w.children.forEach(this::putWord)
            w.references.forEach({ r -> putRef(r, w) })
        }

        internal fun putRef(ref: Ref, parent: Word) {
            refsBySource[ref.source] = ref
            refParent[ref.source] = parent
            ref.relationships.forEach({ rel ->
                relatedRefs
                    .getOrPut(rel.source, ::mutableListOf)
                    .add(ref)
            })
        }

        /** All words in the data modle, both those at the top level and nested ones
         * These “words” also include grammar and text pages on Eldamo, so you might
         * want to filter for [Word.isNormalWord] */
        val allWords: Collection<Word> get() = wordsById.values
        /** All references in the data model. */
        val allRefs: Collection<Ref> get() = refsBySource.values

        private fun fail(vararg key: Any): Nothing =
            throw NoSuchElementException(
                "No element for key ${key.contentToString()}"
            )

        /** Find the parent [Word] of this reference */
        fun getParent(ref: Ref): Word = refParent[ref.source] ?: fail(ref)
        /** Find the word that is identified by the page id. The page id is the string
         * of numbers found in an Eldamo link `https://eldamo.org/content/words/word-$pageId.html` */
        fun findWord(pageId: String): Word = wordsById[pageId] ?: fail(pageId)
        fun findRule(key: Word.RuleKey): Word? = rules[key]
        /** Finds a word from its form and language, wrapped up as a [Word.Key] */
        fun findWord(key: Word.Key): Word = wordsByKey[key] ?: fail(key)
        /** Finds a reference by its [Ref.source] attribute, which is unique. */
        fun findRef(source: String): Ref = refsBySource[source] ?: fail(source)

        /** Collect all references that concern a word. Those are both the references
         * are listed inside the word itself, and the references that are inside
         * other Words but show a connection to this word (e.g. cognates, derivations, etc.) */
        fun collectRefs(w: Word): List<Ref> = buildList {
            addAll(w.references)
            for (r in w.references) {
                addAll(relatedRefs[r.source] ?: emptyList())
            }
        }
    }

    private fun maybeUpdate() {
        if (queryLastModified() > lastModified) {
            init()
        }
    }

    companion object {

        /** Download the most current data from https://eldamo.org/content/data-model/eldamo-data.xml */
        @JvmStatic
        fun current(): Eldamo = download(URI("https://eldamo.org/content/data-model/eldamo-data.xml"))

        /** Download an XML file from the specified URL. */
        @JvmStatic
        fun download(uri: URI) = Eldamo(
            queryLastModified = { uri.toURL().openConnection().lastModified },
            reload = { uri.toURL().openConnection().inputStream!! },
        )

        @JvmStatic
        fun local() = fromFile(Path.of("/home/jakobteuber/Projekte/eldamo/src/data/eldamo-data.xml"))

        /** Load the XML data from a file */
        @JvmStatic
        fun fromFile(file: Path) = Eldamo(
            queryLastModified = { Files.getLastModifiedTime(file).toMillis() },
            reload = { Files.newInputStream(file) },
        )
    }
}