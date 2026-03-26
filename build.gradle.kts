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
val jfxClassifier: String = run {
    val explicit = (findProperty("jfxPlatform") as? String) ?: System.getenv("JFX_PLATFORM")
    if (explicit != null) {
        // Map short platform names to full JavaFX classifiers
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        when (explicit) {
            "mac" -> if (arch.contains("aarch64") || arch.contains("arm64")) "mac-aarch64" else "mac"
            else -> explicit
        }
    } else {
        // Auto-detect from current system
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        when {
            os.contains("win") -> "win"
            os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64")) -> "mac-aarch64"
            os.contains("mac") -> "mac"
            arch.contains("aarch64") || arch.contains("arm64") -> "linux-aarch64"
            else -> "linux"
        }
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

// jpackage: create native executable from fat JAR
tasks.register<Exec>("jpackage") {
    dependsOn(tasks.named("shadowJar"))
    group = "distribution"
    description = "Creates a native application bundle using jpackage"

    val jarFile = layout.buildDirectory.file("libs/po-toolbox-${project.version}-${jfxClassifier}.jar")
    val inputDir = layout.buildDirectory.dir("jpackage-input")
    val outputDir = layout.buildDirectory.dir("package")
    val os = System.getProperty("os.name").lowercase()
    val iconPath = when {
        os.contains("win") -> "src/main/resources/icons/icon.ico"
        os.contains("mac") -> "src/main/resources/icons/icon.png"
        else -> "src/main/resources/icons/icon.png"
    }

    doFirst {
        // jpackage fails if destination exists — clean both the output dir and app subdirectory
        File(outputDir.get().asFile, "PO-Toolbox").deleteRecursively()
        File(outputDir.get().asFile, "PO-Toolbox.app").deleteRecursively()
        inputDir.get().asFile.deleteRecursively()
        inputDir.get().asFile.mkdirs()
        jarFile.get().asFile.copyTo(
            inputDir.get().file(jarFile.get().asFile.name).asFile, overwrite = true
        )
    }

    commandLine(
        "jpackage",
        "--input", inputDir.get().asFile.absolutePath,
        "--main-jar", jarFile.get().asFile.name,
        "--name", "PO-Toolbox",
        "--app-version", project.version.toString(),
        "--type", "app-image",
        "--dest", outputDir.get().asFile.absolutePath,
        "--icon", iconPath,
        "--java-options", "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--java-options", "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--java-options", "--enable-native-access=ALL-UNNAMED"
    )
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
