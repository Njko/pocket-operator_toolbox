package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.validation.PatternValidator
import java.io.File

class ValidateCommand : CliktCommand(name = "validate") {
    override fun help(context: Context) = "Validate PO-12 pattern files for correctness"

    private val patternFiles by argument(
        name = "files",
        help = "Pattern markdown files to validate"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .multiple(required = true)

    private val terminal = Terminal()

    override fun run() {
        val parser = MarkdownParser()
        val validator = PatternValidator()

        var totalErrors = 0
        var totalWarnings = 0
        var validCount = 0

        patternFiles.forEach { file ->
            terminal.println()
            terminal.println((bold)("Validating: ${file.name}"))

            try {
                val pattern = parser.parse(file)
                val result = validator.validate(pattern)

                if (result.isValid) {
                    terminal.println((green)("✓ Valid"))
                    validCount++
                } else {
                    terminal.println((red)("✗ Invalid"))
                }

                if (result.hasErrors) {
                    terminal.println((red + bold)("  Errors:"))
                    result.errors.forEach { error ->
                        terminal.println((red)("    • $error"))
                        totalErrors++
                    }
                }

                if (result.hasWarnings) {
                    terminal.println((yellow + bold)("  Warnings:"))
                    result.warnings.forEach { warning ->
                        terminal.println((yellow)("    • $warning"))
                        totalWarnings++
                    }
                }

            } catch (e: Exception) {
                terminal.println((red)("✗ Parse error: ${e.message}"))
                totalErrors++
            }
        }

        // Summary
        terminal.println()
        terminal.println((bold + cyan)("=== Validation Summary ==="))
        terminal.println("Total files: ${patternFiles.size}")
        terminal.println((green)("Valid: $validCount"))
        terminal.println((red)("Errors: $totalErrors"))
        terminal.println((yellow)("Warnings: $totalWarnings"))
        terminal.println()

        if (totalErrors > 0) {
            throw RuntimeException("Validation failed with $totalErrors error(s)")
        }
    }
}
