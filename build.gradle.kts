plugins {
    kotlin("jvm") version "2.1.0"
    application
}

group = "fr.nicolaslinard.po.toolbox"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Clikt - CLI framework
    implementation("com.github.ajalt.clikt:clikt:5.0.3")

    // Mordant - Terminal UI
    implementation("com.github.ajalt.mordant:mordant:3.0.2")

    // Kotlin standard library
    implementation(kotlin("stdlib"))

    // Testing
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("fr.nicolaslinard.po.toolbox.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
