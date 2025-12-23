package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.terminal.Terminal
import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.io.MidiExporter
import fr.nicolaslinard.po.toolbox.io.MidiExportOptions
import java.io.File

class MidiCommand : CliktCommand(name = "midi") {
    override fun help(context: Context) =
        "Export PO-12 patterns to MIDI format for DAW integration"

    private val patternFiles by argument(
        help = "Pattern markdown file(s) to export"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true).multiple()

    private val output by option(
        "-o", "--output",
        help = "Output MIDI file path (default: <pattern-name>.mid)"
    ).file()

    private val resolution by option(
        "--resolution",
        help = "MIDI resolution in PPQ (Pulses Per Quarter note)"
    ).int().default(96)

    private val velocity by option(
        "--velocity",
        help = "Default note velocity (1-127)"
    ).int().default(100)

    private val noteDuration by option(
        "--duration",
        help = "Note duration in ticks"
    ).int().default(96)

    private val noMetadata by option(
        "--no-metadata",
        help = "Exclude pattern metadata from MIDI file"
    ).flag()

    private val terminal = Terminal()
    private val parser = MarkdownParser()
    private val exporter = MidiExporter()

    override fun run() {
        terminal.println((bold + cyan)("MIDI Export - PO-12 to MIDI Converter"))
        terminal.println()

        // Validate velocity
        if (velocity !in 1..127) {
            terminal.println((red)("Error: Velocity must be between 1 and 127"))
            return
        }

        try {
            // Parse pattern files
            val patterns = patternFiles.map { file ->
                terminal.println("Reading pattern: ${file.name}")
                parser.parse(file)
            }

            // Determine output file
            val outputFile = output ?: File(
                "${patterns[0].metadata.name.replace(" ", "_")}.mid"
            )

            // Create export options
            val options = MidiExportOptions(
                resolution = resolution,
                defaultVelocity = velocity,
                noteDuration = noteDuration,
                includeMetadata = !noMetadata
            )

            // Export to MIDI
            terminal.println()
            terminal.println((bold)("Exporting to MIDI..."))
            terminal.println("─".repeat(50))

            if (patterns.size == 1) {
                exporter.exportToMidi(patterns.first(), outputFile, options)
            } else {
                exporter.exportPatternsToMidi(patterns, outputFile, options)
            }

            // Display export summary
            displayExportSummary(patterns, outputFile, options)

        } catch (e: Exception) {
            terminal.println((red)("Error: ${e.message}"))
            e.printStackTrace()
        }
    }

    private fun displayExportSummary(
        patterns: List<fr.nicolaslinard.po.toolbox.models.PO12Pattern>,
        outputFile: File,
        options: MidiExportOptions
    ) {
        terminal.println()
        terminal.println((green + bold)("✓ MIDI export successful!"))
        terminal.println()

        terminal.println((bold)("Output:"))
        terminal.println("  File: ${outputFile.absolutePath}")
        terminal.println("  Size: ${outputFile.length()} bytes")
        terminal.println()

        terminal.println((bold)("Patterns exported: ${patterns.size}"))
        patterns.forEachIndexed { index, pattern ->
            val bpm = pattern.metadata.bpm ?: 120
            val voiceCount = pattern.voices.size
            val noteCount = pattern.voices.values.sumOf { it.size }

            terminal.println("  ${index + 1}. ${pattern.metadata.name}")
            terminal.println("     Pattern #${pattern.number} | ${bpm} BPM | $voiceCount voices | $noteCount notes")
        }

        terminal.println()
        terminal.println((bold)("MIDI Settings:"))
        terminal.println("  Resolution: ${options.resolution} PPQ")
        terminal.println("  Velocity: ${options.defaultVelocity}")
        terminal.println("  Note duration: ${options.noteDuration} ticks")
        terminal.println("  Channel: 10 (GM Drums)")
        terminal.println("  Metadata: ${if (options.includeMetadata) "Included" else "Excluded"}")

        terminal.println()
        terminal.println((dim)("Tip: Import this MIDI file into your DAW to edit and arrange."))
        terminal.println((dim)("     Use MIDI channel 10 for General MIDI drum sounds."))
    }
}
