package fr.nicolaslinard.po.toolbox.ocr

/**
 * Parses OCR results into pattern data structures.
 */
class NotationParser(
    private val minConfidence: Double = 0.7
) {

    /**
     * Parse OCR result into pattern data.
     * Groups detected notes by instrument and filters by confidence.
     */
    fun parseToPatternData(ocrResult: OcrResult): PatternData {
        if (!ocrResult.success) {
            return PatternData(voices = emptyMap())
        }

        // Group notes by instrument and filter by confidence
        val voices = ocrResult.detectedNotes
            .filter { it.confidence >= minConfidence }
            .groupBy { it.instrument }
            .mapValues { (_, notes) ->
                notes.map { it.step }.sorted().distinct()
            }

        return PatternData(
            voices = voices,
            metadata = mapOf(
                "source" to "OCR",
                "confidence" to ocrResult.confidence.toString()
            )
        )
    }

    /**
     * Merge multiple OCR results (e.g., from processing multiple images).
     */
    fun mergeResults(results: List<OcrResult>): PatternData {
        val allNotes = results
            .filter { it.success }
            .flatMap { it.detectedNotes }

        val mergedResult = OcrResult(
            success = true,
            text = "",
            confidence = results.mapNotNull { if (it.success) it.confidence else null }.average(),
            detectedNotes = allNotes
        )

        return parseToPatternData(mergedResult)
    }
}
