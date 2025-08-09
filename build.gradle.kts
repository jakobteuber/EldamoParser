import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.dokka") version "2.0.0"
    application
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "com.github.jakobteuber"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.10")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.1")
    implementation("org.openjdk.jol:jol-core:0.17")

    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.0")
}

application { mainClass = "com.github.jakobteuber.eldamo.MainKt" }

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral(host = SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "eldamoParser", version.toString())

    pom {
        name = "Eldamo parser"
        description = "A utility library for working with the Elvish Data Model."
        inceptionYear = "2025"
        url = "https://github.com/jakobteuber/EldamoParser"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "jakob.teuber"
                name = "Jakob Teuber"
                url = "https://github.com/jakobteuber/"
            }
        }
        scm {
            url = "https://github.com/jakobteuber/EldamoParser"
            connection = "scm:git:git://github.com/jakobteuber/EldamoParser"
            developerConnection = "scm:git:ssh://git@github.com/jakobteuber/EldamoParser"
        }
    }
}