package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.models.PatternChain
import fr.nicolaslinard.po.toolbox.models.PatternChainBuilder
import java.io.File

class ChainCommand : CliktCommand(name = "chain") {
    override fun help(context: Context) = "Display pattern chain information and programming instructions"

    private val patternFiles by argument(
        name = "patterns",
        help = "Pattern files to chain together (in order)"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .multiple(required = true)

    private val name by option(
        "--name", "-n",
        help = "Name for the pattern chain"
    )

    private val terminal = Terminal()

    override fun run() {
        val parser = MarkdownParser()

        // Parse all patterns
        val patterns = patternFiles.map { file ->
            try {
                parser.parse(file)
            } catch (e: Exception) {
                terminal.println((red)("Error parsing ${file.name}: ${e.message}"))
                throw e
            }
        }

        // Create chain
        val chainName = name ?: patterns.firstOrNull()?.metadata?.name ?: "Pattern Chain"
        val metadata = patterns.firstOrNull()?.metadata ?: return

        val chain = PatternChainBuilder()
            .name(chainName)
            .metadata(metadata)
            .apply {
                patterns.forEach { addPattern(it) }
            }
            .sequenceFromPatterns()
            .build()

        // Display chain information
        displayChain(chain)
    }

    private fun displayChain(chain: PatternChain) {
        terminal.println()
        terminal.println((bold + cyan)("=== ${chain.name} ==="))
        terminal.println()

        terminal.println((bold)("Pattern Chain"))
        terminal.println((dim)("Total bars: ${chain.totalBars}"))
        terminal.println((dim)("Patterns: ${chain.patterns.map { it.number }.sorted().joinToString(", ")}"))
        terminal.println((dim)("Sequence: ${chain.chainSequenceString}"))
        terminal.println()

        chain.metadata.bpm?.let {
            terminal.println((dim)("BPM: ") + (bold)("$it"))
        }
        terminal.println()

        // Show each pattern
        chain.patterns.sortedBy { it.number }.forEach { pattern ->
            terminal.println((bold + cyan)("Pattern ${pattern.number}:"))

            pattern.voices.keys.sortedBy { it.poNumber }.forEach { voice ->
                val steps = pattern.getActiveSteps(voice)
                terminal.println("  ${voice.displayName} (Sound ${voice.poNumber}): ${steps.joinToString(", ")}")
            }
            terminal.println()
        }

        // Programming instructions
        terminal.println((bold + cyan)("PO-12 Programming Instructions:"))
        terminal.println()

        var step = 1
        chain.patterns.sortedBy { it.number }.forEach { pattern ->
            terminal.println((bold)("${step}. Program Pattern ${pattern.number}:"))

            pattern.voices.keys.sortedBy { it.poNumber }.forEach { voice ->
                val steps = pattern.getActiveSteps(voice)
                if (steps.isNotEmpty()) {
                    terminal.println("   ${voice.displayName} (button ${voice.poNumber}): ${steps.joinToString(", ")}")
                }
            }
            terminal.println()
            step++
        }

        terminal.println((bold)("${step}. Chain the patterns:"))
        terminal.println("   - Press the ${(cyan)("pattern")} button")
        terminal.println("   - Enter sequence: ${(bold + green)(chain.chainSequenceString)}")
        terminal.println("   - Press ${(cyan)("write")} to save")
        terminal.println()

        terminal.println((dim)("The PO-12 will now loop through all ${chain.totalBars} bars continuously!"))
        terminal.println()
    }
}
