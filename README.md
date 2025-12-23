# PO-Toolbox

A command-line tool for creating and managing Pocket Operator PO-12 pattern libraries. Translate drum notation into sequencer patterns and store them as human-readable markdown files.

## Features

- 🎵 Interactive 16-step grid editor with cursor navigation
- 📝 Human-readable markdown pattern storage (GitHub-friendly)
- 🎼 Support for all 16 PO-12 drum voices
- 📊 Rich metadata (BPM, genre, difficulty, source attribution)
- 🎹 Step-by-step PO-12 programming instructions
- 🔮 Designed for future extensibility (other PO models, OCR, MIDI export)

## Requirements

- Java 17 JDK
- Terminal with UTF-8 support

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd pocket-operator_scores
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Create your first pattern**
   ```bash
   ./gradlew run --args="create"
   ```

4. **Follow the interactive prompts** to:
   - Enter pattern metadata (name, BPM, genre, etc.)
   - Select drum voices to program
   - Use arrow keys (←/→) to navigate the grid
   - Press SPACE to toggle steps on/off
   - Press ENTER to save

5. **Find your pattern** in the `patterns/` directory as a markdown file

## Usage

### Create a Pattern

```bash
po-toolbox create
```

Interactive mode with grid editor for each drum voice:
- Arrow keys to navigate
- SPACE to toggle steps
- ENTER to finish editing a voice
- ESC to cancel

### Pattern Output

Patterns are saved as markdown files with:
- YAML frontmatter metadata
- ASCII grid visualization
- PO-12 programming instructions
- Notes and attribution

Example: `patterns/amen-break-bar1.md`

## Project Structure

```
pocket-operator_scores/
├── src/main/kotlin/fr/nicolaslinard/po/toolbox/
│   ├── Main.kt                        # CLI entry point
│   ├── models/                        # Data models
│   │   ├── PO12Pattern.kt
│   │   ├── PO12DrumVoice.kt
│   │   └── PatternMetadata.kt
│   ├── commands/                      # CLI commands
│   │   └── CreateCommand.kt
│   ├── ui/                           # Terminal UI
│   │   └── GridEditor.kt
│   └── io/                           # File operations
│       └── MarkdownWriter.kt
├── patterns/                          # Generated patterns
├── doc/                              # Documentation & examples
└── CLAUDE.md                         # Development guide

```

## Roadmap

- [x] **Phase 1:** Core pattern creation with interactive grid
- [ ] **Phase 2:** View, edit, and text notation input
- [ ] **Phase 3:** Pattern listing, filtering, and chaining
- [ ] **Phase 4:** Image transcription and OCR integration

## Contributing

Contributions welcome! Please see [CLAUDE.md](CLAUDE.md) for development setup and architecture details.

## About Pocket Operator PO-12

The PO-12 Rhythm is a pocket-sized programmable drum machine by Teenage Engineering:
- 16 drum/percussion sounds
- 16 patterns with 16 steps each
- Pattern chaining
- Parameter locks and effects
- Official guide: https://teenage.engineering/guides/po-12

## License

[Add your license here]

## Author

Nicolas Linard

---

*Built with Kotlin, Clikt, and Mordant*
