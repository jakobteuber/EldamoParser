package com.github.jakobteuber.eldamo

import com.github.jakobteuber.eldamo.data.Ref
import com.github.jakobteuber.eldamo.data.Word
import com.github.jakobteuber.eldamo.data.WordData
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.xml.bind.JAXBContext
import java.io.File
import java.io.InputStream
import java.net.URI
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

class Eldamo(
    private val queryLastModified: () -> Long,
    private val reload: () -> InputStream,
) {
    private val context = JAXBContext.newInstance(WordData::class.java)
    private val unmarshaller = context.createUnmarshaller()

    private var lastModified: Long = 0
    private var myData: WordData? = null
    private var myIndex = Index()
    val data: WordData get() { maybeUpdate(); return myData!! }
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
                    "(${index.words.size} words, ${index.refs.size} references) " +
                    "in ${time.inWholeMilliseconds} ms" }
    }

    class Index {
        private val myWords: MutableMap<Word.Key, Word> = HashMap()
        private val myWordById: MutableMap<String, Word> = HashMap()
        private val myRefParent: MutableMap<String, Word> = HashMap()
        private val myRefs: MutableMap<String, Ref> = HashMap()
        private val myRelatedRefs: MutableMap<String, MutableList<Ref>> = HashMap()

        internal fun putWord(w: Word) {
            myWords[w.key] = w
            myWordById[w.pageId] = w
            w.children.forEach(this::putWord)
            w.references.forEach({ r -> putRef(r, w) })
        }

        internal fun putRef(ref: Ref, parent: Word) {
            myRefs[ref.source] = ref
            myRefParent[ref.source] = parent
            ref.relationships.forEach({ rel ->
                myRelatedRefs
                    .getOrPut(rel.source, ::mutableListOf)
                    .add(ref)
            })
        }

        val words: Collection<Word> get() = myWordById.values
        val refs: Collection<Ref> get() = myRefs.values

        fun getParent(ref: Ref): Word = getParent(ref.source)
        fun getParent(ref: String): Word = myRefParent[ref]!!
        fun findWord(pageId: String): Word = myWordById[pageId]!!
        fun findWord(key: Word.Key): Word = myWords[key]!!

        fun collectRefs(w: Word): List<Ref> = buildList {
            addAll(w.references)
            for (r in w.references) {
                addAll(myRelatedRefs[r.source] ?: emptyList())
            }
        }
    }



    private fun maybeUpdate() {
        if (queryLastModified() > lastModified) {
            init()
        }
    }



    companion object {
        fun current(): Eldamo = download(URI("https://eldamo.org/content/data-model/eldamo-data.xml"))

        fun download(uri: URI) = Eldamo(
            queryLastModified = { uri.toURL().openConnection().lastModified },
            reload = { uri.toURL().openConnection().inputStream!! },
        )

        fun local() = fromFile(File("/home/jakobteuber/Projekte/eldamo/src/data/eldamo-data.xml"))

        fun fromFile(file: File) = Eldamo(
            queryLastModified = { file.lastModified() },
            reload = { file.inputStream() },
        )
    }
}