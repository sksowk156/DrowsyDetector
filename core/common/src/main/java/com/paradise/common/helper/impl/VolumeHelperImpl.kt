package com.paradise.common.helper.impl

import android.content.Context
import android.media.AudioManager
import androidx.fragment.app.Fragment
import com.paradise.common.helper.VolumeHelper
import javax.inject.Inject

class VolumeHelperImpl @Inject constructor(
    private var fragment: Fragment,
) : VolumeHelper {
    private var contextRef: Context? = null
    override fun initVolumeHelper() {
        contextRef = fragment.requireContext()
    }

    override fun releaseVolumeHelper() {
        audioManager = null
        contextRef = null
    }

    private var audioManager: AudioManager? = null

    override fun initAudio() {
        contextRef?.let { context ->
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }

    override fun getMaxVolume() = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    override fun getCurrentVolume() = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)

    override fun setVolume(volume: Int) {
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }
}