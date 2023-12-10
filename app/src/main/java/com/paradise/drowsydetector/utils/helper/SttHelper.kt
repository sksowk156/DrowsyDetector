//package com.paradise.drowsydetector.utils.helper
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.speech.RecognitionListener
//import android.speech.RecognizerIntent
//import android.speech.SpeechRecognizer
//import android.util.Log
//import androidx.lifecycle.MutableLiveData
//import com.paradise.drowsydetector.utils.showToast
//import java.lang.ref.WeakReference
//
//class SttHelper(private var contextRef: WeakReference<Context>) {
//    companion object {
//        @Volatile
//        private var instance: SttHelper? = null
//        fun getInstance(context: Context) = instance ?: synchronized(this) {
//            // LocationSercie 객체를 생성할 때 같이 한번만 객체를 생성한다.
//            instance ?: SttHelper(
//                WeakReference(context),
//            ).also {
//                instance = it
//            }
//        }
//    }
//
//    // 음성인식을 위한 필드
//    private var mRecognizer: SpeechRecognizer? = null
//    var sttResult = MutableLiveData<String>("")
//    var sttState = MutableLiveData<Boolean>(false)
//    private val duration = 4
//
//    // 음성인식을 종료하는 메소드
//    fun releaseSttHelper() {
//        mRecognizer?.run {
//            sttResult.value = ("")
//            sttState.value = false
//            stopListening()
//            destroy()
//            mRecognizer?.setRecognitionListener(null)
//            mRecognizer = null
//        }
//    }
//
//    fun clearContext() {
//        releaseSttHelper()
//        contextRef.clear()
//        instance = null
//    }
//
//    // 음성인식을 시작하는 메소드
//    fun startSTT() {
//        contextRef.get()?.let { context ->
//            Log.d("whatisthis", "stt 시작")
//            sttResult.value = ("")
//            sttState.value = true
//            // 음성인식을 위한 Intent 생성
//            val intent =
//                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH) // 음성인식을 위한 Intent 객체를 생성, RecognizerIntent.ACTION_RECOGNIZE_SPEECH 상수는 음성인식을 수행하는 액션을 나타냄
//            intent.putExtra(
//                RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName
//            )  // 음성인식을 호출하는 패키지 이름을 Intent 객체에 추가, RecognizerIntent.EXTRA_CALLING_PACKAGE 상수는 음성인식을 호출하는 패키지 이름을 나타냄
//            intent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"
//            ) // 음성인식을 위한 언어를 Intent 객체에 추가, RecognizerIntent.EXTRA_LANGUAGE 상수는 음성인식을 위한 언어를 나타내고 한국어를 사용함
//            // 음성인식을 위한 SpeechRecognizer 객체 생성
//            mRecognizer =
//                SpeechRecognizer.createSpeechRecognizer(context) // 음성인식을 위한 SpeechRecognizer 객체를 생성, createSpeechRecognizer 메소드는 현재 컨텍스트에 맞는 SpeechRecognizer 객체를 반환해줌.
//            mRecognizer?.setRecognitionListener(listener) // 음성인식을 위한 SpeechRecognizer 객체에 리스너를 설정, setRecognitionListener 메소드는 음성인식의 상태와 결과를 받을 수 있는 리스너를 설정하는 메소드
//            mRecognizer?.startListening(intent) // 음성인식을 위한 SpeechRecognizer 객체에 음성인식을 시작 명령, startListening 메소드는 Intent 객체에 설정된 옵션에 따라 음성인식을 시작하는 메소드
//            // 음성 인식을 수행할 시간만큼 타이머를 설정합니다.
//            val timer = object : CountDownTimer((duration * 1000).toLong(), 1000) {
//                override fun onTick(millisUntilFinished: Long) {
//                    // 타이머가 작동하는 동안에는 아무것도 하지 않습니다.
//                }
//
//                override fun onFinish() {
//                    // 타이머가 끝나면, 음성 인식을 종료합니다.
//                    Log.d("whatisthis", "stt 종료1")
//                    releaseSttHelper()
//                }
//            }
//            // 타이머를 시작합니다.
//            timer.start()
//        }
//    }
//
//
//    // 음성인식 결과를 처리하는 메소드
//    private val listener = object : RecognitionListener {
//        // 음성인식 준비 완료
//        override fun onReadyForSpeech(params: Bundle?) {
//            showToast("음성인식을 시작합니다.")
//        }
//
//        // 음성이 입력되기 시작
//        override fun onBeginningOfSpeech() {}
//
//        // 음성의 크기 변화
//        override fun onRmsChanged(rmsdB: Float) {}
//
//        // 음성 데이터를 받음
//        override fun onBufferReceived(buffer: ByteArray?) {}
//
//        // 음성 입력이 끝남
//        override fun onEndOfSpeech() {}
//
//        override fun onError(error: Int) {
//            sttResult.value = ("onError")
//            sttState.value = (false)
//            // 음성인식 에러 발생
//            val message = when (error) {
//                SpeechRecognizer.ERROR_AUDIO -> "오디오 입력 에러"
//                SpeechRecognizer.ERROR_CLIENT -> "사용자측 에러"
//                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 없음 에러"
//                SpeechRecognizer.ERROR_NETWORK -> "네트워크 연결 에러"
//                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 연결 시간 초과 에러"
//                SpeechRecognizer.ERROR_NO_MATCH -> "음성 인식 결과를 찾을 수 없음 에러"
//                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식 서비스가 바쁨 에러"
//                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함 에러"
//                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과 에러"
//                else -> "알 수 없는 오류"
//            }
//            Log.e("whatisthis", message)
//            showToast("에러 발생 : $message")
//        }
//
//        // 음성인식 결과를 받음
//        override fun onResults(results: Bundle?) {
//            val key = SpeechRecognizer.RESULTS_RECOGNITION // 음성인식 결과를 나타내는 키 값
//            val mResult =
//                results?.getStringArrayList(key) // 번들 객체에서 음성인식 결과를 문자열 리스트로 가져옴. 음성인식 결과는 여러 개의 후보가 있을 수 있다.
//            val rs = mResult?.toTypedArray() // 문자열 리스트를 문자열 배열로 변환합니다.
//            var result = checkWord(rs?.get(0) ?: "")
//            sttResult.value = (result) // 텍스트뷰에 음성인식 결과의 첫 번째 후보를 출력함.
//            sttState.value = (false)
//            Log.d("whatisthis", result.toString())
//            Log.d("whatisthis", "stt 종료2")
//        }
//
//        // 부분적인 음성인식 결과를 받음
//        override fun onPartialResults(partialResults: Bundle?) {}
//
//        // 음성인식 이벤트 발생
//        override fun onEvent(eventType: Int, params: Bundle?) {}
//    }
//
//
//    /**
//     * Set of num
//     *
//     * 음성 인식 결과가 조금 틀릴 경우 결과를 조금 수정
//     *
//     * HashMap을 쓰지 않은 이유는 HashMap은 모든 번호(1번, 2번, 3번, 4번)을 하나의 HashMap에서 관리해야 하므로, 글자가 많아지면 구분하기 힘들어서
//     *
//     * HashSet로 1번, 2번, 3번, 4번 나눠서 관리했다.
//     */
//    val setOfNum1: HashSet<String> = hashSetOf("일본", "일번", "일분", "1분")
//    val setOfNum2: HashSet<String> = hashSetOf("이번", "이분", "2분")
//    val setOfNum3: HashSet<String> = hashSetOf("삼번", "상번", "삼분", "3분")
//    val setOfNum4: HashSet<String> = hashSetOf("4분", "사분")
//    private fun checkWord(word: String) = if (setOfNum1.contains(word)) "1번"
//    else if (setOfNum2.contains(word)) "2번"
//    else if (setOfNum3.contains(word)) "3번"
//    else if (setOfNum4.contains(word)) "4번"
//    else word
//
//}