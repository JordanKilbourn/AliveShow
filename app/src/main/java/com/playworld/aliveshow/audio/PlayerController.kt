package com.playworld.aliveshow.audio

import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

/**
 * Minimal wrapper around MediaPlayer for our preview.
 * - setSource(uri)
 * - togglePlay(), stop()
 * - amplitude is NOT here (we use AudioAnalyzer + Visualizer)
 * - exposes audioSessionId so the analyzer can attach
 */
class PlayerController(private val context: Context) {

    private var player: MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setOnCompletionListener { isPrepared = false }
    }

    private var isPrepared = false
    val audioSessionId: Int get() = player.audioSessionId

    fun setSource(uri: Uri) {
        try {
            player.reset()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            // SAF uri – no storage permission needed
            val fd = context.contentResolver.openAssetFileDescriptor(uri, "r")
            fd?.use {
                player.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            player.prepare()
            isPrepared = true
        } catch (e: Exception) {
            e.printStackTrace()
            isPrepared = false
        }
    }

    fun togglePlay() {
        if (!isPrepared) return
        if (player.isPlaying) player.pause() else player.start()
    }

    fun stop() {
        if (!isPrepared) return
        player.pause()
        player.seekTo(0)
    }

    fun release() {
        try {
            player.stop()
        } catch (_: Throwable) {}
        player.release()
    }
}