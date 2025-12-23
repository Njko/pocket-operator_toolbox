# PO-Toolbox 🥁

A comprehensive command-line tool for creating, managing, and analyzing Pocket Operator PO-12 drum patterns. Translate traditional drum notation into sequencer patterns, find similar beats, export to MIDI, and more.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-130%2B%20passing-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-80%25%2B-green)]()

## Features

### 🎵 Pattern Management (Phases 1-3)
- ✅ Interactive 16-step grid editor for pattern creation
- ✅ View, edit, and list pattern libraries
- ✅ Text notation input (`kick: 1,5,9,13`)
- ✅ Pattern validation with warnings and errors
- ✅ Multi-bar pattern chaining support
- ✅ Human-readable markdown storage (GitHub-friendly)

### 🚀 Advanced Features (Phase 4)
- ✅ **Image Display** - View drum notation images for manual transcription
- ✅ **OCR Integration** - Hooks for automatic notation parsing
- ✅ **MIDI Export** - Convert patterns to .mid files for DAWs

### 📊 Analysis & Export (Phase 5)
- ✅ **Pattern Similarity** - Find similar patterns based on rhythm, voices, and steps
- ✅ **Pattern Statistics** - Analyze density, complexity, and syncopation
- ✅ **JSON Export** - Programmatic data exchange
- ✅ **CSV Export** - Spreadsheet analysis (list and grid formats)

## Quick Start

### Requirements
- **Java 21** JDK (or Java 17+)
- Terminal with UTF-8 support

### Installation

```bash
# Clone the repository
git clone <repository-url>
cd pocket-operator_toolbox

# Build the project
./gradlew build

# Install distribution
./gradlew installDist

# Add to PATH (optional)
export PATH=$PATH:$(pwd)/build/install/po-toolbox/bin
```

### Your First Pattern

```bash
# Create a new pattern interactively
./build/install/po-toolbox/bin/po-toolbox create

# Follow prompts to enter:
# - Pattern name, BPM, genre, difficulty
# - Voice selection (kick, snare, hi-hat, etc.)
# - Step programming (use text input: 1,5,9,13)
```

## Usage & Examples

All examples below use the `po-toolbox` command. Adjust path as needed:
```bash
# If installed to PATH
po-toolbox <command>

# Or use full path
./build/install/po-toolbox/bin/po-toolbox <command>
```

### 📝 Pattern Creation & Management

#### Create Pattern
```bash
po-toolbox create

# Example interaction:
# Pattern name: My Four-on-Floor
# BPM: 128
# Genre: house
# Difficulty: beginner
# Voices: kick, clap, closed-hh
# kick steps: 1,5,9,13
# clap steps: 5,13
# closed-hh steps: 1,3,5,7,9,11,13,15
```

#### View Pattern
```bash
# View the Amen Break pattern
po-toolbox view patterns/amen-break-bar-1.md

# Output includes:
# - Metadata (name, BPM, genre, difficulty)
# - ASCII grid visualization
# - Voice-by-voice breakdown
# - Programming instructions for PO-12
```

#### Edit Pattern
```bash
# Modify an existing pattern
po-toolbox edit patterns/my-pattern.md

# Interactive editing of:
# - Metadata fields
# - Voice steps (text notation)
```

#### List Patterns
```bash
# List all patterns
po-toolbox list

# Filter by genre
po-toolbox list --genre breakbeat

# Filter by BPM range
po-toolbox list --min-bpm 160 --max-bpm 180

# Filter by difficulty
po-toolbox list --difficulty intermediate
```

#### Validate Pattern
```bash
# Check pattern file validity
po-toolbox validate patterns/my-pattern.md

# Checks for:
# ✓ Pattern number (1-16)
# ✓ Step range (1-16)
# ✓ Voice validity
# ✓ BPM warnings (outside 60-206)
# ✓ Best practices
```

#### Pattern Chains
```bash
# Display chaining instructions for multi-bar phrases
po-toolbox chain patterns/amen-break-bar-1.md patterns/amen-break-bar-2.md

# Output:
# Pattern Chain Programming Instructions
# Patterns: 2
# Chain sequence: 1,2
# Total bars: 2
```

### 🖼️ Image & OCR (Phase 4.1-4.2)

#### View Drum Notation Images
```bash
# List available drum notation images
po-toolbox image doc/images/

# Output shows:
# - Image files found
# - Dimensions
# - File paths for reference
```

#### OCR Pattern Recognition
```bash
# Process drum notation image with OCR
po-toolbox ocr doc/images/pattern1_score_example.png

# Features:
# - Image validation (format, dimensions)
# - OCR processing (mock implementation - ready for real OCR)
# - Confidence scoring
# - Pattern data conversion
# - Instrument mapping (BD → kick, SD → snare)

# Adjust confidence threshold
po-toolbox ocr image.png --min-confidence 0.8
```

### 🎹 MIDI Export (Phase 4.3)

#### Export Single Pattern
```bash
# Export to MIDI file
po-toolbox midi patterns/amen-break-bar-1.md

# Creates: Amen_Break_-_Bar_1.mid
# - General MIDI drum map (channel 10)
# - Proper tempo meta events
# - 96 PPQ resolution
```

#### Export Chained Patterns
```bash
# Export 2-bar Amen Break as single MIDI file
po-toolbox midi patterns/amen-break-bar-1.md patterns/amen-break-bar-2.md -o amen-break-full.mid

# Result:
# - Both patterns in sequence
# - Continuous playback
# - 303 bytes
```

#### Custom MIDI Settings
```bash
# High-resolution export with custom velocity
po-toolbox midi patterns/my-pattern.md \
  --resolution 480 \
  --velocity 90 \
  --duration 120 \
  --no-metadata
```

### 🔍 Pattern Similarity (Phase 5.1)

#### Find Similar Patterns
```bash
# Search for patterns similar to Amen Break
po-toolbox similar patterns/amen-break-bar-1.md

# Output:
# ┌──────┬────────────┬──────────────────┬────────┬───────┬─────┬──────────────┐
# │ Rank │ Similarity │ Pattern          │ Voices │ Notes │ BPM │ Difficulty   │
# ├──────┼────────────┼──────────────────┼────────┼───────┼─────┼──────────────┤
# │ 1    │ 84.3%      │ Amen Break Bar 2 │ 3      │ 18    │ 169 │ intermediate │
# └──────┴────────────┴──────────────────┴────────┴───────┴─────┴──────────────┘
#
# Detailed breakdown:
#   Overall similarity: 84.3%
#   ├─ Voice similarity: 100.0%
#   ├─ Step similarity: 69.0%
#   └─ Rhythm similarity: 83.3%
#
#   Common voices: Bass Drum, Snare, Closed Hi-Hat
```

#### Adjust Similarity Weights
```bash
# Emphasize rhythm over voice choice
po-toolbox similar patterns/my-pattern.md \
  --voice-weight 0.2 \
  --step-weight 0.2 \
  --rhythm-weight 0.6

# Find only highly similar patterns
po-toolbox similar patterns/my-pattern.md --threshold 0.9 --limit 5
```

### 📊 Pattern Statistics (Phase 5.2)

Statistics are calculated programmatically using the analyzer classes:

```kotlin
// In your code
import fr.nicolaslinard.po.toolbox.analysis.PatternStatisticsAnalyzer
import fr.nicolaslinard.po.toolbox.io.MarkdownParser

val parser = MarkdownParser()
val analyzer = PatternStatisticsAnalyzer()

val pattern = parser.parse(File("patterns/amen-break-bar-1.md"))
val stats = analyzer.analyze(pattern)

println("Total notes: ${stats.totalNotes}")
println("Density: ${stats.density}")
println("Complexity: ${stats.complexity}")
println("Syncopation: ${analyzer.calculateSyncopation(pattern)}")
```

#### Library Statistics
```kotlin
// Analyze entire pattern library
val library = patternsDir.listFiles()
    .filter { it.extension == "md" }
    .map { parser.parse(it) }

val libraryStats = analyzer.analyzeLibrary(library)

println("Total patterns: ${libraryStats.totalPatterns}")
println("Average density: ${libraryStats.averageDensity}")
println("Most used voices: ${libraryStats.mostUsedVoices}")
println("BPM range: ${libraryStats.bpmRange.min}-${libraryStats.bpmRange.max}")
```

### 💾 Export Formats (Phase 5.3)

#### JSON Export
```kotlin
// Export pattern to JSON
import fr.nicolaslinard.po.toolbox.io.JsonExporter

val exporter = JsonExporter(prettyPrint = true)
exporter.export(pattern, File("output.json"))

// Export multiple patterns
exporter.exportMultiple(patterns, File("library.json"))
```

**JSON Output Example:**
```json
{
  "patternNumber": 1,
  "metadata": {
    "name": "Amen Break - Bar 1",
    "bpm": 169,
    "genre": ["breakbeat", "jungle", "drum and bass"],
    "difficulty": "intermediate",
    "source": "Amen Brother - The Winstons (1969)"
  },
  "voices": [
    {
      "shortName": "kick",
      "displayName": "Bass Drum",
      "poNumber": 1,
      "steps": [1, 7, 11]
    }
  ]
}
```

#### CSV Export
```kotlin
// Export to CSV (list format)
import fr.nicolaslinard.po.toolbox.io.CsvExporter

val csvExporter = CsvExporter(includeMetadata = true)
csvExporter.export(pattern, File("output.csv"))

// Export to grid format (voices × 16 steps)
csvExporter.exportGrid(pattern, File("grid.csv"))

// Export multiple patterns
csvExporter.exportMultiple(patterns, File("library.csv"))
```

**CSV List Format:**
```csv
Voice Short Name,Voice Display Name,Step
kick,Bass Drum,1
kick,Bass Drum,7
snare,Snare,5
snare,Snare,13
```

**CSV Grid Format:**
```csv
Voice,Step 1,Step 2,Step 3,...,Step 16
Bass Drum,X,,,,...,
Snare,,,,X,...,
```

## Pattern Library

Included example patterns:
- **Amen Break** (2 bars) - The iconic breakbeat from "Amen Brother"
- **Test patterns** - Various difficulty levels for testing

Create your own library of:
- Classic breakbeats (Funky Drummer, Apache, etc.)
- 808 patterns (Roland TR-808 classics)
- Genre-specific beats (house, techno, jungle, etc.)
- Your original compositions

## File Format

Patterns are stored as markdown files with YAML frontmatter:

```markdown
---
name: Four-on-the-Floor
bpm: 128
genre: [house, techno]
difficulty: beginner
source: Classic house pattern
author: Nicolas Linard
date: 2025-01-15
---

# Four-on-the-Floor (Pattern #1)

## Pattern Grid

### Bass Drum (kick)
Step:  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
      [●][ ][ ][ ][●][ ][ ][ ][●][ ][ ][ ][●][ ][ ][ ]

### Clap (clap)
Step:  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
      [ ][ ][ ][ ][●][ ][ ][ ][ ][ ][ ][ ][●][ ][ ][ ]

## Programming Instructions for PO-12

1. Select pattern #1
2. **Bass Drum** - Steps: 1, 5, 9, 13
3. **Clap** - Steps: 5, 13
4. **Closed Hi-Hat** - Steps: 1, 3, 5, 7, 9, 11, 13, 15

## Notes

**Tempo:** 128 BPM
**Style:** Classic four-on-the-floor house pattern
```

## Development

### Build & Test
```bash
# Build project
./gradlew build

# Run all tests (130+ tests)
./gradlew test

# Run specific test
./gradlew test --tests "PatternSimilarityTest"

# Generate coverage report
./gradlew jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html

# Run in watch mode
./gradlew test --continuous
```

### Test-Driven Development
All features from Phase 4 onwards were built using strict TDD:
- ✅ RED: Write failing tests first
- ✅ GREEN: Implement minimal code to pass
- ✅ REFACTOR: Clean up and optimize

See [CLAUDE.md](CLAUDE.md) for detailed development documentation.

### Architecture

```
src/main/kotlin/fr/nicolaslinard/po/toolbox/
├── Main.kt                           # CLI entry point
├── models/                           # Data models
│   ├── PO12Pattern.kt
│   ├── PO12DrumVoice.kt              # Enum: all 16 voices
│   ├── PatternMetadata.kt
│   └── PatternChain.kt
├── commands/                         # CLI commands
│   ├── CreateCommand.kt              # Interactive pattern creation
│   ├── ViewCommand.kt                # Display patterns
│   ├── EditCommand.kt                # Modify patterns
│   ├── ListCommand.kt                # Filter & list patterns
│   ├── ValidateCommand.kt            # Pattern validation
│   ├── ChainCommand.kt               # Multi-bar chaining
│   ├── ImageCommand.kt               # Image display
│   ├── OcrCommand.kt                 # OCR integration
│   ├── MidiCommand.kt                # MIDI export
│   └── SimilarCommand.kt             # Similarity search
├── io/                               # File I/O
│   ├── MarkdownWriter.kt             # Write patterns to markdown
│   ├── MarkdownParser.kt             # Read patterns from markdown
│   ├── TextNotationParser.kt         # Parse "kick: 1,5,9,13"
│   ├── MidiExporter.kt               # MIDI file generation
│   ├── MidiNoteMapper.kt             # GM drum mapping
│   ├── JsonExporter.kt               # JSON export/import
│   ├── CsvExporter.kt                # CSV export
│   └── ImageDisplay.kt               # Image metadata
├── ui/                               # Terminal UI
│   └── GridEditor.kt                 # Text-based step editor
├── validation/                       # Validation
│   └── PatternValidator.kt           # Validate patterns
├── analysis/                         # Pattern analysis
│   ├── PatternSimilarityAnalyzer.kt  # Similarity search
│   └── PatternStatisticsAnalyzer.kt  # Statistics & metrics
└── ocr/                              # OCR integration
    ├── OcrEngine.kt                  # OCR interface
    ├── NotationParser.kt             # Parse OCR results
    ├── InstrumentMapper.kt           # Map notation → voices
    └── OcrPreprocessor.kt            # Image validation
```

## Implementation Phases

- ✅ **Phase 1**: MVP - Pattern creation with interactive grid
- ✅ **Phase 2**: Enhanced input - View, edit, text notation
- ✅ **Phase 3**: Management - List, validate, chain patterns
- ✅ **Phase 4**: Advanced - Image display, OCR hooks, MIDI export
- ✅ **Phase 5**: Analysis - Similarity search, statistics, JSON/CSV export

**Total:** 130+ tests, 100% passing, built with TDD

## Technology Stack

- **Kotlin 2.1.0** - Modern JVM language
- **Clikt 5.0.3** - CLI framework with type-safe arguments
- **Mordant 3.0.2** - Terminal UI with colors and tables
- **org.json** - JSON serialization
- **javax.sound.midi** - MIDI file generation
- **javax.imageio** - Image metadata extraction
- **JUnit 5 + MockK** - Testing framework
- **JaCoCo** - Code coverage reporting

## About Pocket Operator PO-12

The PO-12 Rhythm is a pocket-sized programmable drum machine by Teenage Engineering:
- 16 drum/percussion sounds
- 16 patterns with 16 steps each (16th note resolution)
- Pattern chaining for multi-bar sequences
- Parameter locks and effects
- Swing, BPM 60-206, live recording
- Official guide: https://teenage.engineering/guides/po-12

**16 Voices:**
1. Bass Drum (kick)
2. Snare
3. Closed Hi-Hat
4. Open Hi-Hat
5. Low Tom
6. Mid Tom
7. High Tom
8. Rim Shot
9. Hand Clap
10. Cowbell
11. Cymbal
12. Click
13. Noise
14. Blip
15. Tone
16. Sticks

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Write tests (we use TDD!)
4. Submit a pull request

See [CLAUDE.md](CLAUDE.md) for development guidelines.

## License

[Add your license here]

## Author

**Nicolas Linard**
Built with ♥️ and Test-Driven Development

---

*Built with Kotlin, Clikt, and Mordant • All features tested with 130+ passing tests*
