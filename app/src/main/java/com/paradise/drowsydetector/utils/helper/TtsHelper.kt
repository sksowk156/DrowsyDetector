//package com.paradise.drowsydetector.utils.helper
//
//import android.content.Context
//import android.speech.tts.TextToSpeech
//import android.speech.tts.UtteranceProgressListener
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.paradise.drowsydetector.utils.TTS_FINISHING
//import com.paradise.drowsydetector.utils.TTS_SPEAKING
//import com.paradise.drowsydetector.utils.TTS_WAITING
//import com.paradise.drowsydetector.utils.showToast
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import java.lang.ref.WeakReference
//import java.util.Locale
//
//class TtsHelper(private var contextRef: WeakReference<Context>) : TextToSpeech.OnInitListener { // 서비스에서 사용하면 계속 메모리 릭이 뜬다.
//    companion object {
//        @Volatile
//        private var instance: TtsHelper? = null
//        fun getInstance(context: Context) = instance ?: synchronized(this) {
//            // LocationSercie 객체를 생성할 때 같이 한번만 객체를 생성한다.
//            instance ?: TtsHelper(
//                WeakReference(context),
//            ).also {
//                instance = it
//            }
//        }
//    }
//
//    private var tts: TextToSpeech? = null
//    fun initTTS() {
//        contextRef.get()?.let { context ->
//            tts = TextToSpeech(context, instance)
//        }
//    }
//
//    private var _isInitialized = MutableLiveData<Boolean>(false)
//    val isInitialized: LiveData<Boolean> get() = _isInitialized
//
//    private var _isSpeaking = MutableLiveData<Int>(TTS_WAITING)
//    val isSpeaking: LiveData<Int> get() = _isSpeaking
//
//
//    fun stopTtsHelper() {
//        tts?.stop()
//        _isSpeaking.postValue(TTS_WAITING)
//    }
//    fun releaseTtsHelper() {
//        stopTtsHelper()
//        tts?.shutdown()
//    }
//
//    fun clearContext() {
//        releaseTtsHelper()
//        tts?.setOnUtteranceProgressListener(null)
//
//        tts = null
//        contextRef.clear()
//        instance = null
//    }
//
//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            val result = tts?.setLanguage(Locale.getDefault())
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                showToast("음성 서비스를 사용할 수 없습니다.")
//                _isInitialized.value = false
//                Log.e("whatisthis", "The Language not supported!")
//                // 다운로드 화면으로 넘어가는 코드인데 일단 제외시켜두자
////                val installIntent = Intent()
////                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
////                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////                temp.startActivity(installIntent)
//            } else {
//                _isInitialized.value = true
//                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
//                    override fun onStart(utteranceId: String?) {
//                        _isSpeaking.postValue(TTS_SPEAKING) // 여기서 livedata는 setValue는 동작하지 않는다.
//                    }
//
//                    override fun onDone(utteranceId: String?) {
//                        _isSpeaking.postValue(TTS_FINISHING)
//                    }
//
//                    @Deprecated("Deprecated in Java")
//                    override fun onError(utteranceId: String?) {
//                        _isSpeaking.postValue(TTS_WAITING)
//                        Log.e("whatisthis", "에러 발생 $utteranceId")
//                    }
//                })
//            }
//        } else {
//            Log.e("whatisthis", "error")
//        }
//    }
//
//    fun speakOut(text: String) {
//        if (_isInitialized.value == true) {
//            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
//        }
//    }
//
//}