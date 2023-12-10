package com.paradise.common.helper

import com.core.model.musicItem
import kotlinx.coroutines.flow.StateFlow

interface MusicHelper {
    fun initMusicHelper()
    val isPrepared: StateFlow<Boolean>
    fun releaseMediaPlayer()
    fun setStandardMusic()
    fun setResMusic()
    fun setMyMusic(musicList: List<musicItem>)
    fun startMusic(resId: Int)
    fun startMusic(music: musicItem)
}