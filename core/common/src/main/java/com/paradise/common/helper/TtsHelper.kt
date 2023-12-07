package com.paradise.common.helper

import androidx.lifecycle.LiveData

interface TtsHelper {
    fun initTtsHelper()

    val isInitialized: LiveData<Boolean>
    val isSpeaking: LiveData<Int>
    fun initTTS()
    fun stopTtsHelper()
    fun releaseTtsHelper()
    fun speakOut(text: String)
}