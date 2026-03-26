package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternChain

/**
 * Result of the pattern creation/edit dialog.
 * Can be a single pattern or a multi-bar chain.
 */
sealed class PatternDialogResult {
    data class Single(val pattern: PO12Pattern) : PatternDialogResult()
    data class Chain(val chain: PatternChain) : PatternDialogResult()

    val patterns: List<PO12Pattern>
        get() = when (this) {
            is Single -> listOf(pattern)
            is Chain -> chain.getPatternsInSequence()
        }

    val firstPattern: PO12Pattern
        get() = when (this) {
            is Single -> pattern
            is Chain -> chain.patterns.first()
        }
}
