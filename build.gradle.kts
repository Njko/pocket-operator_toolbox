plugins {
    kotlin("jvm") version "2.1.0"
    application
    jacoco // Code coverage reporting
    id("com.github.johnrengelman.shadow") version "8.1.1" // Fat JAR creation
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

    // JSON support
    implementation("org.json:json:20231013")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8") // Mocking framework
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1") // JUnit 5
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

// Configure JaCoCo for code coverage
jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% coverage minimum
            }
        }
    }
}

// Configure Shadow plugin for executable JAR
tasks.shadowJar {
    archiveBaseName.set("po-toolbox")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")
    manifest {
        attributes["Main-Class"] = "fr.nicolaslinard.po.toolbox.MainKt"
    }
    mergeServiceFiles()
}
