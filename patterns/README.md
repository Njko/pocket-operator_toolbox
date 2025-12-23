# PO-12 Pattern Library

This directory contains Pocket Operator PO-12 drum patterns translated from traditional notation.

## Pattern Collection

### Amen Break
The iconic drum break from "Amen Brother" by The Winstons (1969). This is the most sampled drum break in music history, fundamental to breakbeat, drum and bass, and hip-hop.

**Files:**
- `amen-break-bar-1.md` - First bar with the main groove (Pattern 1)
- `amen-break-bar-2.md` - Second bar with the famous snare fill (Pattern 2)

**How to play:**
1. Program both patterns on your PO-12 (Patterns 1 and 2)
2. Set tempo to 169 BPM
3. Chain the patterns: Press `pattern` button, then enter `1,2` to chain them together
4. The PO-12 will loop through both bars continuously

**Pattern chaining syntax:**
- `1,2` - Play pattern 1, then pattern 2, then repeat
- `1,1,1,2` - Play pattern 1 three times, then pattern 2 once
- Experiment with different chain sequences for variation!

### Test Pattern
- `test-pattern.md` - Simple test pattern for verifying the tool

## About These Patterns

Each markdown file contains:
- YAML metadata (BPM, genre, difficulty, source)
- Visual step grids showing active/inactive steps
- Step-by-step programming instructions for the PO-12
- Notes about tempo and attribution

These files are human-readable and can be viewed directly on GitHub or in any text editor.

## Creating New Patterns

Use the po-toolbox CLI to create your own patterns:

```bash
po-toolbox create
```

Follow the interactive prompts to program your pattern!
