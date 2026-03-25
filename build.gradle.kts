plugins {
    kotlin("jvm") version "2.3.20"
    application
    jacoco // Code coverage reporting
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

group = "fr.nicolaslinard.po.toolbox"
version = "1.0.0"

repositories {
    mavenCentral()
}

val jfxVersion = "24.0.1"

// JavaFX classifier: set via -PjfxPlatform=win or env JFX_PLATFORM, default auto-detect
val jfxClassifier: String = (findProperty("jfxPlatform") as? String)
    ?: System.getenv("JFX_PLATFORM")
    ?: run {
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        when {
            os.contains("win") -> "win"
            os.contains("mac") && arch.contains("aarch64") -> "mac-aarch64"
            os.contains("mac") -> "mac"
            arch.contains("aarch64") || arch.contains("arm64") -> "linux-aarch64"
            else -> "linux"
        }
    }

dependencies {
    // Kotlin standard library
    implementation(kotlin("stdlib"))

    // JSON support
    implementation("org.json:json:20250517")

    // JavaFX (platform-specific, set by jfxClassifier)
    implementation("org.openjfx:javafx-base:$jfxVersion:$jfxClassifier")
    implementation("org.openjfx:javafx-graphics:$jfxVersion:$jfxClassifier")
    implementation("org.openjfx:javafx-controls:$jfxVersion:$jfxClassifier")
    implementation("org.openjfx:javafx-fxml:$jfxVersion:$jfxClassifier")

    // TornadoFX - Desktop UI framework
    implementation("no.tornado:tornadofx:1.7.20")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.3") // Mocking framework
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2") // JUnit 5
}

application {
    mainClass.set("fr.nicolaslinard.po.toolbox.desktop.POToolboxAppKt")
    applicationDefaultJvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}

// Configure JaCoCo for code coverage
jacoco {
    toolVersion = "0.8.13"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// Exclude desktop UI classes from coverage (untestable without TestFX)
val jacocoExcludes = listOf(
    "fr/nicolaslinard/po/toolbox/desktop/**"
)

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExcludes) }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExcludes) }
        })
    )
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% coverage on non-UI code
            }
        }
    }
}

// Shadow JAR: fat JAR with all dependencies, named by platform
tasks.shadowJar {
    archiveBaseName.set("po-toolbox")
    archiveClassifier.set(jfxClassifier)
    mergeServiceFiles()
    manifest {
        attributes(
            "Main-Class" to "fr.nicolaslinard.po.toolbox.desktop.POToolboxAppKt",
            "Add-Opens" to "java.base/java.lang java.base/java.lang.reflect"
        )
    }
}
