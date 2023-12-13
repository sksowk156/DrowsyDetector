package com.paradise.drowsydetector.utils.helper

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.drowsydetector.utils.LEFT_RIGHT_ANGLE_THREDHOLD
import com.paradise.drowsydetector.utils.UP_DOWN_ANGLE_THREDHOLD
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraHelper(
    private var contextRef: WeakReference<Context>,
    private var lifecycleOwner: WeakReference<LifecycleOwner>,
) {
    companion object {
        @Volatile
        private var instance: CameraHelper? = null
        fun getInstance(context: Context, lifecycleOwner: LifecycleOwner) =
            instance ?: synchronized(this) {
                // LocationSercie 객체를 생성할 때 같이 한번만 객체를 생성한다.
                instance ?: CameraHelper(
                    WeakReference(context), WeakReference(lifecycleOwner)
                ).also {
                    instance = it
                }
            }
    }

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

    fun stopCameraHelper() {
        cameraExecutor.shutdown()
    }

    fun releaseCameraHelper() {
        stopCameraHelper()
        faceDetector.close()
        faceMesh.close()
    }

    fun clearContext() {
        releaseCameraHelper()
        contextRef.clear()
        lifecycleOwner.clear()
        instance = null
    }

    fun startAnalyze(
        previewView: PreviewView,
        analyzedData: (Face, List<FaceMeshPoint>) -> Unit,
    ) {
        contextRef.get()?.let { context ->
            cameraExecutor = Executors.newSingleThreadExecutor()
            val cameraController = LifecycleCameraController(context)
            // 전면 카메라
            cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraController.bindToLifecycle(lifecycleOwner.get()!!)
            previewView.controller = cameraController.apply {
                setImageAnalysisAnalyzer(cameraExecutor, MlKitAnalyzer(
                    listOf(faceDetector, faceMesh),
                    CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    ContextCompat.getMainExecutor(context)
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
    }

    private var cameraProvider: ProcessCameraProvider? = null

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera2(
        previewView: PreviewView,
        analyzedData: (Face, List<FaceMeshPoint>) -> Unit,
    ) {
        contextRef.get()?.let { context ->
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

            lifecycleOwner.get()?.let { lifecycleOwner ->
                // 카메라 바인딩
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalysis
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }

    fun calRatio(upDownAngle: Float, leftRightAngle: Float, landmark: List<FaceMeshPoint>): Double {
        var upDownSec =
            (1 / Math.cos(Math.toRadians(upDownAngle.toDouble()))) //  val upDownRadian = upDownAngle * Math.PI / 180.0
        var leftRightSec =
            (1 / Math.cos(Math.toRadians(leftRightAngle.toDouble()))) // val leftRightRadian = leftRightAngle * Math.PI / 180.0

        /////////////////////////////////////////////////////
        val rightUpper = landmark.get(159).position
        val rightLower = landmark.get(145).position
        val leftUpper = landmark.get(386).position
        val leftLower = landmark.get(374).position

//        /////////////////////////////////////////////////////
//        var rightWidth = calDist(landmark.get(33).position, landmark.get(133).position) // 오른쪽 가로 길이
//        var rightHeight = calDist(
//            landmark.get(159).position, landmark.get(145).position
//        )  // 오른쪽 세로 길이 -> 세로 길이는 항상 보정
//        var leftWidth = calDist(landmark.get(263).position, landmark.get(362).position) // 왼쪽 가로 길이
//        var leftHeight = calDist(
//            landmark.get(386).position, landmark.get(374).position
//        )  // 왼쪽 세로 길이 -> 세로 길이는 항상 보정
//        /////////////////////////////////////////////
//
//
//        var basewidthLower0 = ((rightWidth + leftWidth) / 2.0)
//        var baseheightAvg0 = (rightHeight + leftHeight) / 2.0
//
//        ////////////////////////////////////////////////
//
//        var basewidthLower1 = ((rightWidth + leftWidth) / 2.0) * leftRightSec
//        var baseheightAvg1 = (rightHeight + leftHeight) / 2.0
//
//
//        ///////////////////////////////////////////////////////////
//        var widthLower0 = (calDist(rightLower, leftLower))
//        var heightAvg0 = (calDist(rightUpper, rightLower) + calDist(leftUpper, leftLower)) / 2.0
//
//        /////////////////////////////////////////////////////////
        var widthLower1 = (calDist(rightLower, leftLower)) * leftRightSec
        var heightAvg1 = (calDist(rightUpper, rightLower) + calDist(leftUpper, leftLower)) / 2.0
//
//        ///////////////////////////////////////////////////////////////
//
        if (leftRightAngle < -30 || leftRightAngle > 30) {
            widthLower1 *= 0.98
//            basewidthLower1 *= 0.98
            upDownSec *= 1.02
        }
        if (upDownAngle < 0) { // 카메라가 위에 있을 경우
            heightAvg1 *= (upDownSec * 1.05) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
//            baseheightAvg1 *= (upDownSec * 1.05)
        } else { // 카메라가 아래에 있을 경우
            heightAvg1 *= (upDownSec * 0.95) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
//            baseheightAvg1 *= (upDownSec * 0.95)
        }
//
//
//
//        Log.d(
//            "whatisthis0",
//            "${(heightAvg1 / widthLower1)} " +
//                    "${heightAvg0 / widthLower0} " +
//                    "${baseheightAvg1 / basewidthLower1} " +
//                    "${baseheightAvg0 / basewidthLower0}"
//        )

        // 종횡비 계산
        return (heightAvg1 / widthLower1)
    }

    fun calDist(point1: PointF3D, point2: PointF3D): Double {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return Math.sqrt((dx * dx + dy * dy).toDouble())
    }

    fun checkHeadAngleInNoStandard(upDownAngle: Float, leftRightAngle: Float) =
        upDownAngle < 4 && upDownAngle > -4 && leftRightAngle < 4 && leftRightAngle > -4

    fun isInLeftRight(leftRightAngle: Float) = leftRightAngle < 4 && leftRightAngle > -4

    fun checkHeadAngleInStandard(leftRightAngle: Float, upDownAngle: Float) =
        leftRightAngle < -LEFT_RIGHT_ANGLE_THREDHOLD || leftRightAngle > LEFT_RIGHT_ANGLE_THREDHOLD || upDownAngle < -UP_DOWN_ANGLE_THREDHOLD || upDownAngle > UP_DOWN_ANGLE_THREDHOLD

}