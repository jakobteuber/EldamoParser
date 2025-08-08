package com.github.jakobteuber.eldamo.data

import com.github.jakobteuber.eldamo.Eldamo
import jakarta.xml.bind.annotation.*

@XmlRootElement(name = "word-data")
class WordData : DataNode() {
    @get:XmlAttribute(required = true) var version = ""
    @get:XmlElement(name = "word") var topLevelWords = mutableListOf<Word>()
    @get:XmlElement(name = "source") var sources = mutableListOf<Source>()

    @get:XmlElement(required = true, name = "cats")
    var semanticCategories by AssignOnce<SemanticCategories>()
}