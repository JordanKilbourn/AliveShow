package com.playworld.aliveshow.audio

import android.media.audiofx.Visualizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

/**
 * Lightweight real-time analyzer using Android's Visualizer.
 * Exposes:
 *  - amplitude: 0..1 RMS
 *  - beat:      discrete pulses (SharedFlow)
 *  - speech:    0..1 (very rough speech-vs-music probability)
 */
class AudioAnalyzer {

    private var visualizer: Visualizer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _speech = MutableStateFlow(0f)
    val speechProb: StateFlow<Float> = _speech.asStateFlow()

    private val _beat = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val beat: SharedFlow<Unit> = _beat.asSharedFlow()

    // State for simple onset/beat detection
    private var lastFlux = 0.0
    private var lastBeatTime = 0L

    fun attachToSession(audioSessionId: Int) {
        release()
        if (audioSessionId == Visualizer.ERROR_BAD_VALUE || audioSessionId == 0) return
        visualizer = Visualizer(audioSessionId).apply {
            // biggest window we can get
            captureSize = Visualizer.getCaptureSizeRange()[1]
            enabled = true
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(v: Visualizer?, data: ByteArray?, rate: Int) {
                    if (data == null) return
                    // RMS amplitude 0..1
                    var sum = 0.0
                    for (b in data) {
                        val f = (b.toInt() and 0xFF) - 128
                        sum += f * f
                    }
                    val rms = sqrt(sum / data.size) / 128.0
                    _amplitude.value = rms.toFloat().coerceIn(0f, 1f)
                }

                override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, rate: Int) {
                    if (fft == null) return
                    // magnitude spectrum
                    val n = fft.size / 2
                    val mags = DoubleArray(n) { i ->
                        val re = fft[2*i].toDouble()
                        val im = fft[2*i+1].toDouble()
                        sqrt(re*re + im*im) + 1e-9
                    }
                    // spectral flux (onset)
                    val flux = mags.fold(0.0) { acc, e -> acc + e }
                    val now = System.currentTimeMillis()
                    val dFlux = (flux - lastFlux) / (flux + 1e-6)
                    lastFlux = flux
                    if (dFlux > 0.08 && now - lastBeatTime > 140) { // adaptive-ish
                        lastBeatTime = now
                        _beat.tryEmit(Unit)
                    }

                    // crude speech/music heuristic via spectral flatness (0=tonal, 1=noise)
                    val logMean = mags.sumOf { ln(it) } / n
                    val flatness = exp(logMean) / (mags.sum() / n)
                    // voice tends to have LOWER flatness than noisy music; invert & smooth
                    val speechish = (1.0 - flatness).coerceIn(0.0, 1.0)
                    _speech.value = (0.8 * _speech.value + 0.2 * speechish).toFloat()
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, true)
        }
    }

    fun release() {
        scope.coroutineContext.cancelChildren()
        visualizer?.release()
        visualizer = null
    }
}