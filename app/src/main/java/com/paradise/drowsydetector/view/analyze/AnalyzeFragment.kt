package com.paradise.drowsydetector.view.analyze

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentAnalyzeBinding
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import com.paradise.drowsydetector.utils.LocationHelper
import com.paradise.drowsydetector.utils.OvalOverlayView
import com.paradise.drowsydetector.utils.calRatio
import com.paradise.drowsydetector.utils.inflateResetMenu
import com.paradise.drowsydetector.utils.showToast
import com.paradise.drowsydetector.viewmodel.AnalyzeViewModel
import com.paradise.drowsydetector.viewmodel.MusicViewModel
import com.paradise.drowsydetector.viewmodel.SettingViewModel
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class AnalyzeFragment :
    BaseViewbindingFragment<FragmentAnalyzeBinding>(FragmentAnalyzeBinding::inflate) {
    private val analyzeViewModel: AnalyzeViewModel by activityViewModels()
    private val settingViewModel : SettingViewModel by activityViewModels()
    private val musicViewModel : MusicViewModel by activityViewModels()

    private lateinit var cameraExecutor: ExecutorService

    private val faceDetectorOption by lazy {
        FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
    }

    private val faceMeshOption by lazy {
        FaceMeshDetectorOptions.Builder().setUseCase(FaceMeshDetectorOptions.FACE_MESH).build()
    }

    private val faceDetector by lazy {
        FaceDetection.getClient(faceDetectorOption)
    }

    private val faceMesh by lazy {
        FaceMeshDetection.getClient(faceMeshOption)
    }

    private val mFusedLocationProviderClient by lazy {
        LocationHelper.getInstance(getApplicationContext().fusedLocationProviderClient, getApplicationContext().geocoder)
    }

    private lateinit var overlay: OvalOverlayView

    private var standard: Double? = null
    private lateinit var timer: CountDownTimer
    var isInAngleRange = false
    var isInDrowsyState = false
    var timeCheckDrowsy: Long? = null
    val standardRatioList = mutableListOf<Double>()
    private var toneGenerator: ToneGenerator? = null

    override fun onDestroyViewInFragMent() {
        stopTimer()
        faceDetector.close()
        faceMesh.close()
        cameraExecutor.shutdown()
    }
    override fun onViewCreated() {
        // 툴바 세팅
        binding.analyzeToolbar
            .setToolbarMenu("졸음 감지", true)
            .inflateResetMenu {
                binding.analyzeTextGazerequest.visibility = View.VISIBLE
                standard = null
            }

        cameraExecutor = Executors.newSingleThreadExecutor()
        overlay = binding.analyzeOverlay

        val msFuture: Long = 2000
        binding.analyzeProgress.setMax(msFuture.toInt())

        timer = object : CountDownTimer(msFuture, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(msUntilFinished: Long) {
                binding.analyzeProgress.visibility = View.VISIBLE
                binding.analyzeTextGazerequest.text = "2초간 응시해주세요\n(눈 크기 측정 중)"
            }

            override fun onFinish() {
                binding.analyzeProgress.visibility = View.INVISIBLE
                binding.analyzeTextGazerequest.text = "얼굴을 정방향으로 유지해주세요"
                binding.analyzeTextGazerequest.visibility = View.INVISIBLE

                if (standardRatioList.size > 0) {
                    standardRatioList.sort()
                    val mid = standardRatioList.slice(3 until standardRatioList.size - 3)
                    standard = mid.average()
                    showToast("설정 완료")
                }
            }
        }

        startCamera()
    }

    private val record = mutableListOf<Double>()

    private fun startCamera() {
        var cameraController = LifecycleCameraController(requireContext())

        val previewView: PreviewView = binding.analyzeViewFinder

        // 전면 카메라
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController.bindToLifecycle(this)

        // 이미지 분석
        previewView.controller = cameraController.apply {
            setImageAnalysisAnalyzer(cameraExecutor,
                MlKitAnalyzer(
                    listOf(faceDetector, faceMesh),
                    CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    ContextCompat.getMainExecutor(requireContext())
                ) { result: MlKitAnalyzer.Result? ->
                    // 화면 회전 시 쓰레드가 종료되는 도중에 binding이 null이 되기 때문에 binding이 먼저 null 되면 쓰레드 내부가 동작하지 못하게 막는다.(nullpointexception 방지)
                    if (this@AnalyzeFragment.isAdded) {
                        // 분석 결과(랜드마크)
                        val analyzeResult1 = result?.getValue(faceDetector)
                        val analyzeResult2 = result?.getValue(faceMesh)

                        // 아무것도 인식하지 못할 땐 return
                        if (analyzeResult1 == null || analyzeResult2 == null || analyzeResult1.size == 0 || analyzeResult2.size == 0) {
                            previewView.setOnTouchListener { _, _ -> false } //no-op
                            return@MlKitAnalyzer
                        }

                        val resultDetector = analyzeResult1[0]
                        val resultMesh = analyzeResult2[0].allPoints

                        val upDownAngle = resultDetector.headEulerAngleX
                        val leftRightAngle = resultDetector.headEulerAngleY

                        val eyeRatio = calRatio(upDownAngle, leftRightAngle, resultMesh)
                        record.add(eyeRatio)

                        if (standard == null) {
                            if (upDownAngle < 4 && upDownAngle > -4 && leftRightAngle < 4 && leftRightAngle > -4) {
                                overlay.onZeroAngle(1)
                                if (!isInAngleRange) {
                                    startTimer()
                                    isInAngleRange = true
                                } else {
                                    standardRatioList.add(eyeRatio)
                                }
                            } else {
                                if (upDownAngle > 4 && leftRightAngle < 4 && leftRightAngle > -4) binding.analyzeTextGazerequest.text =
                                    "고개를 살짝만 내려 주세요"
                                if (upDownAngle < -4 && leftRightAngle < 4 && leftRightAngle > -4) binding.analyzeTextGazerequest.text =
                                    "고개를 살짝만 들어 주세요"
                                if ((leftRightAngle > 4 || leftRightAngle < -4) && upDownAngle < 4 && upDownAngle > -4) binding.analyzeTextGazerequest.text =
                                    "고개를 살짝만 돌려 주세요"

                                overlay.onZeroAngle(3)
                                if (isInAngleRange) {
                                    isInAngleRange = false
                                    stopTimer()
                                    standardRatioList.clear()
                                }
                            }
                        } else {
                            val ratioRange = standard!!
                            val eyeState = eyeRatio / ratioRange
                            var limit = 0.78
                            if (leftRightAngle < -60 || leftRightAngle > 60 || upDownAngle < -40 || upDownAngle > 40) {
                                // 카메라 정면 요청
                                binding.analyzeTextScreenrequest.visibility = View.VISIBLE
                                binding.analyzeTextDrowsycheck.visibility = View.INVISIBLE
                                overlay.onZeroAngle(2)
                            } else {
                                binding.analyzeTextScreenrequest.visibility = View.INVISIBLE
                                overlay.onZeroAngle(1)
                            }

                            // 비율이 제한을 벗어나거나 웃지 않을 때 (웃을 때 눈 웃음 때문에 눈 작아짐)
                            if (eyeState <= limit && resultDetector.smilingProbability!! <= 0.9) {
                                if (!isInDrowsyState) {
                                    isInDrowsyState = true
                                    timeCheckDrowsy = Date().time
                                }

                                if (timeCheckDrowsy != null) {
                                    val maintainTime = Date().time - timeCheckDrowsy!!
                                    // 졸음 감지!!!!!!!!!!!!!!!!!!!!!!!!!!
                                    if (maintainTime > 1800) {
                                        if (analyzeViewModel.checkDrowsy) {
                                            analyzeViewModel.checkDrowsy = false
                                            mFusedLocationProviderClient.apply {
                                                setLastLocationEventListener {
                                                    getReverseGeocoding(it.latitude, it.longitude) {
                                                        val tempLocation =
                                                            Location(LocationManager.GPS_PROVIDER)
                                                        tempLocation.latitude = it.latitude
                                                        tempLocation.longitude = it.longitude

//                                                        analyzeViewModel.getAllParkingLot2(
//                                                            ctprvnNm = it.adminArea,
//                                                            signguNm = (it.locality
//                                                                ?: it.subLocality),
//                                                            latitude = it.latitude,
//                                                            longitude = it.longitude
//                                                        )
                                                    }
                                                }
                                            }
                                        }


                                        binding.analyzeTextDrowsycheck.visibility = View.VISIBLE
                                        if (toneGenerator == null) beep(
                                            ToneGenerator.TONE_CDMA_ABBR_ALERT,
                                            500,
                                            ToneGenerator.MAX_VOLUME
                                        )
                                    }
                                }
                            } else {
                                if (isInDrowsyState) {
                                    analyzeViewModel.checkDrowsy = true
                                    binding.analyzeTextDrowsycheck.visibility = View.INVISIBLE
                                    isInDrowsyState = false
                                    timeCheckDrowsy = null
                                }
                            }
//                        Log.d("whatisthis", "상태 : " + eyeState + " ")
                        }
                    }
                })
        }
    }

//    fun findDrowsyShelter() {
//        mFusedLocationProviderClient.apply {
//            setLastLocationEventListener {
//                getReverseGeocoding(it.latitude, it.longitude) {
//                    val addressString = StringBuilder()
//                    with(it) {
//                        if (adminArea != null) {
//                            addressString.append(adminArea)
//                            addressString.append(" ")
//                        }
//                        if (locality != null) {
//                            addressString.append(locality)
//                            addressString.append(" ")
//                        }
//                        if (subLocality != null) {
//                            addressString.append(subLocality)
//                            addressString.append(" ")
//                        }
//                    }
////                    Log.d("whatisthis", addressString.toString())
//                }
//            }
//        }
//    }


    fun beep(mediaFileRawId: Int, duration: Int, volume: Int) {
        if (toneGenerator == null) toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volume)

        toneGenerator!!.startTone(mediaFileRawId, duration)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            if (toneGenerator != null) {
                toneGenerator!!.release()
                toneGenerator = null
            }
        }, 200)
    }

    fun startTimer() {
        timer.start()
    }

    fun stopTimer() {
        timer.cancel()
        binding.analyzeProgress.visibility = View.INVISIBLE
        binding.analyzeTextGazerequest.text = "얼굴을 정방향으로 유지해주세요"
    }

}