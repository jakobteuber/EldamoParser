package io.github.jakobteuber.eldamoParser

import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement

/**
 * The root node of the XML data modle.
 */
@XmlRootElement(name = "word-data")
class WordData : NeedsIndex() {
    /** The Eldamo version of the loaded data */
    @get:XmlAttribute(required = true) var version = ""
    /** The words declared at top level. Earlier variants of words are nested
     * within later variants, so this list does not contain all words.
     * If you want to get all words, regardless of nesting, use
     * [Eldamo.Index.allWords]*/
    @get:XmlElement(name = "word") var topLevelWords = mutableListOf<Word>()

    /** All sources from which the data on Eldamo is compiled */
    @get:XmlElement(name = "source") var sources = mutableListOf<Source>()

    /** The semantic categories, according to which words are classified */
    @get:XmlElement(required = true, name = "cats")
    var semanticCategories by AssignOnce<SemanticCategories>()
}