package com.github.jakobteuber.eldamo.data

import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement

class Source {
    @get:XmlAttribute(required = true) var name = ""
    @get:XmlAttribute(required = true) var prefix = ""
    @get:XmlAttribute var type: SourceType? = null
    @get:XmlElement(required = true) var cite = ""
    @get:XmlElement(required = true) var notes = ""
}

class SemanticCategories {
    @get:XmlElement(required = true, name = "cat-group")
    var groups = mutableListOf<Group>()

    class Group {
        @get:XmlAttribute(required = true) var id = ""
        @get:XmlAttribute(required = true) var label = ""
        @get:XmlAttribute(required = true, name = "num") var number = ""
        @get:XmlElement(required = true, name = "cat") var categories = mutableListOf<Category>()
    }

    class Category {
        @get:XmlAttribute(required = true) var id = ""
        @get:XmlAttribute(required = true) var label = ""
        @get:XmlAttribute(required = true, name = "num") var number = ""
    }
}

class LanguageGroup {
    @get:XmlAttribute var id: String? = null

    @get:XmlElement(required = true, name = "language")
    var languages = mutableListOf<LanguageInfo>()

    @get:XmlElement(required = true, name = "language-cat")
    var subcategories = mutableListOf<LanguageGroup>()
}

class LanguageInfo {
    @get:XmlAttribute(required = true) var id = ""
    @get:XmlAttribute(required = true) var name = ""
    @get:XmlElement(required = true) var deprecations = ""
    @get:XmlAttribute(required = true) var grammar = ""
    @get:XmlAttribute(required = true) var names = ""
    @get:XmlAttribute(required = true) var neologisms = ""
    @get:XmlAttribute(required = true) var notes = ""
    @get:XmlAttribute(required = true) var phonetics = ""
    @get:XmlAttribute(required = true) var phrases = ""
    @get:XmlAttribute(required = true) var roots = ""
    @get:XmlAttribute(required = true) var words = ""
    @get:XmlAttribute(required = true) var vocabulary = ""
    @get:XmlAttribute(required = true, name = "language")
    var childLanguages = mutableListOf<LanguageInfo>()
}