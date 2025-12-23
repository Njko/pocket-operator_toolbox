package fr.nicolaslinard.po.toolbox.ocr

import java.io.File

/**
 * Interface for OCR engines that can process drum notation images.
 * This provides hooks for integrating various OCR solutions.
 */
interface OcrEngine {
    /**
     * Process an image file and extract drum notation data.
     * @param imageFile The image file to process
     * @return OcrResult containing detected notation and confidence scores
     */
    fun processImage(imageFile: File): OcrResult
}

/**
 * Result of OCR processing.
 */
data class OcrResult(
    val success: Boolean,
    val text: String = "",
    val confidence: Double = 0.0,
    val detectedNotes: List<DetectedNote> = emptyList(),
    val error: String? = null
) {
    companion object {
        /**
         * Create a failed OCR result with an error message.
         */
        fun failure(errorMessage: String): OcrResult {
            return OcrResult(
                success = false,
                text = "",
                confidence = 0.0,
                detectedNotes = emptyList(),
                error = errorMessage
            )
        }
    }
}

/**
 * Represents a detected drum note from OCR.
 */
data class DetectedNote(
    val instrument: String,  // e.g., "kick", "snare", "hi-hat"
    val step: Int,           // Step number (1-16)
    val confidence: Double   // Confidence score (0.0 - 1.0)
)

/**
 * Data structure for parsed pattern data from OCR.
 */
data class PatternData(
    val voices: Map<String, List<Int>>,  // Instrument name -> step numbers
    val metadata: Map<String, String> = emptyMap()
)
