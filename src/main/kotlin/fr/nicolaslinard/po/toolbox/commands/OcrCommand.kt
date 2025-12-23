package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import fr.nicolaslinard.po.toolbox.ocr.*
import java.io.File

class OcrCommand : CliktCommand(name = "ocr") {
    override fun help(context: Context) =
        "Process drum notation images with OCR (integration hooks)"

    private val imageFile by argument(
        help = "Path to drum notation image file"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true)

    private val minConfidence by option(
        "--min-confidence",
        help = "Minimum confidence threshold for detected notes (0.0-1.0)"
    ).double().default(0.7)

    private val validate by option(
        "--validate",
        help = "Validate image suitability before processing"
    ).flag(default = true)

    private val terminal = Terminal()

    override fun run() {
        terminal.println((bold + cyan)("OCR Integration - Drum Notation Parser"))
        terminal.println()

        // Step 1: Validate image if requested
        if (validate) {
            validateImage(imageFile)
        }

        // Step 2: Process image with OCR engine
        processImage(imageFile)
    }

    private fun validateImage(file: File) {
        terminal.println((bold)("Step 1: Image Validation"))
        terminal.println("─".repeat(50))

        val preprocessor = OcrPreprocessor()
        val isValid = preprocessor.validateImage(file)

        if (isValid) {
            terminal.println((green)("✓ Image is suitable for OCR processing"))
        } else {
            terminal.println((yellow)("⚠ Image may not be optimal for OCR"))
            terminal.println()
            terminal.println((bold)("Recommendations:"))
            preprocessor.getRecommendedSteps().forEach { step ->
                terminal.println("  • $step")
            }
        }

        terminal.println()
    }

    private fun processImage(file: File) {
        terminal.println((bold)("Step 2: OCR Processing"))
        terminal.println("─".repeat(50))

        // Use mock OCR engine for demonstration
        // In production, this would be replaced with a real OCR implementation
        val ocrEngine = MockOcrEngine()
        val result = ocrEngine.processImage(file)

        if (!result.success) {
            terminal.println((red)("✗ OCR processing failed: ${result.error}"))
            return
        }

        // Display OCR results
        terminal.println((green)("✓ OCR processing successful"))
        terminal.println("Confidence: ${String.format("%.1f%%", result.confidence * 100)}")

        if (result.text.isNotEmpty()) {
            terminal.println("Detected text: ${result.text}")
        }
        terminal.println()

        // Display detected notes
        if (result.detectedNotes.isNotEmpty()) {
            displayDetectedNotes(result)
        }

        // Step 3: Parse into pattern data
        parseToPattern(result)
    }

    private fun displayDetectedNotes(result: OcrResult) {
        terminal.println((bold)("Detected Notes:"))

        val notesTable = table {
            header {
                row((bold)("Instrument"), (bold)("Step"), (bold)("Confidence"))
            }
            body {
                result.detectedNotes
                    .sortedBy { it.step }
                    .forEach { note ->
                        val confidenceColor = when {
                            note.confidence >= 0.9 -> green
                            note.confidence >= 0.7 -> yellow
                            else -> red
                        }
                        row(
                            note.instrument,
                            note.step.toString(),
                            confidenceColor(String.format("%.1f%%", note.confidence * 100))
                        )
                    }
            }
        }

        terminal.println(notesTable)
        terminal.println()
    }

    private fun parseToPattern(ocrResult: OcrResult) {
        terminal.println((bold)("Step 3: Pattern Data Conversion"))
        terminal.println("─".repeat(50))

        val parser = NotationParser(minConfidence = minConfidence)
        val patternData = parser.parseToPatternData(ocrResult)

        if (patternData.voices.isEmpty()) {
            terminal.println((yellow)("⚠ No notes detected above confidence threshold ($minConfidence)"))
            return
        }

        terminal.println((green)("✓ Converted to pattern data"))
        terminal.println()
        terminal.println((bold)("Voices:"))

        patternData.voices.forEach { (voice, steps) ->
            terminal.println("  ${(cyan)(voice)}: ${steps.joinToString(", ")}")
        }

        terminal.println()
        terminal.println((bold)("Metadata:"))
        patternData.metadata.forEach { (key, value) ->
            terminal.println("  $key: $value")
        }

        terminal.println()
        terminal.println((dim)("Note: OCR integration uses mock data for demonstration."))
        terminal.println((dim)("To use real OCR, implement the OcrEngine interface with"))
        terminal.println((dim)("your preferred OCR library (Tesseract, Google Vision, etc.)"))
    }
}

/**
 * Mock OCR engine for demonstration purposes.
 * Replace with real OCR implementation in production.
 */
private class MockOcrEngine : OcrEngine {
    override fun processImage(imageFile: File): OcrResult {
        if (!imageFile.exists()) {
            return OcrResult.failure("File not found: ${imageFile.path}")
        }

        // Mock successful OCR result with example drum pattern
        return OcrResult(
            success = true,
            text = "Drum Pattern - ${imageFile.nameWithoutExtension}",
            confidence = 0.87,
            detectedNotes = listOf(
                DetectedNote("kick", 1, 0.95),
                DetectedNote("kick", 5, 0.92),
                DetectedNote("kick", 11, 0.88),
                DetectedNote("snare", 5, 0.91),
                DetectedNote("snare", 13, 0.89),
                DetectedNote("closed-hh", 1, 0.85),
                DetectedNote("closed-hh", 3, 0.86),
                DetectedNote("closed-hh", 5, 0.84),
                DetectedNote("closed-hh", 7, 0.85),
                DetectedNote("closed-hh", 9, 0.83),
                DetectedNote("closed-hh", 11, 0.85),
                DetectedNote("closed-hh", 13, 0.84),
                DetectedNote("closed-hh", 15, 0.86)
            )
        )
    }
}
