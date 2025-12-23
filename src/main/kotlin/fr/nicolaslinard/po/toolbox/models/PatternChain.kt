package fr.nicolaslinard.po.toolbox.models

/**
 * Represents a chain of multiple PO-12 patterns that play in sequence.
 * Useful for multi-bar drum phrases like the 2-bar Amen break.
 *
 * Example: Amen break uses patterns 1 and 2
 * Chain sequence: [1, 2] or "1,2" on the PO-12
 */
data class PatternChain(
    val name: String,
    val patterns: List<PO12Pattern>,
    val sequence: List<Int>, // Pattern numbers in play order
    val metadata: PatternMetadata
) {
    init {
        require(patterns.isNotEmpty()) { "Chain must contain at least one pattern" }
        require(sequence.isNotEmpty()) { "Chain sequence cannot be empty" }

        // Validate sequence references valid pattern numbers
        sequence.forEach { patternNum ->
            require(patterns.any { it.number == patternNum }) {
                "Sequence references pattern $patternNum but it's not in the chain"
            }
        }
    }

    /**
     * Total number of bars in this chain.
     * Counts unique patterns in the sequence.
     */
    val totalBars: Int get() = sequence.size

    /**
     * Format the chain sequence for PO-12 entry.
     * Example: [1, 1, 2] -> "1,1,2"
     */
    val chainSequenceString: String get() = sequence.joinToString(",")

    /**
     * Get a specific pattern by its number.
     */
    fun getPattern(number: Int): PO12Pattern? {
        return patterns.find { it.number == number }
    }

    /**
     * Get patterns in sequence order.
     */
    fun getPatternsInSequence(): List<PO12Pattern> {
        return sequence.mapNotNull { getPattern(it) }
    }
}

/**
 * Builder for creating pattern chains.
 */
class PatternChainBuilder {
    private var name: String = ""
    private val patterns = mutableListOf<PO12Pattern>()
    private val sequence = mutableListOf<Int>()
    private var metadata: PatternMetadata? = null

    fun name(name: String) = apply { this.name = name }

    fun addPattern(pattern: PO12Pattern) = apply {
        patterns.add(pattern)
    }

    fun sequence(vararg numbers: Int) = apply {
        sequence.clear()
        sequence.addAll(numbers.toList())
    }

    fun sequence(numbers: List<Int>) = apply {
        sequence.clear()
        sequence.addAll(numbers)
    }

    fun metadata(metadata: PatternMetadata) = apply {
        this.metadata = metadata
    }

    /**
     * Convenience method to set sequence from patterns.
     * Uses the order patterns were added.
     */
    fun sequenceFromPatterns() = apply {
        sequence.clear()
        sequence.addAll(patterns.map { it.number })
    }

    fun build(): PatternChain {
        require(name.isNotBlank()) { "Chain name is required" }
        require(metadata != null) { "Chain metadata is required" }

        return PatternChain(
            name = name,
            patterns = patterns.toList(),
            sequence = sequence.toList(),
            metadata = metadata!!
        )
    }
}
