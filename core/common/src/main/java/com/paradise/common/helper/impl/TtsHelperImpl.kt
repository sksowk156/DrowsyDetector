package com.paradise.common.helper.impl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.paradise.common.helper.TtsHelper
import com.paradise.common.network.TTS_FINISHING
import com.paradise.common.network.TTS_SPEAKING
import com.paradise.common.network.TTS_WAITING
import java.util.Locale
import javax.inject.Inject

class TtsHelperImpl @Inject constructor(
    private val fragment: Fragment,
) : TextToSpeech.OnInitListener, TtsHelper {
    private var contextRef: Context? = null
    override fun initTtsHelper() {
        contextRef = fragment.requireContext()
    }


    private var tts: TextToSpeech? = null
    override fun initTTS() {
        contextRef?.let { context ->
            tts = TextToSpeech(context, this)
        }
    }

    private var _isInitialized = MutableLiveData<Boolean>(false)

    override val isInitialized: LiveData<Boolean> get() = _isInitialized

    private var _isSpeaking = MutableLiveData<Int>(TTS_WAITING)
    override val isSpeaking: LiveData<Int> get() = _isSpeaking


    override fun stopTtsHelper() {
        tts?.stop()
        _isSpeaking.postValue(TTS_WAITING)
    }

    override fun releaseTtsHelper() {
        stopTtsHelper()
        tts?.shutdown()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                showToast("음성 서비스를 사용할 수 없습니다.")
                _isInitialized.value = false
                Log.e("whatisthis", "The Language not supported!")
                // 다운로드 화면으로 넘어가는 코드인데 일단 제외시켜두자
//                val installIntent = Intent()
//                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
//                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                temp.startActivity(installIntent)
            } else {
                _isInitialized.value = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.postValue(TTS_SPEAKING) // 여기서 livedata는 setValue는 동작하지 않는다.
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.postValue(TTS_FINISHING)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.postValue(TTS_WAITING)
                        Log.e("whatisthis", "에러 발생 $utteranceId")
                    }
                })
            }
        } else {
            Log.e("whatisthis", "error")
        }
    }

    override fun speakOut(text: String) {
        if (_isInitialized.value == true) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }
}