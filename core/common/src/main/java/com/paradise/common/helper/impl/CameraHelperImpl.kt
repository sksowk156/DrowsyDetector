package com.paradise.common.helper.impl

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.common.helper.CameraHelper
import com.paradise.common.network.LEFT_RIGHT_ANGLE_THREDHOLD
import com.paradise.common.network.UP_DOWN_ANGLE_THREDHOLD
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class CameraHelperImpl @Inject constructor(
    private val fragment: Fragment,
) : CameraHelper {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var contextRef: Context
    private lateinit var lifecycleOwner: LifecycleOwner
    override fun initCameraHelper() {
        contextRef = fragment.requireContext()
        lifecycleOwner = fragment.viewLifecycleOwner
    }

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

    override fun stopCameraHelper() {
        cameraExecutor.shutdown()
    }

    override fun releaseCameraHelper() {
        stopCameraHelper()
        faceDetector.close()
        faceMesh.close()
    }


    override fun startAnalyze(
        previewView: PreviewView,
        analyzedData: (Face, List<FaceMeshPoint>) -> Unit,
    ) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraController = LifecycleCameraController(contextRef!!)
        // 전면 카메라
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController.bindToLifecycle(lifecycleOwner!!)
        previewView.controller = cameraController.apply {
            setImageAnalysisAnalyzer(cameraExecutor, MlKitAnalyzer(
                listOf(faceDetector, faceMesh),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(contextRef!!)
            ) { result: MlKitAnalyzer.Result? ->
                // 화면 회전 시 쓰레드가 종료되는 도중에 binding이 null이 되기 때문에 binding이 먼저 null 되면 쓰레드 내부가 동작하지 못하게 막는다.(nullpointexception 방지)
                if (result != null) {
                    // 분석 결과(랜드마크)
                    val analyzeResult1 = result.getValue(faceDetector)
                    val analyzeResult2 = result.getValue(faceMesh)
                    // 아무것도 인식하지 못할 땐 return
                    if (analyzeResult1 == null || analyzeResult2 == null || analyzeResult1.size == 0 || analyzeResult2.size == 0) {
                        previewView.setOnTouchListener { _, _ -> false } //no-op
                        return@MlKitAnalyzer
                    }
                    val resultDetector = analyzeResult1[0]
                    val resultMesh = analyzeResult2[0].allPoints
                    analyzedData(resultDetector, resultMesh)
                }
            })
        }

    }

    private var cameraProvider: ProcessCameraProvider? = null

    @SuppressLint("UnsafeOptInUsageError")
    override fun startCamera2(
        previewView: PreviewView,
        analyzedData: (Face, List<FaceMeshPoint>) -> Unit,
    ) {
        contextRef?.let { context ->
            // 카메라 및 ML Kit 설정
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            // 카메라 미리보기 설정
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // 이미지 분석 설정
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

            // 이미지 분석기 설정
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    faceDetector.process(image).addOnSuccessListener { analyzeResult1 ->
                        // 얼굴이 감지되면 FaceMesh를 적용
                        if (analyzeResult1 == null || analyzeResult1.size == 0) return@addOnSuccessListener
                        faceMesh.process(image).addOnSuccessListener { analyzeResult2 ->
                            if (analyzeResult2 == null || analyzeResult2.size == 0) return@addOnSuccessListener
                            val resultDetector = analyzeResult1[0]
                            val resultMesh = analyzeResult2[0].allPoints
                            analyzedData(resultDetector, resultMesh)
                        }.addOnFailureListener {

                        }.addOnCompleteListener {
                            imageProxy.close()
                        }
                    }.addOnFailureListener { e ->
                        // 에러 처리
                    }.addOnCompleteListener {
                        // 이미지 프록시를 닫아야 다음 이미지를 분석할 수 있습니다.
                        imageProxy.close()
                    }
                }
            }

            lifecycleOwner.let { lifecycleOwner ->
                // 카메라 바인딩
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner!!, cameraSelector, preview, imageAnalysis
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }

    override fun calRatio(
        upDownAngle: Float,
        leftRightAngle: Float,
        landmark: List<FaceMeshPoint>,
    ): Double {
        val upDownSec =
            (1 / Math.cos(Math.toRadians(upDownAngle.toDouble()))) //  val upDownRadian = upDownAngle * Math.PI / 180.0
        var leftRightSec =
            (1 / Math.cos(Math.toRadians(leftRightAngle.toDouble()))) // val leftRightRadian = leftRightAngle * Math.PI / 180.0

        val rightUpper = landmark.get(159).position
        val rightLower = landmark.get(145).position

        val leftUpper = landmark.get(386).position
        val leftLower = landmark.get(374).position

        var widthLower = (calDist(rightLower, leftLower)) * leftRightSec
        var heightAvg = (calDist(rightUpper, rightLower) + calDist(leftUpper, leftLower)) / 2.0

        if (upDownAngle < 0) { // 카메라가 위에 있을 경우
            heightAvg *= (upDownSec * 1.1) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
        } else { // 카메라가 아래에 있을 경우
            heightAvg *= (upDownSec * 0.9) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
        }

        // 종횡비 계산
        return (heightAvg / widthLower)
    }

    override fun calDist(point1: PointF3D, point2: PointF3D): Double {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return Math.sqrt((dx * dx + dy * dy).toDouble())
    }

    override fun checkHeadAngleInNoStandard(upDownAngle: Float, leftRightAngle: Float) =
        upDownAngle < 4 && upDownAngle > -4 && leftRightAngle < 4 && leftRightAngle > -4

    override fun isInLeftRight(leftRightAngle: Float) = leftRightAngle < 4 && leftRightAngle > -4

    override fun checkHeadAngleInStandard(leftRightAngle: Float, upDownAngle: Float) =
        leftRightAngle < -LEFT_RIGHT_ANGLE_THREDHOLD || leftRightAngle > LEFT_RIGHT_ANGLE_THREDHOLD || upDownAngle < -UP_DOWN_ANGLE_THREDHOLD || upDownAngle > UP_DOWN_ANGLE_THREDHOLD


}