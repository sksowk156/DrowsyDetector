package com.paradise.drowsydetector.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.utils.ACTION_SHOW_TRACKING_FRAGMENT
import com.paradise.drowsydetector.utils.ACTION_START_OR_RESUME_SERVICE
import com.paradise.drowsydetector.utils.ACTION_STOP_SERVICE
import com.paradise.drowsydetector.utils.NOTIFICATION_CHANNEL_ID
import com.paradise.drowsydetector.utils.NOTIFICATION_CHANNEL_NAME
import com.paradise.drowsydetector.utils.NOTIFICATION_ID
import com.paradise.drowsydetector.view.MainActivity

class AnalyzeeService : LifecycleService() {
    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        var serviceKilled = MutableLiveData<Boolean>() // 서비스 종료 유무
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        serviceKilled.postValue(true)
    }

    fun killService() {
        postInitialValues()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mFloatingView, params)

        val closeButtonCollapsed = mFloatingView!!.findViewById<ImageView>(R.id.iv_floating_close)
        closeButtonCollapsed.setOnClickListener { stopSelf() }

        mFloatingView!!.findViewById<View>(R.id.layout_floating)
            .setOnTouchListener(object : View.OnTouchListener {
                private var lastAction: Int = 0
                private var initialX: Int = 0
                private var initialY: Int = 0
                private var initialTouchX: Float = 0.toFloat()
                private var initialTouchY: Float = 0.toFloat()

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            lastAction = event.action
                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            if (lastAction == MotionEvent.ACTION_DOWN) {
                                val intent = Intent(this@AnalyzeeService, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                stopSelf()
                            }
                            lastAction = event.action
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            mWindowManager!!.updateViewLayout(mFloatingView, params)
                            lastAction = event.action
                            return true
                        }
                    }
                    return false
                }
            })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 서비스가 시작될 때 수행할 작업을 여기에 작성합니다.
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> { // 달리기 시작 or 계속
                    startForegroundService()
                    val preview: PreviewView = mFloatingView!!.findViewById(R.id.preview_floating)
                    startCamera(preview.surfaceProvider)
                }

                ACTION_STOP_SERVICE -> { // 달리기 종료
                    killService()
                }

                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val faceDetectorOption by lazy {
        FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
    }

    private val faceDetector by lazy {
        FaceDetection.getClient(faceDetectorOption)
    }

    private var cameraProvider: ProcessCameraProvider? = null

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera(surfaceProvider: Preview.SurfaceProvider) {
        // 카메라 및 ML Kit 설정
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // 카메라 미리보기 설정
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(surfaceProvider)
        }

        // 이미지 분석 설정
        val imageAnalysis =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        // 이미지 분석기 설정
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                faceDetector.process(image).addOnSuccessListener { faces ->
                        // 얼굴이 감지되면 FaceMesh를 적용
                        if (faces == null || faces.size == 0) return@addOnSuccessListener
                        val face = faces[0]
                        val upDownAngle = face.headEulerAngleX
                        val leftRightAngle = face.headEulerAngleY
                        val landmark = face.allLandmarks
                        Log.d(
                            "whatisthis",
                            upDownAngle.toString() + " " + leftRightAngle.toString() + " " + landmark.size
                        )

                    }.addOnFailureListener { e ->
                        // 에러 처리
                    }.addOnCompleteListener {
                        // 이미지 프록시를 닫아야 다음 이미지를 분석할 수 있습니다.
                        imageProxy.close()
                    }
            }
        }

        // 카메라 바인딩
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }

    // 알림 관리 ***********************************************************************************************
    private fun startForegroundService() {
        isTracking.postValue(true)
        serviceKilled.value = false

        val channel = createNotificationChannel()
        val notificationManager: NotificationManager = createNotificationManager()
        val notificationBuilder = createNotificationBuilder()
        notificationManager.createNotificationChannel(channel)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
//        // 알림창 시간 변경
//        timeRunInSeconds.observe(this, Observer { time ->
//            if (serviceKilled.value == false) { // 서비스가 종료 되지 않았을 때
//                notificationBuilder.setContentText(TrackingUtility.getFormattedStopWatchTime(time * 1000L))
//                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
//            }
//        })
    }

    private fun createNotificationChannel() = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_LOW // 알림음이 없습니다.
    ).apply { description = "졸음 감지" }


    private fun createNotificationManager() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    private fun createNotificationBuilder() =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setAutoCancel(false)
            .setOngoing(true) // 스와이프로 지울 수 없다.
            .setSmallIcon(R.drawable.icon_x).setContentTitle("WatchOut").setContentText("졸음 감지 중")
            .setContentIntent(getMainActivityPendingIntent())

    private fun getMainActivityPendingIntent(): PendingIntent =
        PendingIntent.getActivity(this, 199, Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        }, PendingIntent.FLAG_IMMUTABLE)

}
