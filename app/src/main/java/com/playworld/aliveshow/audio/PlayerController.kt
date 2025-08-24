package com.playworld.aliveshow.audio

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Minimal no-op controller so the app builds without audio deps.
class PlayerController(@Suppress("unused") private val context: Context) {
    private val _amplitude = MutableStateFlow(0f)  // always 0 (mouth stays closed)
    val amplitude: StateFlow<Float> = _amplitude

    fun setSource(@Suppress("unused") uri: Uri) { /* no-op */ }
    fun togglePlay() { /* no-op */ }
    fun stop() { _amplitude.value = 0f }
    fun release() { /* no-op */ }
}
