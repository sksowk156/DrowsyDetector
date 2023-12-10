package com.paradise.common.helper

import android.content.Context
import android.media.AudioManager

interface VolumeHelper {
    fun initVolumeHelper()
    fun releaseVolumeHelper()
    fun initAudio()

    fun getMaxVolume() : Int?

    fun getCurrentVolume() : Int?

    fun setVolume(volume: Int)
}