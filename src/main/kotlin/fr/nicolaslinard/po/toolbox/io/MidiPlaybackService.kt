package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequencer

class MidiPlaybackService {

    companion object {
        private const val DEFAULT_RESOLUTION = 96
        private const val END_OF_TRACK = 0x2F
        private const val STEPS_PER_BAR = 16
        private const val DRUM_NOTE_DURATION = 12 // Short percussive hit (half a 16th note at 96 PPQ)
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

    val currentStep: Int
        get() {
            val seq = sequencer ?: return 0
            if (!isPlaying) return 0
            val resolution = seq.sequence?.resolution ?: DEFAULT_RESOLUTION
            return tickToStep(seq.tickPosition, resolution)
        }

    fun play(pattern: PO12Pattern) {
        stop()
        val seq = ensureSequencer()
        val sequence = midiExporter.createSequence(listOf(pattern), playbackOptions)
        seq.sequence = sequence

        val ticksPerStep = playbackOptions.resolution / 4
        loopEndTick = (STEPS_PER_BAR * ticksPerStep).toLong()

        if (isLooping) {
            seq.loopStartPoint = 0
            seq.loopEndPoint = loopEndTick
            seq.loopCount = Sequencer.LOOP_CONTINUOUSLY
        }
        seq.start()
        isPlaying = true
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

    fun toggleLoop() {
        isLooping = !isLooping
        sequencer?.let { seq ->
            if (isLooping) {
                val ticksPerStep = playbackOptions.resolution / 4
                loopEndTick = (STEPS_PER_BAR * ticksPerStep).toLong()
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
