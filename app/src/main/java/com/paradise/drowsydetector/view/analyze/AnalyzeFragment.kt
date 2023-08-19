package com.paradise.drowsydetector.view.analyze

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseFragment
import com.paradise.drowsydetector.databinding.FragmentAnalyzeBinding
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

    override fun init() {
        initAppbar(binding.analyzeToolbar, null, true, "")

        cameraExecutor = Executors.newSingleThreadExecutor()

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

                    val eyeRatio = calRatio(angle, landmark)
                    Log.d("whatisthis", eyeRatio.toString())
                })
        }
    }

    fun calDist(point1: PointF3D, point2: PointF3D): Float {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun calRatio(angle : Face, landmark : List<FaceMeshPoint>) : Double{
        val upDownAngle = angle.headEulerAngleX + 7 // 정면을 봤을 때 -7부터 시작해서
        val leftRightAngle = angle.headEulerAngleY

        val upDownSec =
            (1 / Math.cos(upDownAngle * Math.PI / 180.0)) //  val upDownRadian = upDownAngle * Math.PI / 180.0
        val leftRightSec =
            (1 / Math.cos(leftRightAngle * Math.PI / 180.0)) // val leftRightRadian = leftRightAngle * Math.PI / 180.0

        var rightWidth =
            calDist(landmark.get(33).position, landmark.get(133).position) // 오른쪽 가로 길이

        var rightHeight =
            calDist(landmark.get(159).position,
                landmark.get(145).position) * upDownSec // 오른쪽 세로 길이 -> 세로 길이는 항상 보정

        var leftWidth =
            calDist(landmark.get(263).position, landmark.get(362).position) // 왼쪽 가로 길이

        var leftHeight =
            calDist(landmark.get(386).position,
                landmark.get(374).position) * upDownSec // 왼쪽 세로 길이 -> 세로 길이는 항상 보정

        if (upDownAngle < 20 && upDownAngle > -10) { // 20(위) > 고개의 범위 > -10(아래) 일 때
            // 가로 길이 보정을 허용한다.
            leftWidth = (leftWidth * leftRightSec).toFloat()
            rightWidth = (rightWidth * leftRightSec).toFloat()
        }


        if (leftRightAngle >= 18) { // 왼쪽으로 18도 이상 넘어갈 경우 -> 오른쪽눈으로만 종횡비 계산
            // 왼쪽 눈 정보를 0으로 바꿈
            leftHeight = 0.0
            leftWidth = 0F
            // 오른쪽 눈 가로 길이에 들어간 보정을 줄인다.(눈이 둥근 형태이기 때문에, 길이가 더 길게 측정되어서)
            rightWidth = (rightWidth / ((leftRightSec + 1) / 2.0)).toFloat()
        } else if (leftRightAngle <= -18) { // 오른쪽으로 18도 이상 넘어갈 경우 -> 왼쪽눈으로만 종횡비 계산
            rightHeight = 0.0
            rightWidth = 0f
            leftWidth = (leftWidth / ((leftRightSec + 1) / 2.0)).toFloat()
        }

        // 오른쪽 눈 왼쪽 눈 길이 정보를 평균한다.
        val heightAvg = (rightHeight + leftHeight) / 2.0
        val widthAvg = (rightWidth + leftWidth) / 2.0

        // 종횡비 계산
        return (heightAvg / widthAvg)
    }


    override fun onDestroyView() {
        cameraExecutor.shutdown()
        faceDetector.close()
        faceMesh.close()
        super.onDestroyView()
    }
}