package com.paradise.drowsydetector.view.analyze

import android.graphics.Point
import android.opengl.Visibility
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseFragment
import com.paradise.drowsydetector.databinding.FragmentAnalyzeBinding
import com.paradise.drowsydetector.utils.OvalOverlayView
import kotlinx.coroutines.NonCancellable.start
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class AnalyzeFragment : BaseFragment<FragmentAnalyzeBinding>(R.layout.fragment_analyze) {
    private lateinit var cameraExecutor: ExecutorService


    private val faceDetectorOption by lazy {
        FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
//            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
//            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
//            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
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

    private lateinit var overlay: OvalOverlayView

    private var standard: Double? = null
    private lateinit var timer: CountDownTimer
    var isInAngleRange = false
    var isInDrowsyState = false
    var timeCheckDrowsy: Long? = null
    val standardRatioList = mutableListOf<Double>()

    override fun init() {
        initAppbar(binding.analyzeToolbar, null, true, "")

        cameraExecutor = Executors.newSingleThreadExecutor()

        overlay = binding.analyzeOverlay

        val msFuture: Long = 2000
        binding.analyzeProgress.setMax(msFuture.toInt())

        timer = object : CountDownTimer(msFuture, 1000) {
            override fun onTick(msUntilFinished: Long) {
//                displayText.setText("remaining sec: " + msUntilFinished / 1000)
                binding.analyzeOverlay.visibility = View.VISIBLE
                binding.analyzeProgress.visibility = View.VISIBLE
                binding.analyzeTextGazerequest.visibility = View.VISIBLE
            }

            override fun onFinish() {
                binding.analyzeOverlay.visibility = View.INVISIBLE
                binding.analyzeProgress.visibility = View.INVISIBLE
                binding.analyzeTextGazerequest.visibility = View.INVISIBLE

                if (standardRatioList.size > 0) {
                    standardRatioList.sort()
                    val mid = standardRatioList.slice(3 until standardRatioList.size - 3)
                    standard = mid.average()
                    showToast("설정 완료")
                }
//                displayText.setText("done!")
            }
        }

        startCamera()
    }

    private fun startCamera() {
        var cameraController = LifecycleCameraController(requireContext())

        val previewView: PreviewView = binding.analyzeViewFinder

        // 전면 카메라
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController.bindToLifecycle(this)

        // 이미지 분석
        previewView.controller = cameraController.apply {
            setImageAnalysisAnalyzer(cameraExecutor,
                MlKitAnalyzer(listOf(faceDetector, faceMesh),
                    CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    ContextCompat.getMainExecutor(requireContext())) { result: MlKitAnalyzer.Result? ->
                    // 분석 결과(랜드마크)
                    val analyzeResult1 = result?.getValue(faceDetector)
                    val analyzeResult2 = result?.getValue(faceMesh)

                    // 아무것도 인식하지 못할 땐 return
                    if (analyzeResult1 == null || analyzeResult2 == null || analyzeResult1.size == 0 || analyzeResult2.size == 0) {
                        previewView.setOnTouchListener { _, _ -> false } //no-op
                        return@MlKitAnalyzer
                    }

                    val angle = analyzeResult1[0]
                    val landmark = analyzeResult2[0].allPoints

                    val upDownAngle = angle.headEulerAngleX + 8 // 정면을 봤을 때 -8부터 시작해서
                    val leftRightAngle = angle.headEulerAngleY

                    val eyeRatio = calRatio(upDownAngle, leftRightAngle, landmark)

                    if (leftRightAngle < -30 || leftRightAngle > 30 || upDownAngle < -20 || upDownAngle > 20) {
                        // 카메라 정면 요청
                        binding.analyzeTextScreenrequest.visibility = View.VISIBLE
                    } else {
                        binding.analyzeTextScreenrequest.visibility = View.INVISIBLE

                        if (standard == null) {
                            if (upDownAngle < 4 && upDownAngle > -4 && leftRightAngle < 4 && leftRightAngle > -4) {
                                overlay.onZeroAngle(true)
                                if (!isInAngleRange) {
                                    startTimer()
                                    isInAngleRange = true
                                } else {
                                    standardRatioList.add(eyeRatio)
                                }
                            } else {
                                overlay.onZeroAngle(false)
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
                            if (eyeState <= limit) {
                                if (!isInDrowsyState) {
                                    isInDrowsyState = true
                                    timeCheckDrowsy = Date().time
                                }

                                if (timeCheckDrowsy != null) {
                                    val maintainTime = Date().time - timeCheckDrowsy!!
                                    if (maintainTime > 1800) {
                                        Log.d("whatisthis", "졸음")
                                        binding.analyzeTextDrowsycheck.visibility = View.VISIBLE
                                    }
                                }
                            } else {
                                if (isInDrowsyState) {
                                    binding.analyzeTextDrowsycheck.visibility = View.INVISIBLE
                                    isInDrowsyState = false
                                    timeCheckDrowsy = null
                                }
                            }
//                            Log.d("whatisthis", "상태 : " + eyeState)
                        }
                    }
                })
        }
    }

    fun startTimer() {
        timer.start()
    }

    fun stopTimer() {
        timer.cancel()
        binding.analyzeProgress.visibility = View.INVISIBLE
    }

    fun calDist(point1: PointF3D, point2: PointF3D): Double {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return Math.sqrt((dx * dx + dy * dy).toDouble())
    }

    fun calRatio(upDownAngle: Float, leftRightAngle: Float, landmark: List<FaceMeshPoint>): Double {

        val upDownSec =
            (1 / Math.cos(upDownAngle * Math.PI / 180.0)) //  val upDownRadian = upDownAngle * Math.PI / 180.0
        var leftRightSec =
            (1 / Math.cos(leftRightAngle * Math.PI / 180.0)) // val leftRightRadian = leftRightAngle * Math.PI / 180.0

/*
var rightWidth =
calDist(landmark.get(159).position,
landmark.get(386).position) * leftRightSec// 오른쪽 가로 길

var rightHeight =
calDist(landmark.get(159).position,
landmark.get(145).position) * upDownSec // 오른쪽 세로 길이 -> 세로 길이는 항상 보정

var leftWidth =
calDist(landmark.get(145).position,
landmark.get(374).position) * leftRightSec // 왼쪽 가로 길이

var leftHeight =
calDist(landmark.get(386).position,
landmark.get(374).position) * upDownSec // 왼쪽 세로 길이 -> 세로 길이는 항상 보정

val widthAvg = (rightWidth + leftWidth) / 2.0
// 오른쪽 눈 왼쪽 눈 길이 정보를 평균한다.
val heightAvg = (rightHeight + leftHeight) / 2.0
*/

        val rightUpper = landmark.get(159).position
        val rightLower = landmark.get(145).position

        val leftUpper = landmark.get(386).position
        val leftLower = landmark.get(374).position

        val rightEyeMid =
            PointF3D.from((Math.round((rightUpper.x + rightLower.x) * 100) / 200.0).toFloat(),
                ((rightUpper.y + rightLower.y) / 2.0).toFloat(),
                0f)

        val leftEyeMid =
            PointF3D.from((Math.round((leftUpper.x + leftLower.x) * 100) / 200.0).toFloat(),
                ((leftUpper.y + leftLower.y) / 2.0).toFloat(),
                0f)

        val midEyeUpper =
            PointF3D.from((Math.round((leftUpper.x + rightUpper.x) * 100) / 200.0).toFloat(),
                ((leftUpper.y + rightUpper.y) / 2.0).toFloat(),
                0f)

        val midEyeLower =
            PointF3D.from((Math.round((rightLower.x + leftLower.x) * 100) / 200.0).toFloat(),
                ((rightLower.y + leftLower.y) / 2.0).toFloat(),
                0f)

        var widthAvg = calDist(rightEyeMid, leftEyeMid)
        var heightAvg = calDist(midEyeUpper, midEyeLower)

        if (upDownAngle < 0) {
            heightAvg *= upDownSec * 1.4
        } else {
            heightAvg *= upDownSec * 1.2
        }

        // 종횡비 계산
        return (heightAvg * upDownSec / widthAvg * leftRightSec)
    }


    override fun onDestroyView() {
        faceDetector.close()
        faceMesh.close()
        cameraExecutor.shutdown()

        super.onDestroyView()
    }
}