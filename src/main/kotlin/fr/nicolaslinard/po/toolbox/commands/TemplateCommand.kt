package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.models.*
import fr.nicolaslinard.po.toolbox.io.MarkdownWriter
import fr.nicolaslinard.po.toolbox.ui.MultiVoiceRenderer
import java.io.File
import java.time.LocalDate

/**
 * GREEN Phase - Minimal implementation to pass tests
 *
 * Command for browsing and creating patterns from templates.
 */
class TemplateCommand : CliktCommand(name = "template") {
    override fun help(context: Context) = "Browse and create patterns from templates"

    private val listTemplates by option(
        "--list", "-l",
        help = "List all available templates"
    ).flag(default = false)

    private val category by option(
        "--category", "-c",
        help = "Filter templates by category"
    )

    private val outputPath by option(
        "--output", "-o",
        help = "Output directory for pattern files"
    ).default("patterns")

    private val patternNumber by option(
        "--pattern-number", "-p",
        help = "PO-12 pattern number (1-16)"
    ).int().default(1)

    private val terminal = Terminal()
    private val multiVoiceRenderer = MultiVoiceRenderer(terminal)

    override fun run() {
        if (listTemplates) {
            displayTemplateList()
        } else {
            createFromTemplate()
        }
    }

    private fun displayTemplateList() {
        terminal.println((bold + cyan)("=== Available Pattern Templates ==="))
        terminal.println()

        val templates = if (category != null) {
            BuiltInTemplates.byCategory(category!!)
        } else {
            BuiltInTemplates.all()
        }

        if (templates.isEmpty()) {
            terminal.println((yellow)("No templates found for category: $category"))
            terminal.println()
            terminal.println((dim)("Available categories:"))
            BuiltInTemplates.all()
                .map { it.category }
                .distinct()
                .forEach { cat ->
                    terminal.println((dim)("  - $cat"))
                }
            return
        }

        templates.groupBy { it.category }.forEach { (cat, tmpls) ->
            terminal.println((bold)("${cat.uppercase()}:"))
            tmpls.forEach { template ->
                terminal.println("  ${(cyan)(template.id)}")
                terminal.println("    ${template.name} - ${template.description}")
                terminal.println("    Difficulty: ${template.difficulty.name.lowercase()}")
                template.suggestedBPM?.let { bpm ->
                    terminal.println("    Suggested BPM: $bpm")
                }
                terminal.println()
            }
        }

        terminal.println((dim)("Use: po-toolbox template --category <category> to filter"))
        terminal.println((dim)("Use: po-toolbox create --from-template <id> to create from template"))
    }

    private fun createFromTemplate() {
        terminal.println((bold + cyan)("=== Create Pattern from Template ==="))
        terminal.println()

        val template = selectTemplate() ?: return

        terminal.println()
        terminal.println((green)("Selected: ${template.name}"))
        terminal.println((dim)(template.description))
        terminal.println()

        multiVoiceRenderer.renderCompactGrid(template.voices)
        terminal.println()

        // Gather metadata
        terminal.println((bold)("Enter pattern details:"))
        terminal.println()

        val name = prompt("Pattern name", template.name)
        val description = prompt("Description (optional)", template.description)
        val bpmString = prompt("BPM (optional)", template.suggestedBPM?.toString() ?: "")
        val bpm = bpmString.toIntOrNull()
        val author = prompt("Author (optional)", "")

        val metadata = PatternMetadata(
            name = name,
            description = description.takeIf { it.isNotBlank() },
            bpm = bpm,
            genre = emptyList(),
            difficulty = template.difficulty,
            sourceAttribution = "Template: ${template.name}".takeIf { it.isNotBlank() },
            author = author.takeIf { it.isNotBlank() },
            dateCreated = LocalDate.now()
        )

        // Create pattern from template
        val pattern = PO12Pattern(
            voices = template.voices,
            metadata = metadata,
            number = patternNumber
        )

        // Write to markdown
        val writer = MarkdownWriter()
        val outputDir = File(outputPath)
        val file = writer.write(pattern, outputDir)

        terminal.println()
        terminal.println((bold + green)("✓ Pattern saved to: ${file.path}"))
        terminal.println((dim)("You can view it with: po-toolbox view ${file.path}"))
    }

    private fun selectTemplate(): PatternTemplate? {
        terminal.println((bold)("Select a template:"))
        terminal.println()

        val templates = BuiltInTemplates.all()
        templates.forEachIndexed { index, template ->
            terminal.println("  ${index + 1}. ${(cyan)(template.name)} (${template.difficulty.name.lowercase()})")
            terminal.println("     ${(dim)(template.description)}")
        }
        terminal.println()

        val input = prompt("Choice (1-${templates.size}, or Enter to cancel)", "")
        if (input.isBlank()) {
            terminal.println((yellow)("Template selection cancelled."))
            return null
        }

        val choice = input.toIntOrNull()
        if (choice == null || choice !in 1..templates.size) {
            terminal.println((red)("Invalid choice. Please enter 1-${templates.size}."))
            return selectTemplate()
        }

        return templates[choice - 1]
    }

    private fun prompt(message: String, default: String = ""): String {
        val defaultText = if (default.isNotBlank()) " [$default]" else ""
        terminal.print((bold)(message) + defaultText + ": ")
        val input = readlnOrNull() ?: default
        return if (input.isBlank() && default.isNotBlank()) default else input
    }
}
