package fr.nicolaslinard.po.toolbox.io

import java.io.File
import kotlin.test.*

/**
 * TDD: RED phase - Tests for image display functionality
 *
 * Feature: Display drum notation images to help users transcribe patterns manually
 */
class ImageDisplayTest {

    // === Image File Detection Tests ===

    @Test
    fun `should detect if file is a valid image format`() {
        val imageDisplay = ImageDisplay()

        assertTrue(imageDisplay.isValidImageFile(File("test.png")))
        assertTrue(imageDisplay.isValidImageFile(File("test.jpg")))
        assertTrue(imageDisplay.isValidImageFile(File("test.jpeg")))
        assertTrue(imageDisplay.isValidImageFile(File("test.gif")))
        assertFalse(imageDisplay.isValidImageFile(File("test.txt")))
        assertFalse(imageDisplay.isValidImageFile(File("test.md")))
    }

    @Test
    fun `should handle case-insensitive file extensions`() {
        val imageDisplay = ImageDisplay()

        assertTrue(imageDisplay.isValidImageFile(File("test.PNG")))
        assertTrue(imageDisplay.isValidImageFile(File("test.JPG")))
        assertTrue(imageDisplay.isValidImageFile(File("test.JPEG")))
    }

    // === Image Metadata Tests ===

    @Test
    fun `should extract image dimensions from file`() {
        val imageDisplay = ImageDisplay()
        // We'll need to create a test image file or mock this
        // For now, test the interface exists

        val testImagePath = "doc/images/pattern1_score_example.png"
        val file = File(testImagePath)

        if (file.exists()) {
            val dimensions = imageDisplay.getImageDimensions(file)

            assertNotNull(dimensions)
            assertTrue(dimensions.width > 0)
            assertTrue(dimensions.height > 0)
        }
    }

    // === Image Display Formatting Tests ===

    @Test
    fun `should generate ASCII representation info for image`() {
        val imageDisplay = ImageDisplay()
        val file = File("doc/images/pattern1_score_example.png")

        if (file.exists()) {
            val info = imageDisplay.getImageInfo(file)

            assertNotNull(info)
            assertTrue(info.contains("Width:"))
            assertTrue(info.contains("Height:"))
            assertTrue(info.contains("Format:"))
        }
    }

    // === Image Reference Tests ===

    @Test
    fun `should find all images in a directory`() {
        val imageDisplay = ImageDisplay()
        val docImages = File("doc/images")

        if (docImages.exists() && docImages.isDirectory) {
            val images = imageDisplay.findImagesInDirectory(docImages)

            assertNotNull(images)
            assertTrue(images.isNotEmpty())
            assertTrue(images.all { it.exists() })
            assertTrue(images.all { imageDisplay.isValidImageFile(it) })
        }
    }

    @Test
    fun `should filter images by name pattern`() {
        val imageDisplay = ImageDisplay()
        val docImages = File("doc/images")

        if (docImages.exists()) {
            val scoreImages = imageDisplay.findImagesInDirectory(
                docImages,
                namePattern = "score"
            )

            assertTrue(scoreImages.all { it.name.contains("score", ignoreCase = true) })
        }
    }

    // === Image Path Resolution Tests ===

    @Test
    fun `should resolve relative image paths`() {
        val imageDisplay = ImageDisplay()

        val resolved = imageDisplay.resolveImagePath("doc/images/pattern1_score_example.png")
        assertNotNull(resolved)
        assertEquals("pattern1_score_example.png", resolved.name)
    }

    @Test
    fun `should return null for non-existent image paths`() {
        val imageDisplay = ImageDisplay()

        val resolved = imageDisplay.resolveImagePath("nonexistent/image.png")
        assertNull(resolved)
    }

    // === Terminal Display Format Tests ===

    @Test
    fun `should create displayable image reference`() {
        val imageDisplay = ImageDisplay()
        val file = File("doc/images/pattern1_score_example.png")

        if (file.exists()) {
            val reference = imageDisplay.createImageReference(file)

            assertNotNull(reference)
            assertTrue(reference.contains(file.name))
            assertTrue(reference.contains("file://") || reference.contains(file.absolutePath))
        }
    }
}

/**
 * Data class for image dimensions (to be implemented in GREEN phase)
 */
data class ImageDimensions(
    val width: Int,
    val height: Int
)
