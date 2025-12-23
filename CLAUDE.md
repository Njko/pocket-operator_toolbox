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
- **Phase 1, 2, & 3 COMPLETE & TESTED ✅**
- Pattern library includes iconic Amen Break (2 bars)
- Full pattern management suite: create, view, edit, list, validate, chain
- Ready for Phase 4 advanced features

**Version Notes:**
- Built with Java 21 (updated from original Java 17 requirement)
- Clikt 5.0 breaking changes resolved (help as override property, main as extension function)
- GridEditor uses text-based input (e.g., "1,5,9,13") instead of arrow key navigation

**Next Steps:**
1. Implement Phase 4 advanced features (Image display, OCR, MIDI export)
2. Implement Phase 5 analysis features (Similarity search, Statistics, Export formats)
3. Create more example patterns (808 patterns, classic breaks, etc.)
4. Community pattern library integration

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

**Build the project:**
```bash
./gradlew build
```

**Run the application:**
```bash
./gradlew run --args="create"
```

**Or run directly after building:**
```bash
./gradlew installDist
./build/install/po-toolbox/bin/po-toolbox create
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
- `fr.nicolaslinard.po.toolbox.commands` - CLI commands (Create, View, Edit, List, Validate, Chain)
- `fr.nicolaslinard.po.toolbox.ui` - Interactive UI components (GridEditor)
- `fr.nicolaslinard.po.toolbox.io` - File I/O (MarkdownWriter, MarkdownParser, TextNotationParser)
- `fr.nicolaslinard.po.toolbox.validation` - Pattern validation utilities

**Important Design Notes:**
- PO12-specific models (PO12Pattern, PO12DrumVoice) are explicitly named to support future PO models (PO-14 Sub, PO-16 Factory, etc.)
- Markdown files are human-readable without the tool - GitHub browsing friendly
- GridEditor uses text-based input for reliability across different terminal environments
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

**Phase 5 (Analysis & Export) - IN PROGRESS (TDD Approach):**
- ✅ **Phase 5.1: Pattern Similarity** - COMPLETED (18 tests, all passing)
- ✅ **Phase 5.2: Pattern Statistics** - COMPLETED (20 tests, all passing)
- ⏸️ **Phase 5.3: Export Formats** - PENDING (JSON, CSV export)

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

1. **RED** - Write a failing test first
   - Define expected behavior through tests
   - Tests should fail initially (no implementation yet)
   - Use descriptive test names that explain the feature

2. **GREEN** - Write minimal code to pass the test
   - Implement just enough to make the test pass
   - Don't add extra features or optimization yet
   - Focus on correctness, not perfection

3. **REFACTOR** - Improve code while keeping tests green
   - Clean up implementation
   - Extract common patterns
   - Optimize if needed
   - All tests must still pass

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
├── analysis/        # Analysis tests (similarity, statistics)
└── integration/     # Integration tests (end-to-end workflows)
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

### Phase 4 & 5 TDD Checklist

Before implementing each Phase 4+ feature:

- [ ] Write test cases defining expected behavior
- [ ] Verify tests fail (RED)
- [ ] Implement minimal code to pass tests (GREEN)
- [ ] Refactor and optimize (REFACTOR)
- [ ] All tests passing before moving to next feature
- [ ] Update CLAUDE.md with progress

**Phase 4 Features (Advanced):**
1. Image display for manual transcription
2. OCR integration hooks
3. MIDI export

**Phase 5 Features (Analysis & Export):**
1. Pattern similarity search
2. Pattern statistics and analysis
3. Additional export formats (JSON, CSV)
