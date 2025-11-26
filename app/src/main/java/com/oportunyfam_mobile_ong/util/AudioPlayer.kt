package com.oportunyfam_mobile_ong.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioUrl: String? = null
    private var onProgressUpdate: ((current: Int, total: Int) -> Unit)? = null

    companion object {
        private const val TAG = "AudioPlayer"
    }

    /**
     * Reproduz um áudio de uma URL ou arquivo local
     */
    fun playAudio(
        audioUrl: String,
        onCompletion: () -> Unit = {},
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ) {
        this.onProgressUpdate = onProgress
        try {
            // Se já está tocando o mesmo áudio, pausar
            if (currentAudioUrl == audioUrl && mediaPlayer?.isPlaying == true) {
                pauseAudio()
                return
            }

            // Se está tocando outro áudio, parar
            if (currentAudioUrl != audioUrl) {
                stopAudio()
            }

            // Se estava pausado, retomar
            if (currentAudioUrl == audioUrl && mediaPlayer != null) {
                mediaPlayer?.start()
                return
            }

            // Iniciar novo áudio
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
                start()

                setOnCompletionListener {
                    stopAudio()
                    onCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Erro ao reproduzir áudio: what=$what, extra=$extra")
                    stopAudio()
                    true
                }
            }

            currentAudioUrl = audioUrl
            Log.d(TAG, "Reproduzindo áudio: $audioUrl")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao reproduzir áudio", e)
            stopAudio()
        }
    }

    /**
     * Pausa o áudio
     */
    fun pauseAudio() {
        try {
            mediaPlayer?.pause()
            Log.d(TAG, "Áudio pausado")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao pausar áudio", e)
        }
    }

    /**
     * Para o áudio e libera recursos
     */
    fun stopAudio() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            currentAudioUrl = null
            Log.d(TAG, "Áudio parado")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar áudio", e)
        }
    }

    /**
     * Verifica se está reproduzindo
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    /**
     * Verifica se está reproduzindo uma URL específica
     */
    fun isPlayingUrl(url: String): Boolean {
        return currentAudioUrl == url && isPlaying()
    }

    /**
     * Obtém a URL do áudio atual
     */
    fun getCurrentAudioUrl(): String? {
        return currentAudioUrl
    }

    /**
     * Obtém a posição atual em milissegundos
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    /**
     * Obtém a duração total em milissegundos
     */
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
}

