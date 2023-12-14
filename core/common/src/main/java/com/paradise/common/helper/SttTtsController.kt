package com.paradise.common.helper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

interface SttTtsController {
    fun initSttTtsController()

    val request : MutableLiveData<String>
    fun releaseSttTtsController()
    fun speakOutTtsHelper(word: String)

     val request : MutableLiveData<String>
    fun speakOutTtsHelper(
        lifecycleOwner: LifecycleOwner,
        initObserver: Observer<Int>,
        speakOutWord: String,
    )
    fun checkTtsSttHelperReady(): Boolean
}