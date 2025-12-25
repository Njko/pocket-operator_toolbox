# PO-Toolbox Copilot Instructions

## Project Overview
PO-Toolbox is a Kotlin CLI application for creating, managing, and analyzing Pocket Operator PO-12 drum patterns. It translates traditional drum notation into 16-step sequencer patterns, with features like MIDI export, pattern analysis, and OCR integration.

## Architecture
- **Core Model**: `PO12Pattern` with 16-step grids per voice, using `PO12DrumVoice` enum (16 voices: kick, snare, closed-hh, etc.)
- **CLI Framework**: Clikt-based commands in `commands/` package
- **UI**: Mordant terminal rendering with text/interactive grid editors
- **Storage**: Human-readable Markdown files with YAML frontmatter (GitHub-friendly)
- **Packages**: `models/`, `commands/`, `ui/`, `io/`, `analysis/`, `ocr/`, `validation/`, `utils/`

## Development Workflow
- **Build**: `./gradlew shadowJar` creates executable JAR with all dependencies
- **Run**: `./po-toolbox.bat <command>` (Windows) or `java -jar build/libs/po-toolbox-1.0.0.jar <command>`
- **Test**: `./gradlew test` (JUnit 5 + Mockk, 80%+ coverage required)
- **Debug**: Use `--info` flag on commands for verbose output

## Key Patterns
- **Text Notation**: `kick: 1,5,9,13` or `snare: 5,13` (comma/space separated steps)
- **Voice Short Names**: Use `PO12DrumVoice.fromShortName()` for parsing (e.g., "closed-hh" → CLOSED_HH)
- **Command Pattern**: Implement `EditCommand` interface for undo/redo in `PatternEditHistory`
- **Validation**: Use `PatternValidator` for errors/warnings before saving
- **Multi-Voice Context**: Show existing voices when editing new ones (see `MultiVoiceRenderer`)

## File Formats
- **Pattern Files**: Markdown with frontmatter metadata + visual grids + PO-12 programming instructions
- **MIDI Export**: GM drum mapping, 120 BPM default, supports pattern chaining
- **JSON/CSV Export**: Programmatic data exchange for analysis

## Examples
- Create pattern: `po-toolbox create` (interactive prompts)
- Edit existing: `po-toolbox edit patterns/amen-break-bar-1.md`
- Chain patterns: `po-toolbox chain patterns/amen-break-bar-1.md patterns/amen-break-bar-2.md`
- Export MIDI: `po-toolbox midi patterns/amen-break-bar-1.md --output midi-exports/`

## Testing Approach
- TDD with Kotlin test framework
- Mockk for mocking dependencies
- JaCoCo coverage verification (80% minimum)
- Test file I/O with temporary directories

## Key Files
- `Main.kt`: CLI entry point with subcommands
- `PO12Pattern.kt`: Core data model
- `CreateCommand.kt`: Interactive pattern creation
- `MarkdownWriter.kt`: Human-readable pattern serialization
- `GridEditor.kt`: Step programming interface
- `MidiExporter.kt`: DAW integration</content>
<parameter name="filePath">c:\Users\nicol\Documents\webapp\pocket-operator_toolbox\.github\copilot-instructions.md