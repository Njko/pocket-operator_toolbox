package fr.nicolaslinard.po.toolbox.ocr

import java.io.File
import javax.imageio.ImageIO

/**
 * Validates and provides preprocessing suggestions for OCR images.
 * Helps ensure images are suitable for reliable OCR processing.
 */
class OcrPreprocessor(
    private val minWidth: Int = 200,
    private val minHeight: Int = 150
) {

    /**
     * Validate that an image file is suitable for OCR processing.
     * Checks file existence, format, and minimum dimensions.
     */
    fun validateImage(file: File): Boolean {
        if (!file.exists() || !file.isFile) {
            return false
        }

        // Check if it's a valid image format
        val extension = file.extension.lowercase()
        if (extension !in listOf("png", "jpg", "jpeg", "gif", "bmp")) {
            return false
        }

        // Try to read the image to verify it's valid
        return try {
            val image = ImageIO.read(file)
            if (image == null) {
                false
            } else {
                // Check minimum dimensions
                checkDimensions(image.width, image.height)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if image dimensions meet minimum requirements.
     * Small images may not have enough detail for reliable OCR.
     */
    fun checkDimensions(width: Int, height: Int): Boolean {
        return width >= minWidth && height >= minHeight
    }

    /**
     * Suggest preprocessing steps based on image characteristics.
     * Analyzes brightness and contrast to recommend improvements.
     */
    fun suggestPreprocessing(brightness: Double, contrast: Double): List<String> {
        val suggestions = mutableListOf<String>()

        // Check brightness (0.0 = black, 1.0 = white)
        when {
            brightness < 0.4 -> suggestions.add("Increase brightness - image appears too dark")
            brightness > 0.8 -> suggestions.add("Decrease brightness - image appears too bright")
        }

        // Check contrast (0.0 = no contrast, 1.0 = high contrast)
        when {
            contrast < 0.4 -> suggestions.add("Increase contrast - low contrast may reduce OCR accuracy")
            contrast > 0.95 -> suggestions.add("Decrease contrast - excessive contrast may introduce noise")
        }

        return suggestions
    }

    /**
     * Get recommended preprocessing steps for common issues.
     */
    fun getRecommendedSteps(): List<String> {
        return listOf(
            "Convert to grayscale for better text detection",
            "Apply slight Gaussian blur to reduce noise",
            "Use adaptive thresholding for binarization",
            "Ensure image resolution is at least ${minWidth}x${minHeight} pixels",
            "Remove skew/rotation if present",
            "Crop to region of interest (drum notation area)"
        )
    }
}
