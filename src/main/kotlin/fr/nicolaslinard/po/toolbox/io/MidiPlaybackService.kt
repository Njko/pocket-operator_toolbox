package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternChain
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequencer

data class BarAndStep(val bar: Int, val step: Int)

class MidiPlaybackService {

    companion object {
        private const val DEFAULT_RESOLUTION = 96
        private const val END_OF_TRACK = 0x2F
        private const val STEPS_PER_BAR = 16
        private const val DRUM_NOTE_DURATION = 12
    }

    private val midiExporter = MidiExporter()
    private var sequencer: Sequencer? = null

    val playbackOptions = MidiExportOptions(
        resolution = DEFAULT_RESOLUTION,
        noteDuration = DRUM_NOTE_DURATION
    )

    @Volatile
    var isPlaying: Boolean = false
        private set

    var isLooping: Boolean = false
        private set

    var onPlaybackStopped: (() -> Unit)? = null

    var loopEndTick: Long = 0L
        private set

    var totalBars: Int = 1
        private set

    val currentStep: Int
        get() {
            val seq = sequencer ?: return 0
            if (!isPlaying) return 0
            val resolution = seq.sequence?.resolution ?: DEFAULT_RESOLUTION
            return tickToStep(seq.tickPosition, resolution)
        }

    val currentBar: Int
        get() {
            val seq = sequencer ?: return 0
            if (!isPlaying) return 0
            val resolution = seq.sequence?.resolution ?: DEFAULT_RESOLUTION
            return tickToBarAndStep(seq.tickPosition, resolution).bar
        }

    fun play(pattern: PO12Pattern) {
        playPatterns(listOf(pattern), 1)
    }

    fun playChain(chain: PatternChain) {
        val patterns = chain.getPatternsInSequence()
        playPatterns(patterns, patterns.size)
    }

    fun stop() {
        val wasPlaying = isPlaying
        sequencer?.let {
            if (it.isRunning) it.stop()
        }
        isPlaying = false
        if (wasPlaying) {
            onPlaybackStopped?.invoke()
        }
    }

    fun tickToStep(tick: Long, resolution: Int = DEFAULT_RESOLUTION): Int {
        val ticksPerStep = resolution / 4
        val ticksPerBar = STEPS_PER_BAR * ticksPerStep
        val tickInBar = (tick % ticksPerBar).toInt()
        return (tickInBar / ticksPerStep) + 1
    }

    fun tickToBarAndStep(tick: Long, resolution: Int = DEFAULT_RESOLUTION): BarAndStep {
        val ticksPerStep = resolution / 4
        val ticksPerBar = STEPS_PER_BAR * ticksPerStep
        val bar = (tick / ticksPerBar).toInt() + 1
        val tickInBar = (tick % ticksPerBar).toInt()
        val step = (tickInBar / ticksPerStep) + 1
        return BarAndStep(bar, step)
    }

    fun toggleLoop() {
        isLooping = !isLooping
        sequencer?.let { seq ->
            if (isLooping) {
                seq.loopStartPoint = 0
                seq.loopEndPoint = loopEndTick
                seq.loopCount = Sequencer.LOOP_CONTINUOUSLY
            } else {
                seq.loopCount = 0
            }
        }
    }

    fun dispose() {
        stop()
        sequencer?.close()
        sequencer = null
    }

    private fun playPatterns(patterns: List<PO12Pattern>, barCount: Int) {
        stop()
        val seq = ensureSequencer()
        val sequence = midiExporter.createSequence(patterns, playbackOptions)
        seq.sequence = sequence

        totalBars = barCount
        val ticksPerStep = playbackOptions.resolution / 4
        loopEndTick = (STEPS_PER_BAR * ticksPerStep * barCount).toLong()

        if (isLooping) {
            seq.loopStartPoint = 0
            seq.loopEndPoint = loopEndTick
            seq.loopCount = Sequencer.LOOP_CONTINUOUSLY
        }
        seq.start()
        isPlaying = true
    }

    private fun ensureSequencer(): Sequencer {
        val seq = sequencer
        if (seq != null && seq.isOpen) return seq
        seq?.close()
        val newSeq = MidiSystem.getSequencer()
        newSeq.open()
        newSeq.addMetaEventListener { event ->
            if (event.type == END_OF_TRACK && !isLooping) {
                isPlaying = false
                onPlaybackStopped?.invoke()
            }
        }
        sequencer = newSeq
        return newSeq
    }
}
