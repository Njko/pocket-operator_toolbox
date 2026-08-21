# PO-14 Sub Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users create, save, view, and play back "classic" bass patterns for the Pocket Operator PO-14 (Sub) in the desktop app, alongside the existing PO-12 (Rhythm) support.

**Architecture:** The PO-14 is monophonic and pitched (one bass sound plays across 16 steps, each step holds at most one white-key note), which is structurally different from the PO-12's per-voice boolean step grid. Rather than force-fitting the PO-14 into `PO12Pattern`, this plan adds a parallel `PO14Pattern` model and a small `AnyPattern` marker interface so the existing repository/list/playback plumbing can hold either pattern type. New, focused classes (`PO14Step`, `NewMelodicPatternDialog`, etc.) sit next to the PO-12 equivalents rather than genericizing them — this codebase's existing precedent is device-specific types (`PO12DrumVoice` is hardcoded everywhere), not one generic abstraction.

**Tech Stack:** Kotlin, JavaFX 24 + TornadoFX (desktop UI), `javax.sound.midi` (playback/export), Kotlin Test + JUnit Platform.

**Spec:** No separate spec doc — requirements come directly from the user's feature request (transcribed below) plus the real PO-14 manual, fetched and cross-checked at https://teenage.engineering/guides/po-14/en. Key facts confirmed from the manual: sounds are picked by holding Sound + a key (15 bass timbres + a drum-machine mode); a step's pitch is set by "hold a lit step and turn A to set note pitch"; the only interval effect is #15 "half note up", triggered live via Style/FX + key 15 during playback or punched in live — there is no user-adjustable scale, confirming the user's framing that steps store natural (white-key) notes and sharps are a live, timing-sensitive performance action.

## Global Constraints

- The app is desktop-only — the CLI (`Main.kt`, `commands/*`) was removed; entry point is `fr.nicolaslinard.po.toolbox.desktop.POToolboxAppKt`. Do not resurrect CLI code.
- TDD applies to every model/IO/repository class below (RED-GREEN-REFACTOR, Kotlin Test). JavaFX UI classes (`NewMelodicPatternDialog`, `PatternDetailView`, `MainView`) have **no unit tests** in this codebase (`NewPatternDialog.kt`, current `PatternDetailView.kt`, `MainView.kt` have none either) — verify those manually via the `/run` workflow instead.
- `PatternChain` / multi-bar chaining stays PO-12-only. PO-14 patterns are single-bar (16 steps) for this plan — no chain support.
- `GeneratePatternDialog` (the existing template-gallery screen) stays PO-12-only for this plan. PO-14 "classic" templates are offered via a dropdown inside the new PO-14 creation dialog instead — do not touch `GeneratePatternDialog.kt`/`GeneratePatternDialogModel.kt`.
- `PatternValidator`, `JsonExporter`, `CsvExporter`, `VoiceCopyUtility` are dead code — not called anywhere under `desktop/` (verified by grep). Do not touch them; wiring unused code into a new feature is out of scope.
- `PODevice.PO_14`'s existing 16 `POVoice` entries (Sine Bass, Square Bass, ... Thump) are reused as-is as the selectable bass-sound presets — do not modify `PODevice.kt`.

---

### Task 1: `AnyPattern` marker interface

**Files:**
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/Pattern.kt`
- Test: `src/test/kotlin/fr/nicolaslinard/po/toolbox/models/AnyPatternTest.kt`

**Interfaces:**
- Produces: `interface AnyPattern { val metadata: PatternMetadata; val number: Int }`, implemented by `PO12Pattern` (this task) and `PO14Pattern` (Task 2). Later tasks (repository, MIDI export, UI) consume this to hold either pattern type.

- [ ] **Step 1: Write the failing test**

```kotlin
package fr.nicolaslinard.po.toolbox.models

import fr.nicolaslinard.po.toolbox.TestFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AnyPatternTest {

    @Test
    fun `PO12Pattern is an AnyPattern exposing metadata and number`() {
        val pattern = TestFixtures.createSimplePattern(name = "Any Test", patternNumber = 4)
        val any: AnyPattern = assertIs<AnyPattern>(pattern)
        assertEquals("Any Test", any.metadata.name)
        assertEquals(4, any.number)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.models.AnyPatternTest"`
Expected: FAIL — compile error, `AnyPattern` is unresolved and `PO12Pattern` does not implement it.

- [ ] **Step 3: Add the interface and implement it on `PO12Pattern`**

In `Pattern.kt`, add the interface above the existing class and make `PO12Pattern` implement it:

```kotlin
package fr.nicolaslinard.po.toolbox.models

/**
 * Common surface shared by every Pocket Operator pattern type, so the
 * repository/UI layers can hold PO-12 drum patterns and PO-14 melodic
 * patterns side by side without knowing which one they have.
 */
sealed interface AnyPattern {
    val metadata: PatternMetadata
    val number: Int
}

/**
 * Represents a pattern specific to the Pocket Operator PO-12 (Rhythm).
 * PO-12 has 16 patterns, each with 16 steps.
 */
data class PO12Pattern(
    val voices: Map<PO12DrumVoice, List<Int>>,  // PO12DrumVoice -> active steps (1-16)
    override val metadata: PatternMetadata,
    override val number: Int = 1                 // PO-12 pattern number (1-16)
) : AnyPattern {
    init {
        require(number in 1..16) { "Pattern number must be between 1 and 16" }
        voices.values.forEach { steps ->
            require(steps.all { it in 1..16 }) { "All steps must be between 1 and 16" }
        }
    }

    fun getActiveSteps(voice: PO12DrumVoice): List<Int> = voices[voice] ?: emptyList()

    fun hasVoice(voice: PO12DrumVoice): Boolean = voices.containsKey(voice)
}
```

Note: `AnyPattern` is declared `sealed` now so that a second implementation must live in the same file/module and every `when` over it stays exhaustive — Task 2 adds `PO14Pattern` in a different file (`PO14Pattern.kt`) in the same package, which Kotlin allows for sealed interfaces (same module + package is enough, no need for the same file).

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.models.AnyPatternTest"`
Expected: PASS

- [ ] **Step 5: Run the full test suite to confirm no regressions**

Run: `./gradlew test`
Expected: All existing tests still PASS (this change is additive — `PO12Pattern`'s public API is unchanged, only `override` keywords were added).

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/models/Pattern.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/models/AnyPatternTest.kt
git commit -m "Add AnyPattern interface so PO12Pattern and future device patterns share a common surface"
```

---

### Task 2: `Pitch`, `PO14Step`, `PO14Pattern` models

**Files:**
- Create: `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/PO14Pattern.kt`
- Test: `src/test/kotlin/fr/nicolaslinard/po/toolbox/models/PO14PatternTest.kt`

**Interfaces:**
- Consumes: `AnyPattern` (Task 1), `PatternMetadata`, `POVoice`/`PODevice.PO_14` (existing, from `PODevice.kt`).
- Produces: `enum class Pitch(val letter: String, val semitonesFromC: Int)` with `Pitch.fromLetter(String): Pitch?`; `data class PO14Step(pitch: Pitch, octave: Int = 3, halfToneUp: Boolean = false)` with `val midiNote: Int` and `val noteText: String`; `data class PO14Pattern(steps: Map<Int, PO14Step>, sound: POVoice, metadata: PatternMetadata, number: Int = 1) : AnyPattern` with `fun getNote(step: Int): PO14Step?`. Later tasks (Markdown I/O, MIDI export, templates, UI) all consume these exact names.

- [ ] **Step 1: Write the failing tests**

```kotlin
package fr.nicolaslinard.po.toolbox.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PO14PatternTest {

    private fun metadata(name: String = "Test Bass") = PatternMetadata(name = name, deviceModel = "PO-14")

    // --- Pitch ---

    @Test
    fun `Pitch fromLetter resolves natural notes case-insensitively`() {
        assertEquals(Pitch.D, Pitch.fromLetter("d"))
        assertEquals(Pitch.D, Pitch.fromLetter("D"))
    }

    @Test
    fun `Pitch fromLetter returns null for unknown letters`() {
        assertNull(Pitch.fromLetter("H"))
    }

    // --- PO14Step ---

    @Test
    fun `PO14Step computes MIDI note for a natural note at octave 3`() {
        // C4 = MIDI 60 (middle C), so C3 = 48
        val step = PO14Step(Pitch.C, octave = 3)
        assertEquals(48, step.midiNote)
    }

    @Test
    fun `PO14Step half tone up raises the MIDI note by one semitone`() {
        val natural = PO14Step(Pitch.C, octave = 3, halfToneUp = false)
        val sharp = PO14Step(Pitch.C, octave = 3, halfToneUp = true)
        assertEquals(natural.midiNote + 1, sharp.midiNote)
    }

    @Test
    fun `PO14Step rejects octave outside 0 to 8`() {
        assertFailsWith<IllegalArgumentException> { PO14Step(Pitch.C, octave = 9) }
        assertFailsWith<IllegalArgumentException> { PO14Step(Pitch.C, octave = -1) }
    }

    @Test
    fun `PO14Step noteText renders letter, sharp and octave`() {
        assertEquals("C3", PO14Step(Pitch.C, octave = 3).noteText)
        assertEquals("D#2", PO14Step(Pitch.D, octave = 2, halfToneUp = true).noteText)
    }

    // --- PO14Pattern ---

    @Test
    fun `PO14Pattern is an AnyPattern`() {
        val pattern = PO14Pattern(
            steps = mapOf(1 to PO14Step(Pitch.C, 3)),
            sound = PODevice.PO_14.voices.first(),
            metadata = metadata(),
            number = 2
        )
        val any: AnyPattern = pattern
        assertEquals(metadata().name, any.metadata.name)
        assertEquals(2, any.number)
    }

    @Test
    fun `getNote returns the programmed step or null for a rest`() {
        val pattern = PO14Pattern(
            steps = mapOf(1 to PO14Step(Pitch.C, 3)),
            sound = PODevice.PO_14.voices.first(),
            metadata = metadata()
        )
        assertEquals(Pitch.C, pattern.getNote(1)?.pitch)
        assertNull(pattern.getNote(2))
    }

    @Test
    fun `rejects pattern number outside 1 to 16`() {
        assertFailsWith<IllegalArgumentException> {
            PO14Pattern(steps = emptyMap(), sound = PODevice.PO_14.voices.first(), metadata = metadata(), number = 17)
        }
    }

    @Test
    fun `rejects step keys outside 1 to 16`() {
        assertFailsWith<IllegalArgumentException> {
            PO14Pattern(
                steps = mapOf(17 to PO14Step(Pitch.C, 3)),
                sound = PODevice.PO_14.voices.first(),
                metadata = metadata()
            )
        }
    }

    @Test
    fun `defaults to an empty pattern with no notes`() {
        val pattern = PO14Pattern(steps = emptyMap(), sound = PODevice.PO_14.voices.first(), metadata = metadata())
        assertTrue(pattern.steps.isEmpty())
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.models.PO14PatternTest"`
Expected: FAIL — compile error, none of `Pitch`/`PO14Step`/`PO14Pattern` exist yet.

- [ ] **Step 3: Create the models**

```kotlin
package fr.nicolaslinard.po.toolbox.models

/**
 * A white-key note (A-G). The PO-14 Sub only stores natural notes; sharps
 * are produced live via the "half note up" effect — see [PO14Step].
 */
enum class Pitch(val letter: String, val semitonesFromC: Int) {
    C("C", 0), D("D", 2), E("E", 4), F("F", 5), G("G", 7), A("A", 9), B("B", 11);

    companion object {
        fun fromLetter(letter: String): Pitch? =
            entries.find { it.letter.equals(letter, ignoreCase = true) }
    }
}

/**
 * One programmed note on the PO-14 Sub sequencer.
 * @param halfToneUp whether effect 15 ("half note up") is triggered live on
 *   this step, raising the note a semitone to reach a black key. On the
 *   real device this is a timing-sensitive live performance action, not a
 *   value you can just dial in with knob A — see the programming
 *   instructions generated by [fr.nicolaslinard.po.toolbox.io.MarkdownWriter].
 */
data class PO14Step(
    val pitch: Pitch,
    val octave: Int = 3,
    val halfToneUp: Boolean = false
) {
    init {
        require(octave in 0..8) { "Octave must be between 0 and 8" }
    }

    /** MIDI note number (C4 = 60), honouring the half-tone-up effect. */
    val midiNote: Int get() = (octave + 1) * 12 + pitch.semitonesFromC + if (halfToneUp) 1 else 0

    /** Compact text form used in markdown files and the UI, e.g. "D#3" or "C4". */
    val noteText: String get() = "${pitch.letter}${if (halfToneUp) "#" else ""}$octave"
}

/**
 * Represents a pattern for the Pocket Operator PO-14 (Sub).
 * Unlike the PO-12's per-voice boolean grid, the PO-14 is monophonic: one
 * bass sound plays across the 16 steps, and each step carries at most one
 * note (or is a rest).
 */
data class PO14Pattern(
    val steps: Map<Int, PO14Step>, // step (1-16) -> note; absent = rest
    val sound: POVoice,
    override val metadata: PatternMetadata,
    override val number: Int = 1
) : AnyPattern {
    init {
        require(number in 1..16) { "Pattern number must be between 1 and 16" }
        require(steps.keys.all { it in 1..16 }) { "All steps must be between 1 and 16" }
    }

    fun getNote(step: Int): PO14Step? = steps[step]
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.models.PO14PatternTest"`
Expected: PASS (all 10 tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/models/PO14Pattern.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/models/PO14PatternTest.kt
git commit -m "Add PO14Pattern model: monophonic white-key steps with a live half-tone-up flag"
```

---

### Task 3: `MarkdownWriter` support for PO-14 patterns

**Files:**
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownWriter.kt`
- Create: `src/test/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownWriterTest.kt`

**Interfaces:**
- Consumes: `PO14Pattern`, `PO14Step`, `AnyPattern` (Task 1-2), existing `PO12Pattern`.
- Produces: `fun write(pattern: PO14Pattern, outputDir: File): File` and `fun write(pattern: AnyPattern, outputDir: File): File` on `MarkdownWriter`. Task 4 (`MarkdownParser`) reads back exactly the frontmatter/section format written here.

- [ ] **Step 1: Write the failing tests**

```kotlin
package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.Pitch
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MarkdownWriterTest {

    private val testOutputDir = File("build/test-output/markdown-writer")
    private val writer = MarkdownWriter()

    @BeforeTest
    fun setup() { testOutputDir.mkdirs() }

    @AfterTest
    fun cleanup() { testOutputDir.listFiles()?.forEach { it.delete() } }

    private fun samplePo14Pattern(): PO14Pattern = PO14Pattern(
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2),
            5 to PO14Step(Pitch.D, 2, halfToneUp = true),
            9 to PO14Step(Pitch.G, 2)
        ),
        sound = PODevice.PO_14.getVoiceByShortName("acid")!!,
        metadata = PatternMetadata(name = "Acid Test Line", bpm = 130, deviceModel = "PO-14"),
        number = 1
    )

    @Test
    fun `writes device field in frontmatter for a PO12 pattern`() {
        val pattern = fr.nicolaslinard.po.toolbox.TestFixtures.createSimplePattern()
        val file = writer.write(pattern, testOutputDir)
        assertTrue(file.readText().contains("device: \"PO-12\""))
    }

    @Test
    fun `writes device field in frontmatter for a PO14 pattern`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        assertTrue(file.readText().contains("device: \"PO-14\""))
    }

    @Test
    fun `writes the selected sound name and number`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        assertTrue(file.readText().contains("Acid Bass (Sound 6)"))
    }

    @Test
    fun `writes a note token per active step and a rest marker per empty step`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        val content = file.readText()
        assertTrue(content.contains("C2"))
        assertTrue(content.contains("D#2"))
        assertTrue(content.contains("G2"))
        assertTrue(content.contains("."))
    }

    @Test
    fun `lists sharp steps in the programming instructions`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        assertTrue(file.readText().contains("half note up"))
    }

    @Test
    fun `dispatches AnyPattern write to the correct overload`() {
        val po14: AnyPattern = samplePo14Pattern()
        val file = writer.write(po14, testOutputDir)
        assertTrue(file.readText().contains("device: \"PO-14\""))
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.io.MarkdownWriterTest"`
Expected: FAIL — `device:` line missing, no `write(PO14Pattern, File)` overload exists.

- [ ] **Step 3: Update `MarkdownWriter.kt`**

Add the `device` frontmatter line (shared helper, so both pattern types benefit) — edit the existing `appendFrontMatter`:

```kotlin
    private fun StringBuilder.appendFrontMatter(metadata: PatternMetadata, patternNumber: Int) {
        appendLine("---")
        appendLine("name: \"${metadata.name}\"")

        metadata.description?.let {
            appendLine("description: \"${it.replace("\"", "\\\"")}\"")
        }

        metadata.bpm?.let {
            appendLine("bpm: $it")
        }

        if (metadata.genre.isNotEmpty()) {
            appendLine("genre: [${metadata.genre.joinToString(", ") { "\"$it\"" }}]")
        }

        metadata.difficulty?.let {
            appendLine("difficulty: ${it.displayName}")
        }

        metadata.sourceAttribution?.let {
            appendLine("source: \"${it.replace("\"", "\\\"")}\"")
        }

        metadata.author?.let {
            appendLine("author: \"$it\"")
        }

        appendLine("date: ${metadata.dateCreated}")
        appendLine("device: \"${metadata.deviceModel}\"")
        appendLine("pattern_numbers: [$patternNumber]")
        appendLine("chain_sequence: null")
        appendLine("---")
    }
```

Add the imports, the PO-14 write path, and the `AnyPattern` dispatch overload at the top/bottom of the class:

```kotlin
import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import java.io.File
```

```kotlin
    /**
     * Writes a PO-14 pattern to a markdown file.
     * Returns the file that was created.
     */
    fun write(pattern: PO14Pattern, outputDir: File): File {
        val fileName = generateFileName(pattern.metadata.name)
        val file = File(outputDir, fileName)

        val content = buildString {
            appendFrontMatter(pattern.metadata, pattern.number)
            appendLine()
            appendTitle(pattern.metadata.name)
            appendDescription(pattern.metadata.description)
            appendLine()
            appendMelodicPatternSection(pattern)
            appendLine()
            appendMelodicProgrammingInstructions(pattern)
            appendNotes(pattern.metadata)
        }

        file.parentFile?.mkdirs()
        file.writeText(content)

        return file
    }

    /**
     * Writes any supported pattern type, dispatching to the matching overload.
     */
    fun write(pattern: AnyPattern, outputDir: File): File = when (pattern) {
        is PO12Pattern -> write(pattern, outputDir)
        is PO14Pattern -> write(pattern, outputDir)
    }

    private fun StringBuilder.appendMelodicPatternSection(pattern: PO14Pattern) {
        appendLine("## Pattern ${pattern.number}")
        appendLine()
        appendLine("**Sound:** ${pattern.sound.displayName} (Sound ${pattern.sound.number})")
        appendLine()
        appendLine("```")
        append("Step: ")
        for (i in 1..16) append(String.format("%4d", i))
        appendLine()
        append("Note: ")
        for (i in 1..16) {
            val text = pattern.getNote(i)?.noteText ?: "."
            append(String.format("%4s", text))
        }
        appendLine()
        appendLine("```")
        appendLine()
    }

    private fun StringBuilder.appendMelodicProgrammingInstructions(pattern: PO14Pattern) {
        appendLine("## PO-14 Programming Instructions")
        appendLine()
        appendLine("1. Select Pattern ${pattern.number} on your PO-14")
        appendLine("2. Hold Sound and press ${pattern.sound.number} to select ${pattern.sound.displayName}")
        appendLine("3. Press Write to enter recording mode")

        val activeSteps = pattern.steps.entries.sortedBy { it.key }
        if (activeSteps.isNotEmpty()) {
            appendLine("4. For each active step, hold the step and turn knob A to set its pitch:")
            activeSteps.forEach { (step, note) ->
                appendLine("   - Step $step: ${note.pitch.letter}${note.octave}")
            }
        }
        appendLine("5. Press Write again to exit recording mode")

        val sharpSteps = activeSteps.filter { it.value.halfToneUp }
        if (sharpSteps.isNotEmpty()) {
            appendLine("6. These steps need a black key (half note up). This can't be dialed in with knob A: " +
                "during playback, hold Style/FX and tap 15 (\"half note up\") exactly on each of these steps, " +
                "or punch it in live by holding Write and tapping the step at the right moment:")
            sharpSteps.forEach { (step, note) ->
                appendLine("   - Step $step -> ${note.noteText}")
            }
        }
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.io.MarkdownWriterTest"`
Expected: PASS (6 tests)

- [ ] **Step 5: Run the full test suite to confirm no regressions**

Run: `./gradlew test`
Expected: All tests PASS, including existing `PatternRepositoryTest` round-trip tests (the new `device:` line is additive; `MarkdownParser` doesn't read it yet until Task 4, but it doesn't break parsing either since `parseFrontMatter` splits on the first `:` and ignores unknown keys).

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownWriter.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownWriterTest.kt
git commit -m "Write PO-14 patterns to markdown with note grid and live half-tone-up instructions"
```

---

### Task 4: `MarkdownParser` support for PO-14 patterns

**Files:**
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownParser.kt`
- Create: `src/test/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownParserTest.kt`

**Interfaces:**
- Consumes: markdown produced by Task 3's `MarkdownWriter`.
- Produces: `fun parse(file: File): AnyPattern` (return type changed from `PO12Pattern`). Task 6 (`PatternRepository`) relies on this new return type.

- [ ] **Step 1: Write the failing tests**

```kotlin
package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.Pitch
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MarkdownParserTest {

    private val testDir = File("build/test-output/markdown-parser")
    private val writer = MarkdownWriter()
    private val parser = MarkdownParser()

    @BeforeTest
    fun setup() { testDir.mkdirs() }

    @AfterTest
    fun cleanup() { testDir.listFiles()?.forEach { it.delete() } }

    @Test
    fun `parses a PO12 pattern file without a device line as PO-12`() {
        val pattern = fr.nicolaslinard.po.toolbox.TestFixtures.createSimplePattern()
        val file = writer.write(pattern, testDir)
        // Simulate an old file saved before the device field existed.
        file.writeText(file.readText().lines().filterNot { it.trim().startsWith("device:") }.joinToString("\n"))

        val parsed = parser.parse(file)
        assertIs<PO12Pattern>(parsed)
    }

    @Test
    fun `round trips a PO12 pattern`() {
        val pattern = fr.nicolaslinard.po.toolbox.TestFixtures.createComplexPattern()
        val file = writer.write(pattern, testDir)

        val parsed = assertIs<PO12Pattern>(parser.parse(file))
        assertEquals(pattern.metadata.name, parsed.metadata.name)
        assertEquals(4, parsed.voices.size)
    }

    @Test
    fun `round trips a PO14 pattern`() {
        val pattern = PO14Pattern(
            steps = mapOf(
                1 to PO14Step(Pitch.C, 2),
                5 to PO14Step(Pitch.D, 2, halfToneUp = true),
                9 to PO14Step(Pitch.G, 2)
            ),
            sound = PODevice.PO_14.getVoiceByShortName("acid")!!,
            metadata = PatternMetadata(name = "Round Trip Bass", bpm = 130, deviceModel = "PO-14"),
            number = 3
        )
        val file = writer.write(pattern, testDir)

        val parsed = assertIs<PO14Pattern>(parser.parse(file))
        assertEquals("Round Trip Bass", parsed.metadata.name)
        assertEquals(130, parsed.metadata.bpm)
        assertEquals(3, parsed.number)
        assertEquals(PODevice.PO_14.getVoiceByShortName("acid"), parsed.sound)
        assertEquals(Pitch.C, parsed.getNote(1)?.pitch)
        assertEquals(2, parsed.getNote(1)?.octave)
        assertEquals(Pitch.D, parsed.getNote(5)?.pitch)
        assertEquals(true, parsed.getNote(5)?.halfToneUp)
        assertNull(parsed.getNote(2))
    }

    @Test
    fun `PO14 metadata reports PO-14 as the device model`() {
        val pattern = PO14Pattern(
            steps = emptyMap(),
            sound = PODevice.PO_14.voices.first(),
            metadata = PatternMetadata(name = "Empty Bass", deviceModel = "PO-14")
        )
        val file = writer.write(pattern, testDir)

        val parsed = parser.parse(file)
        assertEquals("PO-14", parsed.metadata.deviceModel)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.io.MarkdownParserTest"`
Expected: FAIL — `parse` returns `PO12Pattern`, so `assertIs<PO14Pattern>` cannot type-check, and PO-14 files don't round-trip.

- [ ] **Step 3: Update `MarkdownParser.kt`**

Replace the class with the version below (adds device-aware dispatch and PO-14 parsing; existing PO-12 parsing logic is unchanged, just renamed/reused):

```kotlin
package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.*
import java.io.File
import java.time.LocalDate

class MarkdownParser {

    /**
     * Parses a markdown pattern file and returns the matching pattern type,
     * dispatched by the `device` frontmatter field (defaults to PO-12 for
     * files saved before that field existed).
     */
    fun parse(file: File): AnyPattern {
        val content = file.readText()
        val lines = content.lines()

        return if (parseDeviceModel(lines) == "PO-14") {
            parseMelodic(file, lines)
        } else {
            parseDrum(lines)
        }
    }

    private fun parseDrum(lines: List<String>): PO12Pattern {
        val metadata = parseFrontMatter(lines)
        val voices = parseVoices(lines)
        val patternNumber = parsePatternNumber(lines)

        return PO12Pattern(
            voices = voices,
            metadata = metadata,
            number = patternNumber
        )
    }

    private fun parseMelodic(file: File, lines: List<String>): PO14Pattern {
        val metadata = parseFrontMatter(lines)
        val patternNumber = parsePatternNumber(lines)
        val sound = parseSound(lines)
            ?: throw IllegalArgumentException("Missing or unrecognized Sound in ${file.name}")
        val steps = parseMelodicSteps(lines)

        return PO14Pattern(
            steps = steps,
            sound = sound,
            metadata = metadata,
            number = patternNumber
        )
    }

    private fun parseDeviceModel(lines: List<String>): String {
        lines.forEach { line ->
            val match = Regex("""^device:\s*"?([^"]+)"?\s*$""").find(line.trim())
            if (match != null) return match.groupValues[1].trim()
        }
        return "PO-12"
    }

    private fun parseSound(lines: List<String>): POVoice? {
        val line = lines.find { it.trim().startsWith("**Sound:**") } ?: return null
        val match = Regex("""\(Sound (\d+)\)""").find(line) ?: return null
        return PODevice.PO_14.getVoiceByNumber(match.groupValues[1].toInt())
    }

    private fun parseMelodicSteps(lines: List<String>): Map<Int, PO14Step> {
        val noteLine = lines.find { it.trim().startsWith("Note:") } ?: return emptyMap()
        val tokens = noteLine.substringAfter("Note:").trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        val steps = mutableMapOf<Int, PO14Step>()
        tokens.forEachIndexed { index, token ->
            val step = index + 1
            if (step !in 1..16 || token == ".") return@forEachIndexed
            val match = Regex("""^([A-Ga-g])(#)?(\d+)$""").find(token) ?: return@forEachIndexed
            val (letter, sharp, octaveText) = match.destructured
            val pitch = Pitch.fromLetter(letter) ?: return@forEachIndexed
            steps[step] = PO14Step(pitch = pitch, octave = octaveText.toInt(), halfToneUp = sharp == "#")
        }
        return steps
    }

    private fun parseFrontMatter(lines: List<String>): PatternMetadata {
        val frontMatterStart = lines.indexOfFirst { it.trim() == "---" }
        val frontMatterEnd = lines.drop(frontMatterStart + 1).indexOfFirst { it.trim() == "---" } + frontMatterStart + 1

        if (frontMatterStart == -1 || frontMatterEnd == -1) {
            throw IllegalArgumentException("Invalid markdown: missing frontmatter")
        }

        val frontMatter = lines.subList(frontMatterStart + 1, frontMatterEnd)
        val map = mutableMapOf<String, String>()

        frontMatter.forEach { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                map[parts[0].trim()] = parts[1].trim()
            }
        }

        return PatternMetadata(
            name = extractQuotedValue(map["name"]) ?: throw IllegalArgumentException("Missing name in frontmatter"),
            description = extractQuotedValue(map["description"]),
            bpm = map["bpm"]?.toIntOrNull(),
            genre = parseGenreList(map["genre"]),
            difficulty = map["difficulty"]?.let { Difficulty.fromString(it) },
            sourceAttribution = extractQuotedValue(map["source"]),
            author = extractQuotedValue(map["author"]),
            dateCreated = map["date"]?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            deviceModel = extractQuotedValue(map["device"]) ?: "PO-12"
        )
    }

    private fun parseVoices(lines: List<String>): Map<PO12DrumVoice, List<Int>> {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Look for voice headers like "### Bass Drum (Sound 1)"
            if (line.startsWith("###")) {
                val soundNumberMatch = Regex("""Sound (\d+)""").find(line)
                if (soundNumberMatch != null) {
                    val soundNumber = soundNumberMatch.groupValues[1].toInt()
                    val voice = PO12DrumVoice.fromPONumber(soundNumber)

                    if (voice != null) {
                        // Find the grid in the next few lines
                        val steps = parseStepGrid(lines, i)
                        if (steps.isNotEmpty()) {
                            voices[voice] = steps
                        }
                    }
                }
            }
            i++
        }

        return voices
    }

    private fun parseStepGrid(lines: List<String>, startIndex: Int): List<Int> {
        // Look for the grid line after the header (within next 5 lines)
        for (i in startIndex until minOf(startIndex + 10, lines.size)) {
            val line = lines[i]
            if (line.contains("[●]") || line.contains("[ ]")) {
                return extractActiveSteps(line)
            }
        }
        return emptyList()
    }

    private fun extractActiveSteps(gridLine: String): List<Int> {
        val steps = mutableListOf<Int>()

        // Find all [●] and [ ] patterns
        val pattern = Regex("\\[(●| )\\]")
        val matches = pattern.findAll(gridLine)

        var stepNumber = 1
        matches.forEach { match ->
            if (match.value == "[●]") {
                steps.add(stepNumber)
            }
            stepNumber++
        }

        return steps
    }

    private fun parsePatternNumber(lines: List<String>): Int {
        // Look for "pattern_numbers: [1]" in frontmatter
        lines.forEach { line ->
            val match = Regex("""pattern_numbers:\s*\[(\d+)]""").find(line)
            if (match != null) {
                return match.groupValues[1].toInt()
            }
        }
        return 1 // Default to pattern 1
    }

    private fun extractQuotedValue(value: String?): String? {
        if (value == null) return null
        val trimmed = value.trim()
        return if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed.substring(1, trimmed.length - 1)
                .replace("\\\"", "\"")
        } else {
            trimmed
        }
    }

    private fun parseGenreList(value: String?): List<String> {
        if (value == null) return emptyList()
        val trimmed = value.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()

        val content = trimmed.substring(1, trimmed.length - 1)
        return content.split(",")
            .map { extractQuotedValue(it.trim()) ?: "" }
            .filter { it.isNotBlank() }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.io.MarkdownParserTest"`
Expected: PASS (4 tests)

- [ ] **Step 5: Run the full test suite to confirm no regressions**

Run: `./gradlew test`
Expected: All tests PASS. `PatternRepositoryTest`'s round-trip tests still pass because `FilePatternRepository` currently type-checks its `PatternSummary` against `PO12Pattern` only — that changes in Task 6, so if this task is executed standalone, `PatternRepository.kt`/`PatternController.kt` will now fail to compile against the new `parse(): AnyPattern` return type. If the build fails to compile at this point, that's expected and resolved by Task 6 immediately after — do not work around it here.

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownParser.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownParserTest.kt
git commit -m "Parse PO-14 pattern files back into PO14Pattern, dispatching on the device field"
```

---

### Task 5: MIDI export and playback for PO-14 patterns

**Files:**
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiNoteMapper.kt`
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiExporter.kt`
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiPlaybackService.kt`
- Modify: `src/test/kotlin/fr/nicolaslinard/po/toolbox/io/MidiExporterTest.kt`

**Interfaces:**
- Consumes: `PO14Pattern`, `PO14Step`, `AnyPattern` (Task 1-2).
- Produces: `MidiExporter.exportToMidi(pattern: AnyPattern, ...)`, `MidiExporter.createSequence(patterns: List<AnyPattern>, ...)`, `MidiPlaybackService.play(pattern: AnyPattern)` (signatures widened from `PO12Pattern`/`List<PO12Pattern>` — source-compatible for all existing callers since `PO12Pattern : AnyPattern` and Kotlin's `List` is declared covariant). Task 6 (`PatternController`/`PatternDetailView`) and Task 10-11 (UI Play buttons) rely on these widened signatures.

- [ ] **Step 1: Add failing tests to `MidiExporterTest.kt`**

Append inside the existing `MidiExporterTest` class (keep all current tests as-is):

```kotlin
    // === PO-14 melodic export ===

    @Test
    fun `maps a PO14 step to its MIDI note`() {
        val mapper = MidiNoteMapper()
        val step = fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.C, octave = 3)
        assertEquals(step.midiNote, mapper.getMidiNote(step))
    }

    @Test
    fun `exports a PO14 pattern to a valid MIDI file`() {
        val pattern = fr.nicolaslinard.po.toolbox.models.PO14Pattern(
            steps = mapOf(
                1 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.C, 2),
                9 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.G, 2)
            ),
            sound = fr.nicolaslinard.po.toolbox.models.PODevice.PO_14.voices.first(),
            metadata = fr.nicolaslinard.po.toolbox.models.PatternMetadata(name = "Midi Bass", bpm = 100, deviceModel = "PO-14")
        )
        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "po14_pattern.mid")

        exporter.exportToMidi(pattern, outputFile)

        assertTrue(outputFile.exists())
        val sequence = MidiSystem.getSequence(outputFile)
        assertNotNull(sequence)

        val track = sequence.tracks.first()
        val noteOnEvents = (0 until track.size()).map { track.get(it) }
            .filter { (it.message as? ShortMessage)?.command == ShortMessage.NOTE_ON }
        // 2 programmed steps -> 2 NOTE_ON events, and none on the drum channel (9)
        assertEquals(2, noteOnEvents.size)
        assertTrue(noteOnEvents.none { (it.message as ShortMessage).channel == 9 })
    }
```

Add the needed imports at the top of the file (alongside the existing ones):

```kotlin
import kotlin.test.assertNotNull
```

(`assertEquals`, `assertTrue` are already imported via the existing `kotlin.test.*` wildcard import in this file.)

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.io.MidiExporterTest"`
Expected: FAIL — `MidiNoteMapper.getMidiNote(PO14Step)` doesn't exist, and `exportToMidi` doesn't accept a `PO14Pattern`.

- [ ] **Step 3a: Add the PO-14 overload to `MidiNoteMapper.kt`**

Add inside the `MidiNoteMapper` class, after the existing `getMidiNote(voice: PO12DrumVoice)`:

```kotlin
    /**
     * Get the MIDI note number for a programmed PO-14 step (already
     * resolves pitch, octave and the live half-tone-up effect).
     */
    fun getMidiNote(step: fr.nicolaslinard.po.toolbox.models.PO14Step): Int = step.midiNote
```

- [ ] **Step 3b: Widen `MidiExporter.kt` to accept `AnyPattern` and add melodic export**

Replace the file's imports and the pattern-processing methods:

```kotlin
package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import java.io.File
import javax.sound.midi.*
```

Change the public methods' parameter types from `PO12Pattern`/`List<PO12Pattern>` to `AnyPattern`/`List<AnyPattern>`:

```kotlin
    fun exportToMidi(
        pattern: AnyPattern,
        outputFile: File,
        options: MidiExportOptions = MidiExportOptions()
    ) {
        val sequence = createSequence(listOf(pattern), options)
        saveMidiFile(sequence, outputFile)
    }

    fun exportPatternsToMidi(
        patterns: List<AnyPattern>,
        outputFile: File,
        options: MidiExportOptions = MidiExportOptions()
    ) {
        val sequence = createSequence(patterns, options)
        saveMidiFile(sequence, outputFile)
    }

    fun createSequence(
        patterns: List<AnyPattern>,
        options: MidiExportOptions
    ): Sequence {
        val sequence = Sequence(Sequence.PPQ, options.resolution)
        val track = sequence.createTrack()

        if (patterns.isNotEmpty()) {
            val bpm = patterns.first().metadata.bpm ?: 120
            addTempoEvent(track, 0, bpm)

            if (options.includeMetadata) {
                addTrackName(track, patterns.first().metadata.name)
            }
        }

        var currentTick = 0L
        patterns.forEach { pattern ->
            currentTick = addPatternToTrack(track, pattern, currentTick, options)
        }

        addEndOfTrack(track, currentTick)

        return sequence
    }
```

Replace `addPatternToTrack` with a dispatcher plus the two type-specific implementations:

```kotlin
    private fun addPatternToTrack(
        track: Track,
        pattern: AnyPattern,
        startTick: Long,
        options: MidiExportOptions
    ): Long = when (pattern) {
        is PO12Pattern -> addDrumPatternToTrack(track, pattern, startTick, options)
        is PO14Pattern -> addMelodicPatternToTrack(track, pattern, startTick, options)
    }

    private fun addDrumPatternToTrack(
        track: Track,
        pattern: PO12Pattern,
        startTick: Long,
        options: MidiExportOptions
    ): Long {
        val ticksPerStep = calculateTicksPerStep(options.resolution)

        pattern.voices.forEach { (voice, steps) ->
            val midiNote = noteMapper.getMidiNote(voice)

            steps.forEach { step ->
                val stepTick = startTick + ((step - 1) * ticksPerStep)

                addNoteEvent(track, stepTick, ShortMessage.NOTE_ON, midiNote, options.defaultVelocity, DRUM_CHANNEL)
                addNoteEvent(track, stepTick + options.noteDuration, ShortMessage.NOTE_OFF, midiNote, 0, DRUM_CHANNEL)
            }
        }

        return startTick + (16 * ticksPerStep)
    }

    private fun addMelodicPatternToTrack(
        track: Track,
        pattern: PO14Pattern,
        startTick: Long,
        options: MidiExportOptions
    ): Long {
        val ticksPerStep = calculateTicksPerStep(options.resolution)

        pattern.steps.forEach { (step, note) ->
            val midiNote = noteMapper.getMidiNote(note)
            val stepTick = startTick + ((step - 1) * ticksPerStep)

            addNoteEvent(track, stepTick, ShortMessage.NOTE_ON, midiNote, options.defaultVelocity, MELODIC_CHANNEL)
            addNoteEvent(track, stepTick + options.noteDuration, ShortMessage.NOTE_OFF, midiNote, 0, MELODIC_CHANNEL)
        }

        return startTick + (16 * ticksPerStep)
    }
```

Update `addNoteEvent` to take an explicit channel (it was hardcoded to `DRUM_CHANNEL` before), and add the `MELODIC_CHANNEL` constant next to the existing `DRUM_CHANNEL` one:

```kotlin
    companion object {
        private const val DRUM_CHANNEL = 9 // MIDI channel 10 (index 9) for drums
        private const val MELODIC_CHANNEL = 0 // MIDI channel 1 (index 0) for the PO-14 bass line
        private const val MICROSECONDS_PER_MINUTE = 60_000_000
        private const val DEFAULT_RESOLUTION = 96 // Pulses Per Quarter note (PPQ)
        private const val DEFAULT_NOTE_DURATION = 96 // Duration in ticks (1 quarter note)
    }
```

```kotlin
    private fun addNoteEvent(
        track: Track,
        tick: Long,
        command: Int,
        note: Int,
        velocity: Int,
        channel: Int
    ) {
        val message = ShortMessage()
        message.setMessage(command, channel, note, velocity)
        track.add(MidiEvent(message, tick))
    }
```

- [ ] **Step 3c: Widen `MidiPlaybackService.kt`**

Change the imports and the `play`/`playPatterns` signatures:

```kotlin
package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PatternChain
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequencer
```

```kotlin
    fun play(pattern: AnyPattern) {
        playPatterns(listOf(pattern), 1)
    }
```

```kotlin
    private fun playPatterns(patterns: List<AnyPattern>, barCount: Int) {
```

(`playChain(chain: PatternChain)` stays untouched — `PatternChain.getPatternsInSequence(): List<PO12Pattern>` upcasts fine to the widened `List<AnyPattern>` parameter of `playPatterns`.)

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.io.MidiExporterTest"`
Expected: PASS (all tests, including the 2 new ones)

- [ ] **Step 5: Run the full test suite to confirm no regressions**

Run: `./gradlew test`
Expected: All tests PASS, including `MidiPlaybackServiceTest`.

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiNoteMapper.kt src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiExporter.kt src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiPlaybackService.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/io/MidiExporterTest.kt
git commit -m "Export and play back PO-14 patterns as pitched MIDI notes on a melodic channel"
```

---

### Task 6: Widen `PatternRepository`, `PatternController`, `PatternSummary` to `AnyPattern`

**Files:**
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternRepository.kt`
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternController.kt`
- Modify: `src/test/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternRepositoryTest.kt`

**Interfaces:**
- Consumes: `AnyPattern`, `PO12Pattern`, `PO14Pattern` (Task 1-2), `MarkdownParser.parse(): AnyPattern` (Task 4), `MarkdownWriter.write(AnyPattern, File)` (Task 3).
- Produces: `PatternRepository.save/update(pattern: AnyPattern)`, `PatternSummary(file: File, pattern: AnyPattern)`, `PatternController.selectedPattern: SimpleObjectProperty<AnyPattern?>`, `PatternController.createPattern/updatePattern(pattern: AnyPattern)`. Tasks 9-12 (UI) consume these.

- [ ] **Step 1: Add failing tests to `PatternRepositoryTest.kt`**

Append inside the existing `PatternRepositoryTest` class:

```kotlin
    // --- PO-14 patterns ---

    @Test
    fun `should save and load a PO14 pattern alongside PO12 patterns`() {
        val po12 = TestFixtures.createSimplePattern(name = "Drum Pattern")
        val po14 = fr.nicolaslinard.po.toolbox.models.PO14Pattern(
            steps = mapOf(1 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.C, 2)),
            sound = fr.nicolaslinard.po.toolbox.models.PODevice.PO_14.voices.first(),
            metadata = fr.nicolaslinard.po.toolbox.models.PatternMetadata(name = "Bass Pattern", deviceModel = "PO-14")
        )

        repository.save(po12)
        repository.save(po14)

        val loaded = repository.loadAll()
        assertEquals(2, loaded.size)
        assertTrue(loaded.any { it.pattern is PO12Pattern })
        assertTrue(loaded.any { it.pattern is fr.nicolaslinard.po.toolbox.models.PO14Pattern })
    }

    @Test
    fun `PO14 pattern summary reports its programmed note count as voiceCount`() {
        val po14 = fr.nicolaslinard.po.toolbox.models.PO14Pattern(
            steps = mapOf(
                1 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.C, 2),
                9 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.G, 2)
            ),
            sound = fr.nicolaslinard.po.toolbox.models.PODevice.PO_14.voices.first(),
            metadata = fr.nicolaslinard.po.toolbox.models.PatternMetadata(name = "Bass Count Test", deviceModel = "PO-14")
        )
        repository.save(po14)

        val summary = repository.loadAll().first()
        assertEquals("2", summary.voiceCount)
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.desktop.PatternRepositoryTest"`
Expected: FAIL to compile — `PatternRepository.save`/`PatternSummary.pattern` are still typed to `PO12Pattern`.

- [ ] **Step 3a: Update `PatternRepository.kt`**

```kotlin
package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.io.MarkdownWriter
import fr.nicolaslinard.po.toolbox.models.AnyPattern
import java.io.File

interface PatternRepository {
    fun loadAll(): List<PatternSummary>
    fun save(pattern: AnyPattern): File
    fun update(originalFile: File, updatedPattern: AnyPattern): File
    fun delete(file: File)
}

class FilePatternRepository(
    private val baseDir: File = File("patterns"),
    private val parser: MarkdownParser = MarkdownParser(),
    private val writer: MarkdownWriter = MarkdownWriter()
) : PatternRepository {

    override fun loadAll(): List<PatternSummary> {
        if (!baseDir.exists()) return emptyList()

        return baseDir.listFiles { f -> f.extension == "md" }
            ?.sortedBy { it.name }
            ?.mapNotNull { file ->
                try {
                    PatternSummary(file, parser.parse(file))
                } catch (_: Exception) {
                    null
                }
            }
            ?: emptyList()
    }

    override fun save(pattern: AnyPattern): File {
        baseDir.mkdirs()
        return writer.write(pattern, baseDir)
    }

    override fun update(originalFile: File, updatedPattern: AnyPattern): File {
        val backup = File(originalFile.parent, originalFile.name + ".bak")
        try {
            originalFile.copyTo(backup, overwrite = true)
            originalFile.delete()
            val newFile = writer.write(updatedPattern, baseDir)
            backup.delete()
            return newFile
        } catch (e: Exception) {
            if (backup.exists() && !originalFile.exists()) {
                backup.renameTo(originalFile)
            }
            backup.delete()
            throw e
        }
    }

    override fun delete(file: File) {
        file.delete()
    }
}
```

- [ ] **Step 3b: Update `PatternController.kt`**

```kotlin
package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import tornadofx.Controller
import java.io.File

class PatternController : Controller() {

    val patterns = FXCollections.observableArrayList<PatternSummary>()
    val selectedPattern = SimpleObjectProperty<AnyPattern?>()
    val selectedSummary = SimpleObjectProperty<PatternSummary?>()
    var activeDialogResult: PatternDialogResult? = null

    var repository: PatternRepository = FilePatternRepository()

    fun loadPatterns() {
        patterns.setAll(repository.loadAll())
    }

    fun selectPattern(summary: PatternSummary?) {
        selectedSummary.set(summary)
        selectedPattern.set(summary?.pattern)
        activeDialogResult = null
    }

    fun createPattern(pattern: AnyPattern) {
        repository.save(pattern)
        loadPatterns()
    }

    fun updatePattern(originalFile: File, updatedPattern: AnyPattern) {
        val newFile = repository.update(originalFile, updatedPattern)
        loadPatterns()
        val updated = patterns.find { it.file.absolutePath == newFile.absolutePath }
        selectPattern(updated)
    }

    fun deleteSelectedPattern() {
        val file = selectedSummary.value?.file ?: return
        repository.delete(file)
        selectedSummary.set(null)
        selectedPattern.set(null)
        loadPatterns()
    }
}

data class PatternSummary(val file: File, val pattern: AnyPattern) {
    val name: String get() = pattern.metadata.name
    val bpm: String get() = pattern.metadata.bpm?.toString() ?: "-"
    val genre: String get() = pattern.metadata.genre.joinToString(", ").ifBlank { "-" }
    val difficulty: String get() = pattern.metadata.difficulty?.displayName ?: "-"
    val voiceCount: String get() = when (pattern) {
        is PO12Pattern -> pattern.voices.size.toString()
        is PO14Pattern -> pattern.steps.size.toString()
    }
    val patternNumber: String get() = "#${pattern.number}"
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.desktop.PatternRepositoryTest"`
Expected: PASS (all tests, including the 2 new ones)

- [ ] **Step 5: Build the whole project to confirm the desktop layer still compiles**

Run: `./gradlew compileKotlin`
Expected: **This will fail** — `NewPatternDialog.kt`, `PatternDetailView.kt`, `MainView.kt`, `GeneratePatternDialog.kt` still assume `PO12Pattern` in a few spots that are only fixed in Tasks 10-12. That's expected at this point in the plan; do not try to patch those files here. Run the narrower test target from Step 4 to confirm this task's own changes are correct, and proceed to the next task.

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternRepository.kt src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternController.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternRepositoryTest.kt
git commit -m "Widen PatternRepository/PatternController/PatternSummary to hold any pattern type"
```

---

### Task 7: "Classic" PO-14 templates

**Files:**
- Create: `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/Po14PatternTemplate.kt`
- Test: `src/test/kotlin/fr/nicolaslinard/po/toolbox/models/Po14PatternTemplateTest.kt`

**Interfaces:**
- Consumes: `PO14Step`, `Pitch`, `PODevice.PO_14` (Task 2, existing).
- Produces: `data class Po14PatternTemplate(id, name, description, difficulty, sound, steps, suggestedBPM)` and `object BuiltInPo14Templates { fun all(): List<Po14PatternTemplate> }`. Task 8 (`NewMelodicPatternDialogModel`) and Task 9 (`NewMelodicPatternDialog`) consume `BuiltInPo14Templates.all()`.

- [ ] **Step 1: Write the failing tests**

```kotlin
package fr.nicolaslinard.po.toolbox.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Po14PatternTemplateTest {

    @Test
    fun `provides at least four built-in templates`() {
        assertTrue(BuiltInPo14Templates.all().size >= 4)
    }

    @Test
    fun `every template has unique id, at least one note, and a valid sound`() {
        val templates = BuiltInPo14Templates.all()
        assertEquals(templates.size, templates.map { it.id }.distinct().size)
        templates.forEach { template ->
            assertTrue(template.steps.isNotEmpty(), "${template.id} has no notes")
            assertTrue(template.steps.keys.all { it in 1..16 }, "${template.id} has an out-of-range step")
            assertTrue(PODevice.PO_14.voices.contains(template.sound), "${template.id} uses an unknown sound")
        }
    }

    @Test
    fun `octave bassline alternates the root note across octaves`() {
        val template = BuiltInPo14Templates.all().first { it.id == "octave-bassline" }
        assertEquals(Pitch.C, template.steps[1]?.pitch)
        assertEquals(Pitch.C, template.steps[5]?.pitch)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.models.Po14PatternTemplateTest"`
Expected: FAIL — `Po14PatternTemplate`/`BuiltInPo14Templates` don't exist.

- [ ] **Step 3: Create the templates**

```kotlin
package fr.nicolaslinard.po.toolbox.models

/**
 * A pre-built PO-14 bass pattern, mirroring [PatternTemplate] for the PO-12
 * but carrying a note grid instead of a per-voice step grid.
 */
data class Po14PatternTemplate(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: Difficulty,
    val sound: POVoice,
    val steps: Map<Int, PO14Step>,
    val suggestedBPM: Int? = null
)

/**
 * Built-in "classic" bass patterns for the PO-14 Sub, covering the same
 * kind of foundational grooves BuiltInTemplates offers for the PO-12.
 */
object BuiltInPo14Templates {

    private val sine = PODevice.PO_14.getVoiceByShortName("sine")!!
    private val acid = PODevice.PO_14.getVoiceByShortName("acid")!!
    private val subDrop = PODevice.PO_14.getVoiceByShortName("drop")!!

    val OCTAVE_BASSLINE = Po14PatternTemplate(
        id = "octave-bassline",
        name = "Octave Bassline",
        description = "Root note alternating with its octave, the classic four-on-the-floor bass companion",
        difficulty = Difficulty.BEGINNER,
        sound = sine,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2), 5 to PO14Step(Pitch.C, 3),
            9 to PO14Step(Pitch.C, 2), 13 to PO14Step(Pitch.C, 3)
        ),
        suggestedBPM = 120
    )

    val WALKING_BASS = Po14PatternTemplate(
        id = "walking-bass",
        name = "Simple Walking Bass",
        description = "Root-third-fifth-octave walk, one note per beat",
        difficulty = Difficulty.BEGINNER,
        sound = sine,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2), 5 to PO14Step(Pitch.E, 2),
            9 to PO14Step(Pitch.G, 2), 13 to PO14Step(Pitch.C, 3)
        ),
        suggestedBPM = 100
    )

    val ACID_16TH = Po14PatternTemplate(
        id = "acid-16th",
        name = "Acid 16th Bassline",
        description = "Syncopated 16th-note acid line with a live half-tone accent",
        difficulty = Difficulty.INTERMEDIATE,
        sound = acid,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2), 3 to PO14Step(Pitch.C, 2),
            5 to PO14Step(Pitch.C, 2), 7 to PO14Step(Pitch.D, 2, halfToneUp = true),
            8 to PO14Step(Pitch.C, 2), 11 to PO14Step(Pitch.C, 2),
            13 to PO14Step(Pitch.C, 2), 15 to PO14Step(Pitch.A, 1)
        ),
        suggestedBPM = 130
    )

    val SUB_DROP_HITS = Po14PatternTemplate(
        id = "sub-drop-hits",
        name = "Sub Drop Hits",
        description = "Sparse sub-bass hits on the downbeats, for dub/reggae-style patterns",
        difficulty = Difficulty.BEGINNER,
        sound = subDrop,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 1), 9 to PO14Step(Pitch.C, 1)
        ),
        suggestedBPM = 78
    )

    fun all(): List<Po14PatternTemplate> = listOf(OCTAVE_BASSLINE, WALKING_BASS, ACID_16TH, SUB_DROP_HITS)
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.models.Po14PatternTemplateTest"`
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/models/Po14PatternTemplate.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/models/Po14PatternTemplateTest.kt
git commit -m "Add classic PO-14 bass pattern templates (octave, walking bass, acid, sub drop)"
```

---

### Task 8: `NewMelodicPatternDialogModel`

**Files:**
- Create: `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewMelodicPatternDialogModel.kt`
- Test: `src/test/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewMelodicPatternDialogModelTest.kt`

**Interfaces:**
- Consumes: `PO14Pattern`, `PO14Step`, `Po14PatternTemplate`, `BuiltInPo14Templates`, `PODevice.PO_14` (Task 2, 7).
- Produces: `class NewMelodicPatternDialogModel(existingPattern: PO14Pattern? = null)` with `name`, `patternNumber`, `bpm`, `difficulty`, `sound` vars, `steps: Map<Int, PO14Step>`, `setStep`, `clearStep`, `isValid()`, `applyTemplate(Po14PatternTemplate)`, `buildPattern(): PO14Pattern`. Task 9 (`NewMelodicPatternDialog`) consumes this directly.

- [ ] **Step 1: Write the failing tests**

```kotlin
package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.BuiltInPo14Templates
import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.Pitch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewMelodicPatternDialogModelTest {

    @Test
    fun `defaults to create mode with an empty name and no notes`() {
        val model = NewMelodicPatternDialogModel()
        assertFalse(model.isEditMode)
        assertEquals("", model.name)
        assertTrue(model.steps.isEmpty())
    }

    @Test
    fun `is invalid when name is blank`() {
        val model = NewMelodicPatternDialogModel()
        model.name = "   "
        assertFalse(model.isValid())
    }

    @Test
    fun `is valid once a name is set`() {
        val model = NewMelodicPatternDialogModel()
        model.name = "My Bassline"
        assertTrue(model.isValid())
    }

    @Test
    fun `setStep adds a note and clearStep removes it`() {
        val model = NewMelodicPatternDialogModel()
        model.setStep(1, PO14Step(Pitch.C, 2))
        assertEquals(Pitch.C, model.steps[1]?.pitch)

        model.clearStep(1)
        assertNull(model.steps[1])
    }

    @Test
    fun `setStep rejects steps outside 1 to 16`() {
        val model = NewMelodicPatternDialogModel()
        assertTrue(runCatching { model.setStep(17, PO14Step(Pitch.C, 2)) }.isFailure)
    }

    @Test
    fun `applyTemplate replaces steps and sound and suggests a BPM`() {
        val model = NewMelodicPatternDialogModel()
        val template = BuiltInPo14Templates.OCTAVE_BASSLINE

        model.applyTemplate(template)

        assertEquals(template.steps, model.steps)
        assertEquals(template.sound, model.sound)
        assertEquals(template.suggestedBPM.toString(), model.bpm)
    }

    @Test
    fun `buildPattern produces a PO14Pattern with PO-14 as the device model`() {
        val model = NewMelodicPatternDialogModel()
        model.name = "Built Pattern"
        model.bpm = "128"
        model.setStep(1, PO14Step(Pitch.C, 2))

        val pattern = model.buildPattern()

        assertEquals("Built Pattern", pattern.metadata.name)
        assertEquals(128, pattern.metadata.bpm)
        assertEquals("PO-14", pattern.metadata.deviceModel)
        assertEquals(Pitch.C, pattern.getNote(1)?.pitch)
    }

    @Test
    fun `pre-fills from an existing pattern in edit mode`() {
        val existing = PO14Pattern(
            steps = mapOf(1 to PO14Step(Pitch.G, 1)),
            sound = PODevice.PO_14.voices.first(),
            metadata = PatternMetadata(name = "Existing", difficulty = Difficulty.INTERMEDIATE, deviceModel = "PO-14"),
            number = 5
        )
        val model = NewMelodicPatternDialogModel(existing)

        assertTrue(model.isEditMode)
        assertEquals("Existing", model.name)
        assertEquals(5, model.patternNumber)
        assertEquals("intermediate", model.difficulty)
        assertEquals(Pitch.G, model.steps[1]?.pitch)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.desktop.NewMelodicPatternDialogModelTest"`
Expected: FAIL — `NewMelodicPatternDialogModel` doesn't exist.

- [ ] **Step 3: Create the model**

```kotlin
package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.POVoice
import fr.nicolaslinard.po.toolbox.models.Po14PatternTemplate
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import java.util.TreeMap

class NewMelodicPatternDialogModel(existingPattern: PO14Pattern? = null) {

    var name: String = existingPattern?.metadata?.name ?: ""
    var patternNumber: Int = existingPattern?.number ?: 1
    var bpm: String = existingPattern?.metadata?.bpm?.toString() ?: ""
    var difficulty: String = existingPattern?.metadata?.difficulty?.displayName ?: "-"
    var sound: POVoice = existingPattern?.sound ?: PODevice.PO_14.voices.first()

    val isEditMode: Boolean = existingPattern != null

    private val _steps: TreeMap<Int, PO14Step> = TreeMap(existingPattern?.steps ?: emptyMap())

    val steps: Map<Int, PO14Step> get() = _steps

    fun setStep(step: Int, note: PO14Step) {
        require(step in 1..16) { "Step must be between 1 and 16" }
        _steps[step] = note
    }

    fun clearStep(step: Int) {
        _steps.remove(step)
    }

    fun isValid(): Boolean = name.trim().isNotEmpty()

    fun applyTemplate(template: Po14PatternTemplate) {
        _steps.clear()
        _steps.putAll(template.steps)
        sound = template.sound
        template.suggestedBPM?.let { bpm = it.toString() }
    }

    fun buildPattern(): PO14Pattern = PO14Pattern(
        steps = steps.toMap(),
        sound = sound,
        metadata = PatternMetadata(
            name = name.trim(),
            bpm = bpm.trim().toIntOrNull(),
            difficulty = Difficulty.fromString(difficulty),
            deviceModel = PODevice.PO_14.modelId
        ),
        number = patternNumber
    )
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "fr.nicolaslinard.po.toolbox.desktop.NewMelodicPatternDialogModelTest"`
Expected: PASS (8 tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewMelodicPatternDialogModel.kt src/test/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewMelodicPatternDialogModelTest.kt
git commit -m "Add NewMelodicPatternDialogModel for PO-14 pattern creation and editing"
```

---

### Task 9: `NewMelodicPatternDialog` (JavaFX UI)

**Files:**
- Create: `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewMelodicPatternDialog.kt`

**Interfaces:**
- Consumes: `NewMelodicPatternDialogModel` (Task 8), `PO14Pattern`, `PO14Step`, `Pitch`, `PODevice`, `POVoice`, `Po14PatternTemplate`, `BuiltInPo14Templates`, `ScaledSize`, `ThemeManager`, `SharedAccessibilityPreferences` (all existing or from earlier tasks).
- Produces: `class NewMelodicPatternDialog(existingPattern: PO14Pattern? = null) { fun show(): PO14Pattern? }`. Task 11 (`MainView`) consumes this directly.

No unit test for this file — it is a JavaFX `Dialog`, matching the existing convention that `NewPatternDialog.kt` has no test file (only its `NewPatternDialogModel` does, covered in Task 8's equivalent). Verified manually in Task 12.

- [ ] **Step 1: Create the dialog**

```kotlin
package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.BuiltInPo14Templates
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.POVoice
import fr.nicolaslinard.po.toolbox.models.Pitch
import fr.nicolaslinard.po.toolbox.models.Po14PatternTemplate
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.StringConverter

class NewMelodicPatternDialog(existingPattern: PO14Pattern? = null) {

    val model = NewMelodicPatternDialogModel(existingPattern)
    private val sz = ScaledSize()

    private val nameField = TextField().apply { promptText = "Nom du pattern (requis)"; accessibleText = "Nom du pattern" }
    private val patternNumberSpinner = Spinner<Int>(1, 16, 1).apply { prefWidth = 70.0; accessibleText = "Numéro de pattern" }
    private val bpmField = TextField().apply { promptText = "ex: 100"; prefWidth = 70.0; accessibleText = "BPM" }
    private val difficultyCombo = ComboBox<String>().apply { accessibleText = "Difficulté" }
    private val soundCombo = ComboBox<POVoice>().apply {
        items.addAll(PODevice.PO_14.voices)
        converter = object : StringConverter<POVoice>() {
            override fun toString(v: POVoice?) = v?.let { "${it.displayName} (${it.number})" } ?: ""
            override fun fromString(s: String) = null
        }
        accessibleText = "Son du PO-14"
    }
    private val templateCombo = ComboBox<Po14PatternTemplate>().apply {
        items.addAll(BuiltInPo14Templates.all())
        converter = object : StringConverter<Po14PatternTemplate>() {
            override fun toString(t: Po14PatternTemplate?) = t?.name ?: ""
            override fun fromString(s: String) = null
        }
        accessibleText = "Template de pattern"
    }

    private val noteCombos = Array(16) { ComboBox<String>() }
    private val octaveSpinners = Array(16) { Spinner<Int>(0, 8, 3) }
    private val sharpChecks = Array(16) { CheckBox("#") }

    fun show(): PO14Pattern? {
        val dialog = Dialog<PO14Pattern?>()
        dialog.title = if (model.isEditMode) "Éditer le pattern PO-14" else "Nouveau pattern PO-14"
        dialog.headerText = null
        dialog.isResizable = true
        val content = buildContent()
        dialog.dialogPane.content = content
        dialog.dialogPane.prefWidth = sz.dialogWidth
        dialog.dialogPane.prefHeight = sz.dialogHeight

        content.prefHeightProperty().bind(dialog.dialogPane.heightProperty())

        val prefs = SharedAccessibilityPreferences.instance
        dialog.dialogPane.sceneProperty().addListener { _, _, scene ->
            scene?.let { ThemeManager.apply(it, prefs) }
        }

        val saveType = ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.addAll(saveType, ButtonType.CANCEL)

        val saveButton = dialog.dialogPane.lookupButton(saveType)
        saveButton.isDisable = nameField.text.trim().isEmpty()
        nameField.textProperty().addListener { _, _, v ->
            saveButton.isDisable = v.trim().isEmpty()
        }

        dialog.setResultConverter { bt ->
            if (bt == saveType) { syncModelFromUI(); model.buildPattern() } else null
        }

        return dialog.showAndWait().orElse(null)
    }

    private fun buildContent(): VBox {
        difficultyCombo.items.addAll("-", "beginner", "intermediate", "advanced")
        difficultyCombo.value = "-"
        soundCombo.value = model.sound

        val metaRow1 = HBox(10.0, Label("Nom *").apply { labelFor = nameField }, nameField).apply {
            alignment = Pos.CENTER_LEFT
            HBox.setHgrow(nameField, Priority.ALWAYS)
        }
        val metaRow2 = HBox(10.0,
            Label("N° pattern").apply { labelFor = patternNumberSpinner }, patternNumberSpinner,
            Label("BPM").apply { labelFor = bpmField }, bpmField,
            Label("Difficulté").apply { labelFor = difficultyCombo }, difficultyCombo,
            Label("Son").apply { labelFor = soundCombo }, soundCombo
        ).apply { alignment = Pos.CENTER_LEFT }

        val applyTemplateBtn = Button("Charger").apply {
            accessibleText = "Charger le template sélectionné"
            setOnAction {
                templateCombo.value?.let { template ->
                    model.applyTemplate(template)
                    soundCombo.value = model.sound
                    if (model.bpm.isNotEmpty()) bpmField.text = model.bpm
                    refreshStepControlsFromModel()
                }
            }
        }
        val templateRow = HBox(8.0, Label("Template"), templateCombo, applyTemplateBtn).apply {
            alignment = Pos.CENTER_LEFT
        }

        val metaBox = VBox(8.0, metaRow1, metaRow2, templateRow).apply {
            padding = Insets(0.0, 0.0, 4.0, 0.0)
        }

        val stepsLabel = Label("Notes (touches blanches uniquement — # = effet 15 \"half note up\" déclenché en direct)").apply {
            styleClass.add("h2")
            isWrapText = true
        }

        val stepsGrid = GridPane().apply {
            hgap = sz.spacing
            vgap = sz.spacing
            add(Label("Step"), 0, 0)
            add(Label("Note"), 1, 0)
            add(Label("Octave"), 2, 0)
            add(Label("#"), 3, 0)
            for (i in 0 until 16) {
                val step = i + 1
                add(Label(step.toString()).apply { prefWidth = sz.toggleSize; alignment = Pos.CENTER }, 0, step)

                noteCombos[i].items.addAll("-", "C", "D", "E", "F", "G", "A", "B")
                noteCombos[i].value = "-"
                noteCombos[i].accessibleText = "Note du step $step"
                add(noteCombos[i], 1, step)

                octaveSpinners[i].isDisable = true
                add(octaveSpinners[i], 2, step)

                sharpChecks[i].isDisable = true
                add(sharpChecks[i], 3, step)

                noteCombos[i].valueProperty().addListener { _, _, v ->
                    val active = v != null && v != "-"
                    octaveSpinners[i].isDisable = !active
                    sharpChecks[i].isDisable = !active
                }
            }
        }

        if (model.isEditMode) {
            nameField.text = model.name
            patternNumberSpinner.valueFactory.value = model.patternNumber
            if (model.bpm.isNotEmpty()) bpmField.text = model.bpm
            difficultyCombo.value = model.difficulty
            refreshStepControlsFromModel()
        }

        val scrollPane = ScrollPane(stepsGrid).apply {
            isFitToWidth = true
            minHeight = sz.scrollMinHeight
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        return VBox(10.0, metaBox, Separator(), stepsLabel, scrollPane).apply {
            padding = Insets(sz.padding)
        }
    }

    private fun refreshStepControlsFromModel() {
        for (i in 0 until 16) {
            val step = i + 1
            val note = model.steps[step]
            if (note == null) {
                noteCombos[i].value = "-"
            } else {
                noteCombos[i].value = note.pitch.letter
                octaveSpinners[i].valueFactory.value = note.octave
                sharpChecks[i].isSelected = note.halfToneUp
            }
        }
    }

    private fun syncModelFromUI() {
        model.name = nameField.text
        model.patternNumber = patternNumberSpinner.value
        model.bpm = bpmField.text
        model.difficulty = difficultyCombo.value
        model.sound = soundCombo.value ?: model.sound

        for (i in 0 until 16) {
            val step = i + 1
            val letter = noteCombos[i].value
            if (letter == null || letter == "-") {
                model.clearStep(step)
            } else {
                val pitch = Pitch.fromLetter(letter) ?: continue
                model.setStep(
                    step,
                    PO14Step(
                        pitch = pitch,
                        octave = octaveSpinners[i].value,
                        halfToneUp = sharpChecks[i].isSelected
                    )
                )
            }
        }
    }
}
```

- [ ] **Step 2: Compile the project**

Run: `./gradlew compileKotlin`
Expected: Compiles cleanly (this file has no dependents yet, so nothing else should break — if the compiler reports errors elsewhere in `desktop/`, they're pre-existing from Task 6 and are fixed in Tasks 10-11, not here).

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewMelodicPatternDialog.kt
git commit -m "Add NewMelodicPatternDialog: per-step note/octave/half-tone editor for the PO-14"
```

---

### Task 10: Render PO-14 patterns in `PatternDetailView`

**Files:**
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternDetailView.kt`

**Interfaces:**
- Consumes: `AnyPattern`, `PO12Pattern`, `PO14Pattern` (Task 1-2, 6).

No unit test — matches the existing convention that `PatternDetailView.kt` has no test file. Verified manually in Task 12.

- [ ] **Step 1: Add the `PO14Pattern` import**

At the top of the file, alongside the existing imports:

```kotlin
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
```

- [ ] **Step 2: Dispatch on pattern type in the `dynamicContent` block**

Replace:

```kotlin
            dynamicContent(controller.selectedPattern) { pattern ->
                if (pattern == null) {
                    label("Sélectionner un pattern pour voir ses détails") {
                        styleClass.add("muted")
                        style = "-fx-font-size: 14px; -fx-padding: 40 0 0 0;"
                    }
                } else {
                    val result = controller.activeDialogResult
                    chainPatterns = result?.patterns ?: listOf(pattern)
                    lastDisplayedBar = 0
                    renderPattern(pattern)
                }
            }
```

with:

```kotlin
            dynamicContent(controller.selectedPattern) { pattern ->
                if (pattern == null) {
                    label("Sélectionner un pattern pour voir ses détails") {
                        styleClass.add("muted")
                        style = "-fx-font-size: 14px; -fx-padding: 40 0 0 0;"
                    }
                } else {
                    val result = controller.activeDialogResult
                    chainPatterns = if (pattern is PO12Pattern) (result?.patterns ?: listOf(pattern)) else emptyList()
                    lastDisplayedBar = 0
                    when (pattern) {
                        is PO12Pattern -> renderPattern(pattern)
                        is PO14Pattern -> renderMelodicPattern(pattern)
                    }
                }
            }
```

- [ ] **Step 3: Add the melodic rendering function**

Add this new private extension function next to the existing `renderPattern`:

```kotlin
    private fun javafx.scene.layout.Pane.renderMelodicPattern(pattern: PO14Pattern) {
        label(pattern.metadata.name) {
            styleClass.add("h1")
        }

        hbox(10.0) {
            alignment = Pos.CENTER_LEFT
            label(pattern.metadata.deviceModel) { style = "-fx-font-weight: bold;" }
            pattern.metadata.bpm?.let {
                label("| BPM : $it") { style = "-fx-font-weight: bold;" }
            }
            pattern.metadata.difficulty?.let {
                label("| ${it.displayName}") { styleClass.add("difficulty-${it.displayName}") }
            }
            label("| Son : ${pattern.sound.displayName}")
        }

        separator()

        hbox(10.0) {
            alignment = Pos.CENTER_LEFT
            label("Grille de notes") { styleClass.add("h2") }
            playbackInfoLabel = label("") { styleClass.add("muted") }
        }

        stepAnimator.clearAnimations()
        stepLabels.clear()
        stepVoiceLabels.clear()
        (1..16).forEach {
            stepLabels[it] = mutableListOf()
            stepVoiceLabels[it] = mutableListOf()
        }

        hbox(0.0) {
            alignment = Pos.CENTER_LEFT
            label("") { prefWidth = sz.voiceLabelWidth }
            (1..16).forEach { step ->
                label(step.toString()) {
                    prefWidth = sz.stepWidth
                    alignment = Pos.CENTER
                    styleClass.add("step-header")
                    accessibleText = "Step $step"
                    stepLabels[step]?.add(this)
                }
                if (step % 4 == 0 && step < 16) {
                    label("") { prefWidth = sz.beatSeparatorWidth; styleClass.add("beat-separator") }
                }
            }
        }

        hbox(0.0) {
            alignment = Pos.CENTER_LEFT
            val soundLabel = label("${pattern.sound.displayName} (%02d)".format(pattern.sound.number)) {
                prefWidth = sz.voiceLabelWidth
                alignment = Pos.CENTER_RIGHT
                styleClass.add("voice-label")
                style = "-fx-padding: 0 ${sz.voiceLabelPadding.toInt()} 0 0;"
            }
            (1..16).forEach { step ->
                val note = pattern.getNote(step)
                if (note != null) stepVoiceLabels[step]?.add(soundLabel)
                label(note?.noteText ?: "·") {
                    prefWidth = sz.stepWidth
                    alignment = Pos.CENTER
                    styleClass.add(if (note != null) "step-active" else "step-inactive")
                    accessibleText = if (note != null) "Note ${note.noteText} au step $step" else "Step $step inactif"
                    stepLabels[step]?.add(this)
                }
                if (step % 4 == 0 && step < 16) {
                    label("") { prefWidth = sz.beatSeparatorWidth; styleClass.add("beat-separator") }
                }
            }
        }

        startStepHighlightTimer()

        separator()

        label("Comment programmer sur le PO-14 Sub") { styleClass.add("h2") }
        label("1. Sélectionner Pattern ${pattern.number}") { styleClass.add("voice-label") }
        label("2. Maintenir Sound et appuyer sur ${pattern.sound.number} pour choisir ${pattern.sound.displayName}") { styleClass.add("voice-label") }
        label("3. Appuyer sur Write, puis pour chaque step actif : maintenir le step et tourner le bouton A jusqu'à la note indiquée") { styleClass.add("voice-label") }

        val sharpSteps = (1..16).mapNotNull { step -> pattern.getNote(step)?.let { step to it } }.filter { it.second.halfToneUp }
        if (sharpSteps.isNotEmpty()) {
            label("4. Ces steps nécessitent une touche noire : en lecture, maintenir Style/FX et appuyer sur 15 (\"half note up\") exactement sur le step, ou l'enregistrer en direct (maintenir Write + appuyer sur le step au bon moment) :") {
                styleClass.add("voice-label")
                isWrapText = true
            }
            sharpSteps.forEach { (step, note) ->
                label("   - Step $step -> ${note.noteText}") { styleClass.add("voice-label") }
            }
        }
    }
```

- [ ] **Step 4: Compile the project**

Run: `./gradlew compileKotlin`
Expected: Compiles cleanly.

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternDetailView.kt
git commit -m "Render PO-14 note grid and live half-tone-up instructions in the pattern detail view"
```

---

### Task 11: Wire PO-14 creation and editing into `MainView`

**Files:**
- Modify: `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/MainView.kt`

**Interfaces:**
- Consumes: `NewMelodicPatternDialog` (Task 9), `PO12Pattern`, `PO14Pattern` (Task 1-2).

No unit test — matches the existing convention that `MainView.kt` has no test file. Verified manually in Task 12.

- [ ] **Step 1: Add imports**

Alongside the existing imports at the top of the file:

```kotlin
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
```

- [ ] **Step 2: Add a PO-14 creation entry point and branch edit by pattern type**

Replace:

```kotlin
    private fun createNewPattern() {
        val result = NewPatternDialog().showMultiBar() ?: return
        controller.activeDialogResult = result
        controller.createPattern(result.firstPattern)
    }

    private fun generatePattern() {
        val pattern = GeneratePatternDialog().show() ?: return
        controller.createPattern(pattern)
    }

    private fun editSelectedPattern() {
        val summary = controller.selectedSummary.value ?: return
        val updatedPattern = NewPatternDialog(summary.pattern).show() ?: return
        controller.updatePattern(summary.file, updatedPattern)
    }
```

with:

```kotlin
    private fun createNewPattern() {
        val result = NewPatternDialog().showMultiBar() ?: return
        controller.activeDialogResult = result
        controller.createPattern(result.firstPattern)
    }

    private fun createNewMelodicPattern() {
        val pattern = NewMelodicPatternDialog().show() ?: return
        controller.createPattern(pattern)
    }

    private fun generatePattern() {
        val pattern = GeneratePatternDialog().show() ?: return
        controller.createPattern(pattern)
    }

    private fun editSelectedPattern() {
        val summary = controller.selectedSummary.value ?: return
        when (val pattern = summary.pattern) {
            is PO12Pattern -> {
                val updated = NewPatternDialog(pattern).show() ?: return
                controller.updatePattern(summary.file, updated)
            }
            is PO14Pattern -> {
                val updated = NewMelodicPatternDialog(pattern).show() ?: return
                controller.updatePattern(summary.file, updated)
            }
        }
    }
```

- [ ] **Step 3: Add the menu item**

Replace:

```kotlin
                        MenuItem("Nouveau pattern").apply {
                            accelerator = KeyCombination.keyCombination("Ctrl+N")
                            setOnAction { createNewPattern() }
                        },
                        MenuItem("Générer un pattern...").apply {
```

with:

```kotlin
                        MenuItem("Nouveau pattern PO-12 (Rhythm)").apply {
                            accelerator = KeyCombination.keyCombination("Ctrl+N")
                            setOnAction { createNewPattern() }
                        },
                        MenuItem("Nouveau pattern PO-14 (Sub)").apply {
                            accelerator = KeyCombination.keyCombination("Ctrl+Shift+N")
                            setOnAction { createNewMelodicPattern() }
                        },
                        MenuItem("Générer un pattern...").apply {
```

- [ ] **Step 4: Build the whole project**

Run: `./gradlew build`
Expected: Compiles cleanly and the full test suite passes (this is the first point in the plan where every file compiles together — Tasks 6-10 left the desktop layer in an intentionally broken intermediate state, resolved by this task).

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/MainView.kt
git commit -m "Add PO-14 pattern creation menu entry and dispatch pattern editing by device type"
```

---

### Task 12: Manual end-to-end verification

**Files:** none (verification only)

- [ ] **Step 1: Launch the app**

Run: `./gradlew run`
Expected: The app window opens without errors.

- [ ] **Step 2: Create a PO-14 pattern from a template**

In the app: File -> "Nouveau pattern PO-14 (Sub)". Enter a name, pick the "Acid 16th Bassline" template from the Template dropdown and click "Charger", then Save.
Expected: The dialog closes, the new pattern appears in the pattern list, and its note grid pre-fills with the template's steps (verify against `BuiltInPo14Templates.ACID_16TH` in Task 7).

- [ ] **Step 3: Verify the detail view and saved file**

Select the new pattern in the list.
Expected: `PatternDetailView` shows "PO-14", the BPM, the sound name, a 16-step note grid, and programming instructions including a "half note up" section for step 7 (the template's sharp step).

Run: `Get-Content patterns/acid-16th-bassline.md` (adjust filename to whatever was generated) — or open the file directly.
Expected: The file has `device: "PO-14"` in the frontmatter, a `**Sound:**` line, a `Note:` grid with `D#2` at step 7, and instructions mentioning effect 15.

- [ ] **Step 4: Verify playback**

Click Play on the selected PO-14 pattern.
Expected: Audio plays a bass line (not drum hits), the step highlight in the detail view advances across the 16 steps, and Stop halts it cleanly.

- [ ] **Step 5: Verify editing**

Select the pattern, click Edit.
Expected: `NewMelodicPatternDialog` opens pre-filled with the saved notes, octaves and the sharp checkbox on step 7. Change one note, Save.
Expected: The change persists (reload the file or reselect the pattern to confirm).

- [ ] **Step 6: Verify PO-12 patterns still work unchanged**

Create a new PO-12 pattern (File -> "Nouveau pattern PO-12 (Rhythm)"), save it, select it, and click Play.
Expected: Behaves exactly as before this plan — drum grid editor, drum-voice detail view, drum playback. Confirms the PO-12 path has no regressions.

- [ ] **Step 7: Report results to the user**

Summarize what was tested and any deviations from the expected results above. Do not proceed to further work without addressing any failures found.

---
