plugins {
    kotlin("jvm") version "2.1.0"
    application
    jacoco // Code coverage reporting
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
