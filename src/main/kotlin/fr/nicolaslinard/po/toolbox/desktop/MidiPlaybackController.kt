package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.io.MidiPlaybackService
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternChain

class MidiPlaybackController(
    private val service: MidiPlaybackService = MidiPlaybackService()
) {

    val isPlaying: Boolean get() = service.isPlaying
    val isLooping: Boolean get() = service.isLooping
    val currentStep: Int get() = service.currentStep
    val currentBar: Int get() = service.currentBar
    val totalBars: Int get() = service.totalBars

    var onPlaybackStopped: (() -> Unit)?
        get() = service.onPlaybackStopped
        set(value) { service.onPlaybackStopped = value }

    fun play(pattern: PO12Pattern) = service.play(pattern)
    fun playChain(chain: PatternChain) = service.playChain(chain)
    fun stop() = service.stop()
    fun toggleLoop() = service.toggleLoop()
    fun dispose() = service.dispose()
}
