package fr.nicolaslinard.po.toolbox.validation

import fr.nicolaslinard.po.toolbox.models.PO12Pattern

/**
 * Validation result for a pattern.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
    val hasWarnings: Boolean get() = warnings.isNotEmpty()
}

/**
 * Validates PO-12 patterns for correctness and best practices.
 */
class PatternValidator {

    fun validate(pattern: PO12Pattern): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Validate pattern number
        if (pattern.number !in 1..16) {
            errors.add("Pattern number must be between 1 and 16, got: ${pattern.number}")
        }

        // Validate voices
        if (pattern.voices.isEmpty()) {
            errors.add("Pattern has no drum voices programmed")
        }

        // Validate steps for each voice
        pattern.voices.forEach { (voice, steps) ->
            if (steps.isEmpty()) {
                warnings.add("Voice '${voice.displayName}' has no active steps")
            }

            steps.forEach { step ->
                if (step !in 1..16) {
                    errors.add("Voice '${voice.displayName}' has invalid step: $step (must be 1-16)")
                }
            }

            // Check for duplicate steps (shouldn't happen but validate anyway)
            if (steps.size != steps.distinct().size) {
                errors.add("Voice '${voice.displayName}' has duplicate steps")
            }
        }

        // Validate metadata
        if (pattern.metadata.name.isBlank()) {
            errors.add("Pattern name cannot be blank")
        }

        pattern.metadata.bpm?.let { bpm ->
            if (bpm !in 60..300) {
                warnings.add("BPM $bpm is outside typical range (60-300)")
            }
            // PO-12 supports 60-206 BPM
            if (bpm > 206) {
                warnings.add("BPM $bpm exceeds PO-12 maximum of 206")
            }
        }

        // Best practices warnings
        if (pattern.voices.size == 1) {
            warnings.add("Pattern only uses 1 voice - consider adding more for fuller sound")
        }

        // Check for common patterns
        val hasKick = pattern.voices.keys.any { it.shortName == "kick" }
        val hasSnare = pattern.voices.keys.any { it.shortName == "snare" }
        if (!hasKick && !hasSnare) {
            warnings.add("Pattern has no kick or snare - might lack rhythmic foundation")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Validates a pattern and throws an exception if invalid.
     */
    fun validateOrThrow(pattern: PO12Pattern) {
        val result = validate(pattern)
        if (!result.isValid) {
            throw IllegalArgumentException("Pattern validation failed:\n${result.errors.joinToString("\n")}")
        }
    }
}
