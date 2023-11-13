package com.paradise.drowsydetector.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.paradise.drowsydetector.data.local.room.music.Music
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicHelper {

    private var mediaPlayer: MediaPlayer? = null
    private var job: Job? = null
    fun playMusic(context: Context, selectedMusic: Music, lifecycleOwner: LifecycleOwner) {
        checkMediaPlayer()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(
                context,
                getUriFromFilePath(context, selectedMusic.newPath!!)!!
            )

            setOnPreparedListener {
                seekTo(selectedMusic.startTime.toInt())
                start()
            }

            setOnCompletionListener {
                stop()
                release()
            }

            prepareAsync()
        }

        job = lifecycleOwner.lifecycleScope.launch {
            delay(selectedMusic.durationTime)
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        }
    }

    fun checkMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
            job = null
        }
    }
}