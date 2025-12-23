package fr.nicolaslinard.po.toolbox.ocr

import fr.nicolaslinard.po.toolbox.TestFixtures
import java.io.File
import kotlin.test.*

/**
 * TDD: RED phase - Tests for OCR integration hooks
 *
 * Feature: OCR integration hooks for automatic drum notation parsing
 * This provides interfaces and basic implementations for OCR engines,
 * not a full OCR solution.
 */
class OcrEngineTest {

    // === OCR Engine Interface Tests ===

    @Test
    fun `should define OCR engine interface`() {
        val mockEngine = MockOcrEngine()

        assertNotNull(mockEngine)
        assertTrue(mockEngine is OcrEngine)
    }

    @Test
    fun `should process image and return OCR result`() {
        val mockEngine = MockOcrEngine()
        val testImage = File("doc/images/pattern1_score_example.png")

        if (testImage.exists()) {
            val result = mockEngine.processImage(testImage)

            assertNotNull(result)
            assertTrue(result.success)
        }
    }

    @Test
    fun `should return confidence score with OCR result`() {
        val mockEngine = MockOcrEngine()
        val testImage = File("doc/images/pattern1_score_example.png")

        if (testImage.exists()) {
            val result = mockEngine.processImage(testImage)

            assertTrue(result.confidence >= 0.0)
            assertTrue(result.confidence <= 1.0)
        }
    }

    @Test
    fun `should handle non-existent image files`() {
        val mockEngine = MockOcrEngine()
        val result = mockEngine.processImage(File("nonexistent.png"))

        assertFalse(result.success)
        assertNotNull(result.error)
    }

    // === OCR Result Tests ===

    @Test
    fun `OCR result should contain detected text`() {
        val result = OcrResult(
            success = true,
            text = "Amen Break",
            confidence = 0.95,
            detectedNotes = emptyList()
        )

        assertTrue(result.success)
        assertEquals("Amen Break", result.text)
        assertEquals(0.95, result.confidence)
    }

    @Test
    fun `OCR result should support detected note positions`() {
        val notes = listOf(
            DetectedNote(instrument = "kick", step = 1, confidence = 0.9),
            DetectedNote(instrument = "snare", step = 5, confidence = 0.85)
        )

        val result = OcrResult(
            success = true,
            text = "",
            confidence = 0.87,
            detectedNotes = notes
        )

        assertEquals(2, result.detectedNotes.size)
        assertEquals("kick", result.detectedNotes[0].instrument)
        assertEquals(1, result.detectedNotes[0].step)
    }

    @Test
    fun `should create failed OCR result with error message`() {
        val result = OcrResult.failure("Image format not supported")

        assertFalse(result.success)
        assertEquals("Image format not supported", result.error)
        assertEquals(0.0, result.confidence)
    }

    // === Notation Parser Tests ===

    @Test
    fun `should parse OCR result into pattern data`() {
        val parser = NotationParser()
        val notes = listOf(
            DetectedNote(instrument = "kick", step = 1, confidence = 0.9),
            DetectedNote(instrument = "kick", step = 5, confidence = 0.9),
            DetectedNote(instrument = "snare", step = 5, confidence = 0.85),
            DetectedNote(instrument = "snare", step = 13, confidence = 0.85)
        )

        val ocrResult = OcrResult(
            success = true,
            text = "Test Pattern",
            confidence = 0.87,
            detectedNotes = notes
        )

        val patternData = parser.parseToPatternData(ocrResult)

        assertNotNull(patternData)
        assertEquals(2, patternData.voices.size)
        assertTrue(patternData.voices.containsKey("kick"))
        assertTrue(patternData.voices.containsKey("snare"))
        assertEquals(listOf(1, 5), patternData.voices["kick"])
        assertEquals(listOf(5, 13), patternData.voices["snare"])
    }

    @Test
    fun `should filter low confidence notes`() {
        val parser = NotationParser(minConfidence = 0.8)
        val notes = listOf(
            DetectedNote(instrument = "kick", step = 1, confidence = 0.9),
            DetectedNote(instrument = "kick", step = 5, confidence = 0.6), // Too low
            DetectedNote(instrument = "snare", step = 5, confidence = 0.85)
        )

        val ocrResult = OcrResult(
            success = true,
            text = "",
            confidence = 0.8,
            detectedNotes = notes
        )

        val patternData = parser.parseToPatternData(ocrResult)

        assertEquals(listOf(1), patternData.voices["kick"])
        assertEquals(listOf(5), patternData.voices["snare"])
    }

    // === Instrument Mapping Tests ===

    @Test
    fun `should map common drum notation terms to PO12 voices`() {
        val mapper = InstrumentMapper()

        assertEquals("kick", mapper.mapToVoice("bass drum"))
        assertEquals("kick", mapper.mapToVoice("bd"))
        assertEquals("kick", mapper.mapToVoice("kick"))

        assertEquals("snare", mapper.mapToVoice("snare drum"))
        assertEquals("snare", mapper.mapToVoice("sd"))
        assertEquals("snare", mapper.mapToVoice("snare"))

        assertEquals("closed-hh", mapper.mapToVoice("closed hi-hat"))
        assertEquals("closed-hh", mapper.mapToVoice("chh"))

        assertEquals("open-hh", mapper.mapToVoice("open hi-hat"))
        assertEquals("open-hh", mapper.mapToVoice("ohh"))
    }

    @Test
    fun `should handle unknown instruments gracefully`() {
        val mapper = InstrumentMapper()

        val result = mapper.mapToVoice("tambourine")

        // Should return null or a default for unknown instruments
        assertTrue(result == null || result == "unknown")
    }

    // === OCR Preprocessor Tests ===

    @Test
    fun `should validate image suitable for OCR`() {
        val preprocessor = OcrPreprocessor()
        val testImage = File("doc/images/pattern1_score_example.png")

        if (testImage.exists()) {
            val isValid = preprocessor.validateImage(testImage)
            assertTrue(isValid)
        }
    }

    @Test
    fun `should detect if image is too small for reliable OCR`() {
        val preprocessor = OcrPreprocessor(minWidth = 800, minHeight = 600)

        // Mock a small image validation
        val result = preprocessor.checkDimensions(width = 100, height = 100)

        assertFalse(result)
    }

    @Test
    fun `should suggest preprocessing steps for image`() {
        val preprocessor = OcrPreprocessor()

        val suggestions = preprocessor.suggestPreprocessing(
            brightness = 0.3,  // Too dark
            contrast = 0.9
        )

        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.contains("brightness", ignoreCase = true) })
    }
}

/**
 * Mock OCR engine for testing
 */
class MockOcrEngine : OcrEngine {
    override fun processImage(imageFile: File): OcrResult {
        if (!imageFile.exists()) {
            return OcrResult.failure("File not found: ${imageFile.path}")
        }

        // Mock successful OCR result
        return OcrResult(
            success = true,
            text = "Mock OCR Text",
            confidence = 0.95,
            detectedNotes = listOf(
                DetectedNote("kick", 1, 0.9),
                DetectedNote("snare", 5, 0.85)
            )
        )
    }
}
