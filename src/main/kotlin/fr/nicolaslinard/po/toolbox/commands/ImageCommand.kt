package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.io.ImageDisplay
import java.io.File

/**
 * Display drum notation images to assist with manual pattern transcription.
 */
class ImageCommand : CliktCommand(name = "image") {
    override fun help(context: Context) = "Display drum notation images for manual transcription"

    private val imagePath by argument(
        name = "image",
        help = "Path to image file, or pattern name to find associated images"
    ).optional()

    private val directory by option(
        "--directory", "-d",
        help = "Directory to search for images"
    ).file(mustExist = true, canBeFile = false, mustBeReadable = true)
        .default(File("doc/images"))

    private val pattern by option(
        "--pattern", "-p",
        help = "Filter images by pattern name (e.g., 'pattern1', 'amen')"
    )

    private val terminal = Terminal()
    private val imageDisplay = ImageDisplay()

    override fun run() {
        when {
            // Specific image file provided
            imagePath != null -> {
                val file = File(imagePath!!)
                if (file.exists() && imageDisplay.isValidImageFile(file)) {
                    displayImage(file)
                } else {
                    terminal.println((red)("Error: Image file not found or invalid: $imagePath"))
                }
            }

            // List images with optional pattern filter
            else -> {
                listImages()
            }
        }
    }

    private fun displayImage(file: File) {
        val reference = imageDisplay.createImageReference(file)

        if (reference != null) {
            terminal.println()
            terminal.println((cyan)(reference))
            terminal.println()
            terminal.println((dim)("To view this image:"))
            terminal.println((dim)("• Open in browser: file://${file.absolutePath}"))
            terminal.println((dim)("• Or use your system's default image viewer"))
            terminal.println()

            // Show suggestions for related images
            val relatedImages = findRelatedImages(file)
            if (relatedImages.isNotEmpty()) {
                terminal.println((bold)("Related images:"))
                relatedImages.forEach { related ->
                    terminal.println((dim)("  • ${related.name}"))
                }
                terminal.println()
            }
        } else {
            terminal.println((red)("Error: Could not read image file"))
        }
    }

    private fun listImages() {
        val images = imageDisplay.findImagesInDirectory(
            directory,
            namePattern = pattern
        )

        if (images.isEmpty()) {
            terminal.println((yellow)("No images found in ${directory.path}"))
            return
        }

        terminal.println()
        terminal.println((bold + cyan)("=== Available Drum Notation Images ==="))
        terminal.println()

        images.sortedBy { it.name }.forEach { file ->
            val dimensions = imageDisplay.getImageDimensions(file)
            val sizeKB = file.length() / 1024

            terminal.print((cyan)(file.name.padEnd(40)))
            if (dimensions != null) {
                terminal.print((dim)(" ${dimensions.width}x${dimensions.height}"))
            }
            terminal.print((dim)(" (${sizeKB}KB)"))
            terminal.println()
        }

        terminal.println()
        terminal.println((dim)("Use 'po-toolbox image <filename>' to view an image"))
        terminal.println((dim)("Use --pattern to filter by name (e.g., --pattern amen)"))
        terminal.println()
    }

    private fun findRelatedImages(file: File): List<File> {
        // Find images with similar names (e.g., pattern1_drum, pattern1_snare, pattern1_score)
        val baseName = file.nameWithoutExtension
            .replace(Regex("_(drum|snare|score|hh|kick).*"), "")

        return imageDisplay.findImagesInDirectory(directory)
            .filter { it != file && it.nameWithoutExtension.startsWith(baseName) }
    }
}
