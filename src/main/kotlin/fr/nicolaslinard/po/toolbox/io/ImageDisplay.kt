package fr.nicolaslinard.po.toolbox.io

import java.io.File
import javax.imageio.ImageIO

/**
 * Handles display and metadata extraction for drum notation images.
 * Helps users view reference images while manually transcribing patterns.
 */
class ImageDisplay {

    companion object {
        private val SUPPORTED_FORMATS = setOf("png", "jpg", "jpeg", "gif", "bmp")
    }

    /**
     * Check if a file is a valid image format.
     */
    fun isValidImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in SUPPORTED_FORMATS
    }

    /**
     * Get image dimensions from file.
     */
    fun getImageDimensions(file: File): ImageDimensions? {
        if (!file.exists() || !isValidImageFile(file)) {
            return null
        }

        return try {
            val image = ImageIO.read(file)
            if (image != null) {
                ImageDimensions(width = image.width, height = image.height)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get formatted information about an image.
     */
    fun getImageInfo(file: File): String? {
        if (!file.exists()) {
            return null
        }

        val dimensions = getImageDimensions(file)
        val format = file.extension.uppercase()

        return buildString {
            appendLine("Image: ${file.name}")
            if (dimensions != null) {
                appendLine("Width: ${dimensions.width}px")
                appendLine("Height: ${dimensions.height}px")
            }
            appendLine("Format: $format")
            appendLine("Size: ${file.length() / 1024}KB")
        }
    }

    /**
     * Find all image files in a directory.
     */
    fun findImagesInDirectory(
        directory: File,
        namePattern: String? = null
    ): List<File> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        return directory.listFiles()
            ?.filter { it.isFile && isValidImageFile(it) }
            ?.filter { file ->
                namePattern == null || file.name.contains(namePattern, ignoreCase = true)
            }
            ?: emptyList()
    }

    /**
     * Resolve an image path, returning the File if it exists.
     */
    fun resolveImagePath(path: String): File? {
        val file = File(path)
        return if (file.exists() && isValidImageFile(file)) {
            file
        } else {
            null
        }
    }

    /**
     * Create a displayable reference to an image file.
     * Returns a formatted string with file path and metadata.
     */
    fun createImageReference(file: File): String? {
        if (!file.exists()) {
            return null
        }

        val info = getImageInfo(file) ?: return null
        val absolutePath = file.absolutePath

        return buildString {
            appendLine("━".repeat(60))
            appendLine(info.trim())
            appendLine("Path: file://$absolutePath")
            appendLine("━".repeat(60))
        }
    }
}

/**
 * Data class representing image dimensions.
 */
data class ImageDimensions(
    val width: Int,
    val height: Int
)
