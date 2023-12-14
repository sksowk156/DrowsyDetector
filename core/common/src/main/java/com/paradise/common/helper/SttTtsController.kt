package com.paradise.common.helper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

interface SttTtsController {
    fun initSttTtsController()

    val request : MutableLiveData<String>
    fun speakOutTtsHelper(word: String)
    fun checkTtsSttHelperReady(): Boolean
    fun stopSttTtsController()
    fun releaseSttTtsController()
}