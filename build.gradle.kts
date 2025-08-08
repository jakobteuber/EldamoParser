plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "com.github.jakobteuber"
version = "1.0-SNAPSHOT"

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