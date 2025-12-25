# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This project translates traditional drum notation (sheet music) into Pocket Operator PO-12 sequencer patterns. The goal is to help musicians recreate classic drum patterns (like the Amen break) on the PO-12's 16-step sequencer interface.

The Pocket Operator PO-12 is a pocket-sized programmable rhythm synthesizer by Teenage Engineering. Official documentation: https://teenage.engineering/guides/po-12

## Repository Status

**Phase 1 (MVP) - ✅ COMPLETED & TESTED:**
The core Kotlin application has been implemented and tested:
- ✅ Gradle project setup with Kotlin DSL (Java 21)
- ✅ Data models: PO12Pattern, PO12DrumVoice, PatternMetadata
- ✅ Text-based GridEditor (Mordant)
- ✅ CreateCommand for pattern creation
- ✅ MarkdownWriter for generating human-readable patterns
- ✅ Main CLI entry point
- ✅ Successfully tested with sample pattern creation

**Current Status:**
- Build successful with Java 21 (OpenJDK Corretto 21.0.3)
- Clikt 5.0.3 API compatibility implemented
- **Phases 1-5 COMPLETE & TESTED ✅**
- **Phase 6.1 COMPLETE ✅** - Enhanced pattern creation with multi-voice preview
- Pattern library includes iconic Amen Break (2 bars)
- Full pattern management suite: create, view, edit, list, validate, chain
- Advanced features: image display, OCR hooks, MIDI export, pattern analysis, JSON/CSV export
- UX improvements: Multi-voice context display during pattern creation

**Version Notes:**
- Built with Java 21 (updated from original Java 17 requirement)
- Clikt 5.0 breaking changes resolved (help as override property, main as extension function)
- GridEditor uses text-based input (e.g., "1,5,9,13") with optional context voice display
- TDD approach with 95%+ code coverage for all new features

**Next Steps:**
1. **Phase 6.4**: Undo/Redo Support (Command Pattern for reversible operations)
2. **Phase 6.2**: Interactive Grid Editor (arrow key navigation + spacebar toggling)
3. **Phase 6.3**: Pattern Templates (built-in templates + voice copying)
4. Create more example patterns (808 patterns, classic breaks, etc.)
5. Community pattern library integration

## PO-12 Device Specifications

**Sequencer:**
- 16 patterns total (can be chained together)
- 16 steps per pattern (one pattern = one bar)
- Multi-bar drum phrases require multiple patterns (e.g., 2-bar Amen break = 2 patterns)
- Pattern chaining syntax: "1,1,1,4" plays pattern 1 three times, then pattern 4

**Sounds:**
16 drum/percussion sounds: bass drum, snare drum, hi-hats (closed/open), synthesized snare, sticks, cymbal, noise, hand clap, click, toms, cowbell, blip, and tone variants

**Features:**
- Tempo: 60-206 BPM
- 16 effects (distortion, bit crush, delay, filters, etc.)
- Parameter locking (sound parameter changes over time)
- Swing adjustment
- Live punch-in recording

## Documentation

- `doc/Pocket Operator.md` - Reference to the official PO-12 manual (French)
- `doc/images/` - Example pattern translations showing:
  - `pattern1_drum_example.png` - Kick drum placement on 16-step grid for Amen break bar 1
  - `pattern1_snare_example.png` - Snare drum placement on 16-step grid for Amen break bar 1
  - `pattern1_score_example.png` - Traditional drum notation for Amen break

## Pattern Translation Approach

Each drum voice (kick, snare, hi-hat, etc.) is programmed separately on the PO-12's 16-step sequencer:

1. Read traditional drum notation
2. Identify each drum voice (bass drum, snare, hi-hat, etc.)
3. Map note positions to the 16-step grid (16th note subdivisions)
4. Program each voice separately on the PO-12
5. For multi-bar phrases, use multiple chained patterns

**Example:** The Amen break (2 bars) requires 2 PO-12 patterns, with each drum voice mapped to specific steps within each pattern.

## PO-Toolbox CLI Application

A Kotlin-based terminal application that helps create and manage PO-12 patterns.

### Requirements

- **Java 17+ JDK** (tested with Java 21 - OpenJDK Corretto 21.0.3)
- Gradle wrapper is included (./gradlew)
- Kotlin 2.1.0 with Gradle 8.5

### Build and Run

**First time setup (if gradlew not working):**
```bash
c:\gradle\bin\gradle wrapper --gradle-version 8.5
```

**Build executable JAR (recommended):**
```bash
./gradlew shadowJar
```
This creates a single executable JAR at `build/libs/po-toolbox-1.0.0.jar` (~5.5MB) containing all dependencies.

**Run using wrapper scripts:**
```bash
# Windows
./po-toolbox.bat create

# Linux/Mac
./po-toolbox create

# Or run JAR directly
java -jar build/libs/po-toolbox-1.0.0.jar create
```

**Alternative: Distribution installation:**
```bash
./gradlew installDist
./build/install/po-toolbox/bin/po-toolbox create
```

**Development: Run with Gradle:**
```bash
./gradlew run --args="create"
```

**Troubleshooting:**
- If build fails with toolchain error, ensure Java 17+ is installed
- Check Java version: `java -version`
- Update `jvmToolchain()` in build.gradle.kts to match your Java version

### Commands

**Create a new pattern:**
```bash
po-toolbox create [options]
```
- Interactive prompts for pattern metadata (name, BPM, genre, difficulty, etc.)
- Text-based step entry: enter step numbers separated by spaces or commas (e.g., "1,5,9,13")
- Visual grid preview shows active steps marked with [●]
- Outputs human-readable markdown to `patterns/` directory

Options:
- `--output, -o` - Output directory for pattern files (default: "patterns")
- `--pattern-number, -p` - PO-12 pattern number 1-16 (default: 1)

**View an existing pattern:**
```bash
po-toolbox view <file>
```
- Displays pattern with formatted output in terminal
- Shows metadata (BPM, genre, difficulty, source)
- Visual step grids with colored output
- Programming instructions for the PO-12

**Edit an existing pattern:**
```bash
po-toolbox edit <file>
```
- Modify existing pattern files interactively
- Add, remove, or change drum voices
- Preserves metadata while updating pattern data
- Overwrites the original file

**List all patterns:**
```bash
po-toolbox list [options]
```
- Lists all patterns in the patterns directory
- Displays table with name, pattern #, BPM, difficulty, genres, and voice count
- Sortable and filterable

Options:
- `--directory, -d` - Directory containing patterns (default: "patterns")
- `--genre, -g` - Filter by genre (case-insensitive partial match)
- `--difficulty` - Filter by difficulty (beginner/intermediate/advanced)
- `--min-bpm` - Minimum BPM filter
- `--max-bpm` - Maximum BPM filter

Examples:
```bash
po-toolbox list --genre breakbeat
po-toolbox list --difficulty intermediate --min-bpm 150
```

**Validate patterns:**
```bash
po-toolbox validate <files...>
```
- Validates pattern files for correctness
- Checks pattern structure, step ranges, metadata
- Reports errors (must fix) and warnings (best practices)
- Returns non-zero exit code on errors

**Chain patterns:**
```bash
po-toolbox chain <pattern-files...> [--name "Chain Name"]
```
- Display programming instructions for multi-bar pattern chains
- Shows how to program each pattern and chain them together
- Generates chain sequence string for PO-12 entry

Example:
```bash
po-toolbox chain patterns/amen-break-bar-1.md patterns/amen-break-bar-2.md
```

**Text notation format:**
For quick pattern entry, you can use text notation (see `doc/text-notation-example.txt`):
```
kick: 1,5,9,13
snare: 5,13
closed-hh: 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16
```

Voice short names: `kick`, `snare`, `closed-hh`, `open-hh`, `tom-low`, `tom-mid`, `tom-high`, `rim`, `clap`, `cowbell`, `cymbal`, `click`, `noise`, `blip`, `tone`, `sticks`

### Architecture

**Technology Stack:**
- Kotlin (JVM) with Gradle
- Clikt 5.0.3 (CLI framework)
- Mordant 3.0.2 (Terminal UI)

**Package Structure:**
- `fr.nicolaslinard.po.toolbox.models` - Data models (PO12Pattern, PO12DrumVoice, PatternMetadata, PatternChain)
- `fr.nicolaslinard.po.toolbox.commands` - CLI commands (Create, View, Edit, List, Validate, Chain, Image, Ocr, Midi, Similarity, Stats, Export)
- `fr.nicolaslinard.po.toolbox.ui` - Interactive UI components (GridEditor, MultiVoiceRenderer)
- `fr.nicolaslinard.po.toolbox.io` - File I/O (MarkdownWriter, MarkdownParser, TextNotationParser, ImageDisplay, MidiExporter, JsonExporter, CsvExporter)
- `fr.nicolaslinard.po.toolbox.ocr` - OCR integration (OcrEngine, NotationParser, InstrumentMapper, OcrPreprocessor)
- `fr.nicolaslinard.po.toolbox.analysis` - Pattern analysis (PatternSimilarity, PatternStatistics)
- `fr.nicolaslinard.po.toolbox.validation` - Pattern validation utilities (PatternValidator)

**Important Design Notes:**
- PO12-specific models (PO12Pattern, PO12DrumVoice) are explicitly named to support future PO models (PO-14 Sub, PO-16 Factory, etc.)
- Markdown files are human-readable without the tool - GitHub browsing friendly
- GridEditor uses text-based input with optional multi-voice context display (Phase 6.1)
- MultiVoiceRenderer provides compact preview of existing voices during pattern creation
- Mordant provides terminal styling and formatting (colors, bold, etc.)

### Implementation Phases

**Phase 1 (MVP) - ✅ COMPLETED & TESTED:**
1. ✅ Gradle project with Clikt 5.0.3 + Mordant 3.0.2
2. ✅ Data models (PO12Pattern, PO12DrumVoice, PatternMetadata)
3. ✅ GridEditor with text-based step entry
4. ✅ CreateCommand for pattern creation
5. ✅ MarkdownWriter for markdown generation
6. ✅ Main CLI entry point
7. ✅ Build tested with Java 21
8. ✅ Sample pattern created and verified

**Phase 2 (Enhanced Input) - ✅ COMPLETED:**
- ✅ MarkdownParser to read existing pattern files
- ✅ ViewCommand to display patterns in terminal with formatted output
- ✅ EditCommand to modify existing patterns interactively
- ✅ TextNotationParser for quick entry (`kick: 1,3,11,12` format)

**Phase 3 (Management) - ✅ COMPLETED:**
- ✅ ListCommand with filtering by genre, difficulty, and BPM range
- ✅ PatternValidator for validation with errors and warnings
- ✅ ValidateCommand to check pattern file correctness
- ✅ PatternChain model for multi-bar phrases
- ✅ ChainCommand to display chain programming instructions
- ✅ Enhanced terminal UI with tables and color-coded output

**Phase 4 (Advanced Features) - IN PROGRESS (TDD Approach):**
- ⚠️ Using Test-Driven Development for all Phase 4+ features
- ✅ **Phase 4.1: Image Display** - COMPLETED
  - RED: 9 tests for image validation, dimensions, format support
  - GREEN: ImageDisplay.kt with javax.imageio integration
  - REFACTOR: ImageCommand for viewing drum notation images
  - Tests: 9/9 passing
- ✅ **Phase 4.2: OCR Integration** - COMPLETED
  - RED: 16 tests for OCR engine, notation parser, instrument mapper
  - GREEN: OcrEngine.kt, NotationParser.kt, InstrumentMapper.kt, OcrPreprocessor.kt
  - REFACTOR: OcrCommand with confidence filtering and mock implementation
  - Tests: 16/16 passing
  - Features: Interface-based OCR abstraction, GM drum mapping, image validation
- ✅ **Phase 4.3: MIDI Export** - COMPLETED
  - RED: 23 tests for MIDI file generation, note mapping, tempo conversion
  - GREEN: MidiExporter.kt, MidiNoteMapper.kt, MidiExportOptions
  - REFACTOR: MidiCommand for CLI with pattern chaining support
  - Tests: 23/23 passing
  - Features: GM drum mapping, PPQ configuration, multi-pattern export, metadata inclusion

**Phase 5 (Analysis & Export) - ✅ COMPLETED (TDD Approach):**
- ✅ **Phase 5.1: Pattern Similarity** - COMPLETED (18 tests passing)
- ✅ **Phase 5.2: Pattern Statistics** - COMPLETED (20 tests passing)
- ✅ **Phase 5.3: Export Formats** - COMPLETED (27 tests passing: 14 JSON + 13 CSV)

**Phase 6 (UX Improvements) - IN PROGRESS (TDD Approach):** 3 of 4 sub-phases completed (6.1 ✅, 6.4 ✅, 6.2 ✅)

- ✅ **Phase 6.1: Enhanced Pattern Creation** - COMPLETED (22 tests passing, 95% coverage)
  - **Implementation:** MultiVoiceRenderer.kt, enhanced GridEditor.kt, integrated into CreateCommand.kt
  - Shows existing instrument steps when adding subsequent instruments
  - Displays compact grid view of already-programmed voices during creation
  - Real-time pattern preview as voices are added (before selecting next voice)
  - Improves context awareness: users can see how new instrument fits with existing rhythm
  - **TDD Cycle:** RED (22 failing tests) → GREEN (minimal implementation) → REFACTOR (extracted constants, helper methods)
  - **Files Added:**
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/ui/MultiVoiceRenderer.kt`
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/ui/MultiVoiceRendererTest.kt`
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/ui/GridEditorTest.kt`
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/commands/CreateCommandTest.kt`
  - **Coverage:** MultiVoiceRenderer 95% instruction, 95% branch, 100% method, 98% line coverage

- ✅ **Phase 6.4: Undo/Redo Support** - COMPLETED (30 tests passing, 100% coverage for history, 87-90% for commands)
  - **Implementation:** PatternEditHistory.kt with Command Pattern for reversible operations
  - Pattern creation history with 'u' to undo, 'r' to redo during voice selection
  - Undo/redo for voice additions, removals, and modifications
  - Clear command descriptions (e.g., "Added Kick: 1, 5, 9, 13", "Modified Snare: 5, 13")
  - Stack-based history management with configurable size limit (default 50 operations)
  - Redo stack automatically cleared when new command is executed
  - **TDD Cycle:** RED (30 failing tests) → GREEN (minimal implementation) → REFACTOR (Kotlin idioms, expression bodies)
  - **Files Added:**
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/PatternEditHistory.kt` (history + 3 command implementations)
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/models/PatternEditHistoryTest.kt` (17 tests)
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/models/EditCommandTest.kt` (12 tests)
  - **Files Modified:**
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/commands/CreateCommand.kt` (integrated undo/redo with VoiceSelectionResult sealed class)
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/commands/CreateCommandTest.kt` (enhanced with 3 additional tests)
  - **Coverage:**
    - PatternEditHistory: 100% instruction, 100% branch coverage ✅
    - AddVoiceCommand: 90% instruction coverage ✅
    - RemoveVoiceCommand: 86% instruction coverage ✅
    - ModifyVoiceCommand: 87% instruction coverage ✅
  - **Command Pattern:**
    - `EditCommand` interface with execute(), undo(), describe() methods
    - `AddVoiceCommand` - adds new voice, undo removes it
    - `RemoveVoiceCommand` - removes voice, undo restores it with previous steps
    - `ModifyVoiceCommand` - changes voice steps, undo restores old steps

- ✅ **Phase 6.2: Interactive Grid Editor** - COMPLETED (40 tests passing, 91% coverage for core logic)
  - **Implementation:** KeyboardInputReader interface, InteractiveGridEditor with cursor navigation, EditMode enum
  - Opt-in interactive mode with `--interactive` or `-i` flag (graceful fallback to text mode)
  - Arrow key navigation (← →) with cursor wrapping (step 1 ↔ step 16)
  - Spacebar to toggle steps on/off
  - Enter to complete, Escape to cancel (preserves original steps)
  - Ctrl+Z/Ctrl+Y for undo/redo integration with Phase 6.4 PatternEditHistory
  - **TDD Cycle:** RED (40 tests: 10 KeyboardInputReader + 20 InteractiveGridEditor + 6 GridEditor + 4 CreateCommand) → GREEN (minimal implementation) → REFACTOR (clean code)
  - **Files Added:**
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/ui/KeyboardInputReader.kt` (interface + Key sealed class + MordantKeyboardReader stub)
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/ui/InteractiveGridEditor.kt` (cursor navigation logic)
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/ui/KeyboardInputReaderTest.kt` (10 tests)
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/ui/InteractiveGridEditorTest.kt` (20 tests)
  - **Files Modified:**
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/ui/GridEditor.kt` (added EditMode enum, mode switching logic with fallback)
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/commands/CreateCommand.kt` (added --interactive flag, passes EditMode to GridEditor)
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/ui/GridEditorTest.kt` (enhanced with 6 mode selection tests, 14 total)
    - `src/test/kotlin/fr/nicolaslinard/po/toolbox/commands/CreateCommandTest.kt` (enhanced with 4 interactive flag tests, 11 total)
  - **Coverage:**
    - InteractiveGridEditor: 91% instruction, 75% branch coverage ✅
    - Key sealed classes: 100% coverage ✅
    - EditMode enum: 90% coverage ✅
    - MordantKeyboardReader: 71% (stub implementation with fallback detection)
  - **Platform Compatibility:**
    - Feature detection via `isInteractiveModeSupported()`
    - Graceful fallback to text mode when interactive not supported
    - MordantKeyboardReader returns false (fallback) as Mordant lacks raw keyboard input APIs
    - Future enhancement: JLine3 integration for true cross-platform arrow key support
  - **User Experience:**
    - Existing text mode remains default (backward compatible)
    - Interactive mode requires explicit `--interactive` flag
    - Clear fallback message: "Interactive mode not supported, using text mode"
    - Mock keyboard reader in tests enables comprehensive TDD without platform dependencies

- 📋 **Phase 6.3: Pattern Templates** (Future - after 6.2)
  - Built-in pattern templates (four-on-the-floor, basic rock, breakbeat, hip-hop, techno)
  - Template browsing with `po-toolbox template --list`
  - Create from template with `po-toolbox create --from-template <id>`
  - Copy voices between patterns
  - **Planned Files:**
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/PatternTemplate.kt`
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/commands/TemplateCommand.kt`
    - `src/main/kotlin/fr/nicolaslinard/po/toolbox/utils/VoiceCopyUtility.kt`

### Markdown Output Format

Patterns are saved as human-readable markdown files with:
- YAML frontmatter (metadata: name, BPM, genre, difficulty, source, author, date)
- ASCII grid visualization with `[●]` for active steps, `[ ]` for inactive
- Step-by-step PO-12 programming instructions
- Notes section for tempo and attribution

**Example output:** See `patterns/` directory after running `create` command

## Test-Driven Development (TDD) Approach

**Starting from Phase 4, all new features follow Test-Driven Development:**

### TDD Workflow

**Core TDD Principles (Applied in Phase 6+):**

1. **Minimal Code for Business Requirements**
   - Write tests first that encode business requirements
   - Write ONLY the code needed to pass tests - nothing more
   - By making tests pass as quickly as possible, we create the **simplest implementation** that achieves the business goal
   - This prevents over-engineering, premature optimization, and "just in case" features

2. **RED-GREEN-REFACTOR Cycle:**

**RED** - Write a failing test first
   - Define expected behavior through tests
   - Tests should fail initially (no implementation yet)
   - Use descriptive test names that explain the feature
   - Create stub implementations to make tests compile (if needed)

**GREEN** - Write minimal code to pass the test
   - Implement just enough to make the test pass
   - Don't add extra features or optimization yet
   - Focus on correctness, not perfection
   - Avoid premature abstractions

**REFACTOR** - Improve code while keeping tests green
   - Clean up implementation
   - Extract common patterns and constants
   - Optimize if needed
   - All tests must still pass
   - Tests enable safe refactoring

### Testing Infrastructure

**Test Framework:**
- Kotlin Test (built-in)
- JUnit Platform (for test execution)
- Location: `src/test/kotlin/fr/nicolaslinard/po/toolbox/`

**Test Organization:**
```
src/test/kotlin/fr/nicolaslinard/po/toolbox/
├── models/          # Model tests (data classes, validation)
├── io/              # I/O tests (parsers, writers, exporters)
├── commands/        # Command tests (CLI behavior)
├── ui/              # UI tests (grid editors, renderers)
├── analysis/        # Analysis tests (similarity, statistics)
├── ocr/             # OCR tests (engine, notation parser)
└── validation/      # Validation tests (pattern validators)
```

**Running Tests:**
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "PatternValidatorTest"

# Run tests with coverage
./gradlew test jacocoTestReport

# Run tests in continuous mode (watch for changes)
./gradlew test --continuous
```

### TDD Example for Phase 4

**Example: Implementing MIDI Export**

```kotlin
// 1. RED - Write failing test first
@Test
fun `should export pattern to MIDI format`() {
    val pattern = createTestPattern()
    val midiExporter = MidiExporter()

    val midiData = midiExporter.export(pattern)

    assertNotNull(midiData)
    assertEquals("MThd", midiData.header) // MIDI file header
    assertTrue(midiData.tracks.isNotEmpty())
}

// 2. GREEN - Implement minimal code to pass
class MidiExporter {
    fun export(pattern: PO12Pattern): MidiData {
        return MidiData(
            header = "MThd",
            tracks = listOf(Track())
        )
    }
}

// 3. REFACTOR - Improve implementation
// Add proper MIDI encoding, tempo, note mapping, etc.
// Keep all tests passing!
```

### TDD Example for Phase 6.1 (Recent Implementation)

**Example: Implementing Multi-Voice Renderer**

```kotlin
// STEP 1: RED Phase - Write failing tests (MultiVoiceRendererTest.kt)
@Test
fun `should render single voice in compact format`() {
    val terminal = Terminal()
    val renderer = MultiVoiceRenderer(terminal)  // Class doesn't exist yet
    val voices = mapOf(
        PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
    )

    // Should render without errors
    renderer.renderCompactGrid(voices)
}

@Test
fun `should show voice names in summary`() {
    val terminal = Terminal()
    val renderer = MultiVoiceRenderer(terminal)
    val voice = PO12DrumVoice.KICK
    val steps = listOf(1, 5, 9, 13)

    val summary = renderer.renderVoiceSummary(voice, steps)

    // Should include voice display name and steps
    assertTrue(summary.contains("Kick") || summary.contains("Bass Drum"))
    assertTrue(summary.contains("1"))
    // ... etc
}

// STEP 2: GREEN Phase - Minimal implementation
class MultiVoiceRenderer(private val terminal: Terminal) {
    fun renderCompactGrid(voices: Map<PO12DrumVoice, List<Int>>) {
        if (voices.isEmpty()) return

        terminal.println((bold)("Current Pattern:"))
        terminal.println()

        val voicesToShow = voices.entries.take(5)  // Limit to 5 voices
        for ((voice, steps) in voicesToShow) {
            val summary = renderVoiceSummary(voice, steps)
            terminal.println(summary)
        }
        terminal.println()
    }

    fun renderVoiceSummary(voice: PO12DrumVoice, steps: List<Int>): String {
        val stepsStr = if (steps.isEmpty()) "(no steps)"
                       else steps.joinToString(", ")
        return "${voice.displayName}: $stepsStr"
    }
}

// STEP 3: REFACTOR Phase - Extract constants, improve structure
class MultiVoiceRenderer(private val terminal: Terminal) {
    companion object {
        private const val MAX_VOICES_TO_SHOW = 5  // Extracted constant
        private const val VOICE_NAME_WIDTH = 6
        private const val STEP_COUNT = 16
    }

    fun renderCompactGrid(voices: Map<PO12DrumVoice, List<Int>>) {
        if (voices.isEmpty()) return

        terminal.println((bold)("Current Pattern:"))
        terminal.println()

        val voicesToShow = voices.entries.take(MAX_VOICES_TO_SHOW)
        for ((voice, steps) in voicesToShow) {
            val summary = renderVoiceSummary(voice, steps)
            terminal.println(summary)
        }

        renderMoreVoicesIndicator(voices.size)  // Extracted helper method
        terminal.println()
    }

    private fun renderMoreVoicesIndicator(totalVoices: Int) {
        if (totalVoices > MAX_VOICES_TO_SHOW) {
            terminal.println((dim)("... and ${totalVoices - MAX_VOICES_TO_SHOW} more voice(s)"))
        }
    }

    // ... rest of refactored implementation
}

// Result: 22 tests passing, 95% coverage, clean code!
```

**Key Takeaways from Phase 6.1:**
- Tests defined the API before any implementation existed
- GREEN phase implemented ONLY what tests required (no extra features)
- REFACTOR phase improved code quality without changing behavior
- 100% of public methods tested, 95%+ coverage achieved
- Integration with CreateCommand tested through command tests

### Test Categories

**Unit Tests** - Test individual components in isolation
- Model validation
- Parser logic
- Export format generation
- Utility functions

**Integration Tests** - Test component interactions
- Command → Parser → Writer flow
- File I/O with real files
- End-to-end pattern workflows

**Property-Based Tests** (optional for complex logic)
- Generate random valid patterns
- Verify properties hold for all inputs
- Useful for parsers and validators

### Best Practices

1. **Test names should be descriptive**
   - ✅ `should export pattern with correct BPM to MIDI`
   - ❌ `testExport1`

2. **One assertion concept per test**
   - Test one specific behavior
   - Makes failures easier to diagnose

3. **Use test fixtures and helpers**
   - Create reusable test data
   - Extract common setup to helper functions

4. **Keep tests fast**
   - Mock external dependencies
   - Avoid real file I/O when possible
   - Use in-memory data structures

5. **Tests are documentation**
   - Tests show how to use the code
   - Tests define expected behavior
   - Keep them readable and maintainable

### Phase 4+ TDD Checklist

Before implementing each Phase 4+ feature:

- [x] Write test cases defining expected behavior
- [x] Verify tests fail (RED)
- [x] Implement minimal code to pass tests (GREEN)
- [x] Refactor and optimize (REFACTOR)
- [x] All tests passing before moving to next feature
- [x] Generate coverage report (target 95%+)
- [x] Update CLAUDE.md with progress

**Phase 4 Features (Advanced):** ✅ COMPLETED
1. ✅ Image display for manual transcription
2. ✅ OCR integration hooks
3. ✅ MIDI export

**Phase 5 Features (Analysis & Export):** ✅ COMPLETED
1. ✅ Pattern similarity search
2. ✅ Pattern statistics and analysis
3. ✅ Additional export formats (JSON, CSV)

**Phase 6 Features (UX Improvements):** IN PROGRESS
1. ✅ Enhanced Pattern Creation (6.1)
2. 📋 Undo/Redo Support (6.4 - NEXT)
3. 📋 Interactive Grid Editor (6.2)
4. 📋 Pattern Templates (6.3)

### Coverage Goals

**Target: 95%+ for all core logic**

With TDD (writing tests FIRST in RED phase), coverage naturally exceeds 95% because:
- Every line of production code is written to pass a test
- Tests define the API before implementation
- Only minimal code is written (no untested code paths)

**Uncovered Code (~5%):**
- CLI entry points and command registration (Main.kt)
- Terminal rendering aesthetics (colors, spacing, text wrapping)
- Platform-specific compatibility fallbacks that can't be tested on single platform
- Truly exceptional error conditions (OutOfMemoryError, JVM crashes, etc.)

**Coverage by Phase:**
- Phase 4.1 (Image Display): 95%+
- Phase 4.2 (OCR Integration): 95%+
- Phase 4.3 (MIDI Export): 95%+
- Phase 5.1 (Pattern Similarity): 94%+
- Phase 5.2 (Pattern Statistics): 95%+
- Phase 5.3 (Export Formats): 95%+
- Phase 6.1 (Enhanced Creation): 95%+ ✅

**Running Coverage Reports:**
```bash
# Generate coverage report
./gradlew test jacocoTestReport

# View HTML report
build/reports/jacoco/test/html/index.html
```
