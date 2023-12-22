package com.paradise.common.helper.impl

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.paradise.common.helper.SttHelper
import com.paradise.common.helper.SttTtsController
import com.paradise.common.helper.SttTtsService
import com.paradise.common.helper.TtsHelper
import com.paradise.common.network.CHECKUSESTTSERVICE
import com.paradise.common.network.SELECTGUIDESETTING
import com.paradise.common.network.SELECTMUSICSETTING
import com.paradise.common.network.SELECTSERVICE
import com.paradise.common.network.TTS_FINISHING
import com.paradise.common.network.TTS_SPEAKING
import javax.inject.Inject

class SttTtsControllerImpl @Inject constructor(
    private val sttHelper: SttHelper,
    private val ttsHelper: TtsHelper,
    private val fragment: Fragment,
) : SttTtsController {
    private var lifecycleOwner: LifecycleOwner? = null
    private var sttTtsService: SttTtsService? = null

    override fun initSttTtsController() {
        lifecycleOwner = fragment.viewLifecycleOwner
        sttTtsService = fragment as SttTtsService

        ttsHelper.initTtsHelper()
        ttsHelper.initTTS()
        sttHelper.initSttHelper()

        subscribeSttResult()
    }

    override fun stopSttTtsController() {
        ttsHelper.stopTtsHelper()
        sttHelper.stopSttHelper()
    }

    override fun releaseSttTtsController() {
        lifecycleOwner?.let {
            sttHelper.sttResult.removeObservers(it) // observer 제거, 결과 받고 나서 제거
            ttsHelper.isSpeaking.removeObservers(it)
            request.removeObservers(it)
        }
        ttsHelper.releaseTtsHelper()
        sttHelper.releaseSttHelper()
    }

    fun resetSttHelper(lifecycleOwner: LifecycleOwner) {
        sttHelper.sttResult.removeObservers(lifecycleOwner) // observer 제거, 결과 받고 나서 제거
        sttHelper.stopSttHelper() // stt 종료
    }

    fun resetTtsHelper(lifecycleOwner: LifecycleOwner) {
        ttsHelper.isSpeaking.removeObservers(lifecycleOwner)
        ttsHelper.stopTtsHelper() // tts 멈춤
    }

    override val request = MutableLiveData<String>()
    override fun speakOutTtsHelper(word: String) {
        lifecycleOwner?.let { lifecycleOwner ->
            Log.d("whatisthis", "${(ttsHelper.isSpeaking.value != TTS_SPEAKING)} ${!(sttHelper.sttState.value)!!}")
            if (checkTtsSttHelperReady()) {
                resetTtsHelper(lifecycleOwner)
                if (word.isNotEmpty()) {
                    speakOutTtsHelper(lifecycleOwner, initTtsStateObserver, word)
                } else {
                    speakOutTtsHelper(lifecycleOwner, initTtsStateObserver, "최근 요청 기록이 없습니다.")
                }
            }
        }
    }

    private fun speakOutTtsHelper(
        lifecycleOwner: LifecycleOwner,
        initObserver: Observer<Int>,
        speakOutWord: String,
    ) {
        ttsHelper.isSpeaking.observe(lifecycleOwner, initObserver)
        ttsHelper.speakOut(speakOutWord)
    }

    private val initTtsStateObserver: Observer<Int> = Observer<Int> {
        if ((ttsHelper.isSpeaking.value == TTS_FINISHING)) { // TTS가 끝났을 때, STT가 시작하지 않았을 때
            ttsHelper.stopTtsHelper() // tts 멈춤
        }
    }

    override fun checkTtsSttHelperReady() =
        (ttsHelper.isSpeaking.value != TTS_SPEAKING && !(sttHelper.sttState.value)!!)

    private fun subscribeSttResult() {
        lifecycleOwner?.let { lifecycleOwner ->
            request.observe(lifecycleOwner) {
                if (it.isNotEmpty()) {
                    if (checkTtsSttHelperReady()) {
                        resetSttHelper(lifecycleOwner)
                        resetTtsHelper(lifecycleOwner)
                        when (it) {
                            CHECKUSESTTSERVICE -> {
                                checkUseSttService(
                                    lifecycleOwner = lifecycleOwner,
                                    initSttObserver = initObserver,
                                    initTtsObserver = initTtsSttServiceObserver,
                                    speakOutWord = "음성 인식 서비스를 사용하시겠습니까?? \n 1번 예, 2번 아니오"
                                )
                            }

                            SELECTSERVICE -> {
                                checkUseSttService(
                                    lifecycleOwner = lifecycleOwner,
                                    initSttObserver = initObserver1,
                                    initTtsObserver = initTtsSttServiceObserver,
                                    speakOutWord = "1번 음악 설정,\n 2번 휴식 공간 안내 설정,\n 3번 휴식 공간 정보 요청,\n 4번 휴식 공간 최근 정보 조회,\n 5번 앱 종료"
                                )
                            }

                            SELECTMUSICSETTING -> {
                                checkUseSttService(
                                    lifecycleOwner = lifecycleOwner,
                                    initSttObserver = initObserver2,
                                    initTtsObserver = initTtsSttServiceObserver,
                                    speakOutWord = "1번 기본 음악으로 설정,\n 2번 사용자 설정 음악"
                                )
                            }

                            SELECTGUIDESETTING -> {
                                checkUseSttService(
                                    lifecycleOwner = lifecycleOwner,
                                    initSttObserver = initObserver3,
                                    initTtsObserver = initTtsSttServiceObserver,
                                    speakOutWord = "1번 안내 받지 않음,\n 2번 안내 받음"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkUseSttService(
        lifecycleOwner: LifecycleOwner,
        initSttObserver: Observer<String>,
        initTtsObserver: Observer<Int>,
        speakOutWord: String,
    ) {
        sttHelper.sttResult.observe(lifecycleOwner, initSttObserver)
        ttsHelper.isSpeaking.observe(lifecycleOwner, initTtsObserver)
        ttsHelper.speakOut(speakOutWord)
    }

    private val initTtsSttServiceObserver: Observer<Int> = Observer<Int> {
        if ((ttsHelper.isSpeaking.value == TTS_FINISHING && !(sttHelper.sttState.value)!!)) { // TTS가 끝났을 때, STT가 시작하지 않았을 때
            sttHelper.startSTT() // stt 시작
            ttsHelper.stopTtsHelper() // tts 멈춤
        }
    }

    private val initObserver = Observer<String> {
        if (it.isNotEmpty()) { // STT 결과가 나왔을 때
            when (it) {
                "onError" -> {
                } // 에러 (말을 안한 것도 포함)
                "1번" -> {
                    request.postValue(SELECTSERVICE)
                } // 예
                "2번" -> {}// 아니오
                else -> {
                }
            }
        }
    }

    private val initObserver1 = Observer<String> {
        if (it.isNotEmpty()) { // STT 결과가 나왔을 때
            when (it) {
                "onError" -> {
                }

                "1번" -> {
                    request.postValue(SELECTMUSICSETTING)
                } // 음악 설정
                "2번" -> {
                    request.postValue(SELECTGUIDESETTING)
                } // 휴식 공간 안내 설정
                "3번" -> {
                    requestRelaxData()
                } // 휴식 공간 정보 요청
                "4번" -> {
                    requestRecentRelaxData()
                } // 휴식 공간 최근 정보 조회
                "5번" -> {
                    requestCancleAnalyze()
                } // 앱 종료
                else -> {
                }
            }
        }
    }

    private val initObserver2 = Observer<String> {
        if (it.isNotEmpty()) { // STT 결과가 나왔을 때
            when (it) {
                "onError" -> { // 에러 (말을 안한 것도 포함)
                }

                "1번" -> { // 기본 음악으로 설정
                    setBaseMusic()
                }

                "2번" -> { // 사용자 음악으로 설정
                    setUserMusic()
                }

                else -> {
                }
            }
        }
    }

    private val initObserver3 = Observer<String> {
        if (it.isNotEmpty()) { // STT 결과가 나왔을 때
            when (it) {
                "onError" -> { // 에러 (말을 안한 것도 포함)
                }

                "1번" -> { // 안내 받지 않음
                    setGuideOff()
                }

                "2번" -> { // 안내 받음
                    setGuideOn()
                }

                else -> {
                }
            }
        }
    }

    private fun setBaseMusic() {
        sttTtsService?.run {
            this.baseMusic()
        }
    }

    private fun setUserMusic() {
        sttTtsService?.run {
            this.userMusic()
        }
    }

    private fun setGuideOff() {
        sttTtsService?.run {
            this.guideOff()
        }
    }

    private fun setGuideOn() {
        sttTtsService?.run {
            this.guideOn()
        }
    }

    private fun requestRelaxData() { // 휴식 공간 정보 요청
        sttTtsService?.run {
            this.relaxData()
        }
    }

    private fun requestRecentRelaxData() { // 휴식 공간 최근 정보 조회
        sttTtsService?.run {
            this.recentRelaxData()
        }
    }

    private fun requestCancleAnalyze() {  // 앱 종료
        sttTtsService?.run {
            this.cancleAnalyze()
        }
    }
}