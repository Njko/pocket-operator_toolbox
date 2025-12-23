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
- Phase 1 & 2 complete with full test coverage
- Pattern library includes iconic Amen Break (2 bars)
- Ready for Phase 3 implementation

**Version Notes:**
- Built with Java 21 (updated from original Java 17 requirement)
- Clikt 5.0 breaking changes resolved (help as override property, main as extension function)
- GridEditor uses text-based input (e.g., "1,5,9,13") instead of arrow key navigation

**Next Steps:**
1. Implement Phase 2 features (view, edit, list commands)
2. Add text notation parser for quicker pattern entry
3. Create more example patterns (Amen Break, etc.)

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
- `fr.nicolaslinard.po.toolbox.models` - Data models (PO12Pattern, PO12DrumVoice, PatternMetadata)
- `fr.nicolaslinard.po.toolbox.commands` - CLI commands (CreateCommand)
- `fr.nicolaslinard.po.toolbox.ui` - Interactive UI components (GridEditor)
- `fr.nicolaslinard.po.toolbox.io` - File I/O (MarkdownWriter)

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

**Phase 3 (Management) - PENDING:**
- ListCommand with filtering by genre/difficulty
- Pattern validation
- PatternChain support (multi-bar phrases)
- Enhanced terminal UI (colors, better rendering)

**Phase 4 (Advanced) - FUTURE:**
- Image display for manual transcription
- OCR integration hooks for automatic notation parsing
- MIDI export
- Pattern search by similarity

### Markdown Output Format

Patterns are saved as human-readable markdown files with:
- YAML frontmatter (metadata: name, BPM, genre, difficulty, source, author, date)
- ASCII grid visualization with `[●]` for active steps, `[ ]` for inactive
- Step-by-step PO-12 programming instructions
- Notes section for tempo and attribution

**Example output:** See `patterns/` directory after running `create` command
