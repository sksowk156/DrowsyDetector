package com.paradise.analyze

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
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
import androidx.lifecycle.lifecycleScope
import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.musicItem
import com.core.model.winkResultItem
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.common.helper.MusicServiceHelper
import com.paradise.common.network.BASICMUSICMODE
import com.paradise.common.network.DROWSY_THREDHOLD
import com.paradise.common.network.GUIDEMODE
import com.paradise.common.network.MUSICVOLUME
import com.paradise.common.network.NOTIFICATION_CHANNEL_ID
import com.paradise.common.network.NOTIFICATION_CHANNEL_NAME
import com.paradise.common.network.NOTIFICATION_ID
import com.paradise.common.network.REFRESHTERM
import com.paradise.common.network.SMILE_THREDHOLD
import com.paradise.common.network.TIME_THREDHOLD
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.getTodayDate
import com.paradise.common.network.mainDispatcher
import com.paradise.data.repository.AnalyzerResultRepository
import com.paradise.data.repository.MusicRepository
import com.paradise.data.repository.SettingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class AnalyzesService : LifecycleService() {
    @Inject
    lateinit var musicRepository: MusicRepository

    @Inject
    lateinit var settingRepository: SettingRepository

    @Inject
    lateinit var analyzeResultRepository: AnalyzerResultRepository

    @Inject
    lateinit var musicHelper: MusicServiceHelper

    private val binder = MyBinder(this@AnalyzesService)

    class MyBinder(service: AnalyzesService) : Binder() {
        private val weakService = WeakReference(service)
        fun getService(): AnalyzesService {
            return weakService.get()!!
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private var mWindowManager: WindowManager? = null
    private var mParams: WindowManager.LayoutParams? = null
    private var mFloatingView: View? = null

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
    private var cameraProvider: ProcessCameraProvider? = null

    var standard: Double? = null
    fun initStandard() {
        standard = null
    }

    private var isInDrowsyState = false
    private var timeCheckDrowsy: Long? = null
    var eyeClosed: Boolean = false

    private var datajob: Job? = null // 눈 깜빡임 감지
    private var musicList = mutableListOf<musicItem>()
    private var checkDrowsy: Boolean = true

    @SuppressLint("RestrictedApi")
    override fun onDestroy() {
        killForegroundService()
        mWindowManager = null
        mParams = null
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()

        initWindowManagerParams()
        subscribeRecord()
        subscribeUserMusicList()
        getRecord(getTodayDate())
        getAllMusic() // 사용자 설정 음악을 사용할 경우, 모든 음악 정보를 들고 온다.
    }

    fun initWindowManagerParams() {
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mParams = WindowManager.LayoutParams(
            300, // 너비
            500,// 높이
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    fun initFloatingView() {
        // floating view
        mFloatingView =
            LayoutInflater.from(this@AnalyzesService).inflate(R.layout.floating_view, null)
        // 종료 버튼
        val closeButtonCollapsed = mFloatingView!!.findViewById<ImageView>(R.id.iv_floating_close)
        closeButtonCollapsed.setOnClickListener {
            killForegroundService()
            stopSelf() // service를 중지시킨다. onDestry()를 호출한다.
        }
        // 플로팅 뷰 전체
        val comebackFloatingView = mFloatingView!!.findViewById<View>(R.id.layout_floating)
        comebackFloatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()

            @SuppressLint("RestrictedApi")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = mParams!!.x
                        initialY = mParams!!.y
                        initialTouchX = event.rawX  // 터치 다운 시의 좌표 저장
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        val upX = event.rawX
                        val upY = event.rawY
                        if (isAClick(initialTouchX, upX, initialTouchY, upY)) { // 클릭 동작이 감지됨
                            stopForegroundInBackground()
                            restartActivity()
                            return true
                        }
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        mParams?.x = initialX + (event.rawX - initialTouchX).toInt()
                        mParams?.y = initialY + (event.rawY - initialTouchY).toInt()
                        mWindowManager!!.updateViewLayout(mFloatingView, mParams)
                        return true
                    }
                }
                return false
            }
        })
        subscribeAllSetting(mFloatingView!!.findViewById<PreviewView>(R.id.preview_floating))
    }

    fun startForegroundInBackground() {
        addFloatingView()
        getAllSetting()
    }

    private fun addFloatingView() {
        if (mFloatingView == null) {
            initFloatingView()
        }
        mWindowManager!!.addView(mFloatingView, mParams)
    }

    fun removeFloatingView() {
        if (mFloatingView != null) {
            mWindowManager!!.removeViewImmediate(mFloatingView)
        }
        _allSettings.removeObservers(this@AnalyzesService)
        mFloatingView = null
    }

    @SuppressLint("RestrictedApi")
    fun stopForegroundInBackground() {
        cameraProvider?.unbindAll() // 모든 카메라를 해제한다. 이 메소드는 카메라를 재사용할 수 있도록 하며, 카메라의 리소스를 해제하지 않는다.
        cameraProvider?.shutdown() // 모든 카메라를 해제하고, 카메라의 리소스를 해제한다다.
        cameraProvider = null
        musicHelper.releaseMediaPlayer()
        removeFloatingView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // 서비스가 시작될 때 수행할 작업을 여기에 작성합니다.
        intent?.let {
            startForeground()
        }
        return START_NOT_STICKY // 서비스가 종료되었을 때, 서비스 재 실행을 하지 않음
    }

    private fun restartActivity() { // Service를 호출한 Activity(Fragment) 재실행
        val intent = this@AnalyzesService.packageManager.getLaunchIntentForPackage(
            this@AnalyzesService.packageName
        )
        intent?.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        this@AnalyzesService.startActivity(intent) // 새로운 Activity를 실행하는 것이 아니라 기존에 있던(Main Activity) 것을 실행한다.
    }

    @SuppressLint("RestrictedApi")
    fun killForegroundService() {
        faceMesh.close()
        faceDetector.close()
        stopForegroundInBackground()
        mFloatingView = null
        datajob?.cancel()
        datajob = null
        initStandard()
        stopForeground(STOP_FOREGROUND_REMOVE) // service를 foreground에서 제거한다. 상태 표시줄의 알림도 함께 제거한다. 이 메소드를 호출하더라도 서비스는 계속 실행된다.
    }

    // 클릭 여부를 판단하는 함수
    private fun isAClick(downX: Float, upX: Float, downY: Float, upY: Float): Boolean {
        val deltaX = upX - downX
        val deltaY = upY - downY
        val distanceSquared = deltaX * deltaX + deltaY * deltaY
        return distanceSquared < (ViewConfiguration.get(this).scaledTouchSlop * ViewConfiguration.get(
            this
        ).scaledTouchSlop)
    }

    fun startRecording() {
        if (datajob == null) {
            datajob = initJob()
            datajob!!.start()
        }
    }

    fun subscribeRecord() {
        _analyzeRecord.observe(this@AnalyzesService) {
            if (it == null) {
                currentAnayzeResult = analyzeResultItem(getTodayDate(), 1)
            } else {
                currentAnayzeResult = it
            }
        }
    }

    private fun subscribeAllSetting(previewView: PreviewView) {
        _allSettings.observe(this@AnalyzesService) {
            if (it.first.size > 0 && it.second.size > 0) {
                val defaultMusic = it.first[1]
                val onRefresh = it.second[1]

                startCamera(
                    startMusic = { startMusic(defaultMusic) },
                    startRefresh = { startRefresh(onRefresh) },
                    floatingPreviewView = previewView
                )
            }
        }
    }

    private fun subscribeUserMusicList() {
        _music.observe(this@AnalyzesService) { musicList ->
            if (musicList.isNotEmpty()) this.musicList.addAll(musicList)
        }
    }

    private fun startMusic(defaultMode: Boolean) {
        musicHelper.releaseMediaPlayer()

        if (defaultMode) {
            musicHelper.setResMusic()
        } else {
            if (musicList.size > 0) musicHelper.setMyMusic(musicList)
        }
    }

    private fun initJob() = lifecycleScope.launch(defaultDispatcher, CoroutineStart.LAZY) {
        while (this.isActive) {
            delay(1 * 60 * 1000)  // 30분 대기
            insertWinkCount(
                winkResultItem(
                    recordId = currentAnayzeResult!!.id, value = currentWinkCount
                )
            )
            insertDrowsyCount(
                drowsyResultItem(
                    recordId = currentAnayzeResult!!.id, value = currentDrowsyCount
                )
            )
            // 초기화
            initWinkCount()
            initDrowsyCount()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun startAnalyze(
        previewView: PreviewView,
        analyzedData: (Face, List<FaceMeshPoint>) -> Unit,
    ) {
        // 카메라 및 ML Kit 설정
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@AnalyzesService)
        // 카메라 미리보기 설정
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // 이미지 분석 설정
        val imageAnalysis =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        // 이미지 분석기 설정
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this@AnalyzesService)) { imageProxy ->
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
                    }.addOnFailureListener {}.addOnCompleteListener {}
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

            cameraProvider?.let {
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                it.unbindAll()
                it.bindToLifecycle(
                    this@AnalyzesService, cameraSelector, preview, imageAnalysis
                )
            }
        }, ContextCompat.getMainExecutor(this@AnalyzesService))
    }

    fun startCamera(
        startMusic: () -> Unit,
        startRefresh: () -> Unit,
        floatingPreviewView: PreviewView,
    ) {
        startAnalyze(floatingPreviewView) { resultDetector, resultMesh ->
            val upDownAngle = resultDetector.headEulerAngleX
            val leftRightAngle = resultDetector.headEulerAngleY
//            startRecording()
            val eyeRatio = calRatio(upDownAngle, leftRightAngle, resultMesh)
            val eyeState = eyeRatio / standard!! // 눈 상태
            // 비율이 제한을 벗어났을 때
            if (eyeState <= DROWSY_THREDHOLD) {
                // 눈 깜빡임 카운팅
                checkEyeWink(eyeState, resultDetector)
                // 웃지 않을 때 (웃을 때 눈 웃음 때문에 눈 작아짐)
                if (resultDetector.smilingProbability!! <= SMILE_THREDHOLD) {
                    if (!isInDrowsyState) {
                        isInDrowsyState = true
                        timeCheckDrowsy = Date().time
                    }

                    if (timeCheckDrowsy != null) {
                        val maintainTime = Date().time - timeCheckDrowsy!!

                        // 졸음 감지!!!!!!!!!!!!!!!!!!!!!!!!!!
                        if (maintainTime > TIME_THREDHOLD) {
                            if (checkDrowsy) { // 한번만 동작하게
                                checkDrowsy = false
                                currentDrowsyCount++
                                // 경고음
                                startMusic()
                                // 환기 요청
                                startRefresh()
                            }
                        }
                    }
                }
            } else {
                setEyeStateInOpen()
            }
        }
    }

    private fun setEyeStateInOpen() {
        eyeClosed = false
        if (isInDrowsyState) {
            checkDrowsy = true
            isInDrowsyState = false
            timeCheckDrowsy = null
        }
    }

    fun checkEyeWink(eyeState: Double, resultDetector: Face) {
        // 눈 깜빡임 카운팅
        if (eyeState <= 0.4) {
            val leftEyeOpen = resultDetector.leftEyeOpenProbability
            val righEyeOpen = resultDetector.rightEyeOpenProbability

            if (datajob != null && datajob!!.isActive) {
                if (leftEyeOpen != null && righEyeOpen != null) {
                    if (leftEyeOpen < 0.3 && righEyeOpen < 0.3 && !eyeClosed) {
                        eyeClosed = true // frame이 높아서 한번에 많이 처리될 수 있기 때문
                        // 눈이 감겨 있음
                        countUpWinkCount()
                    }
                }
            }
        }
    }

    // 환기 요청
    private fun startRefresh(a: Int) {

    }


    // musicRepositoryImpl ///////////////////////////////////////////////////////////////////////////////////////
    private val _music = MutableLiveData<List<musicItem>>(emptyList())

    /* R : 이벤트 전체 조회 메서드 */
    fun getAllMusic() {
        lifecycleScope.launch {
            musicRepository.getAllMusic().collect {
                _music.value = it
            }
        }
    }

    fun calRatio(upDownAngle: Float, leftRightAngle: Float, landmark: List<FaceMeshPoint>): Double {
        var upDownSec =
            (1 / Math.cos(Math.toRadians(upDownAngle.toDouble()))) //  val upDownRadian = upDownAngle * Math.PI / 180.0
        var leftRightSec =
            (1 / Math.cos(Math.toRadians(leftRightAngle.toDouble()))) // val leftRightRadian = leftRightAngle * Math.PI / 180.0

        val rightUpper = landmark.get(159).position
        val rightLower = landmark.get(145).position

        val leftUpper = landmark.get(386).position
        val leftLower = landmark.get(374).position

        var widthLower = (calDist(rightLower, leftLower)) * leftRightSec
        var heightAvg = (calDist(rightUpper, rightLower) + calDist(leftUpper, leftLower)) / 2.0

        if (leftRightAngle < -30 || leftRightAngle > 30) {
            widthLower *= 0.98
            upDownSec *= 1.02
        }
        if (upDownAngle < 0) { // 카메라가 위에 있을 경우
            heightAvg *= (upDownSec * 1.05) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
        } else { // 카메라가 아래에 있을 경우
            heightAvg *= (upDownSec * 0.95) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
        }

        // 종횡비 계산
        return (heightAvg / widthLower)
    }

    fun calDist(point1: PointF3D, point2: PointF3D): Double {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return Math.sqrt((dx * dx + dy * dy).toDouble())
    }


    // settingRepositoryImpl ///////////////////////////////////////////////////////////////////////////////////////
    private val _guideMode = MutableLiveData<Boolean>(true)

    private val _basicMusicMode = MutableLiveData<Boolean>(true)

    private val _musicVolume = MutableLiveData<Int>(0)

    private val _refreshTerm = MutableLiveData<Int>(0)
    fun getSettingModeInt(key: String) {
        lifecycleScope.launch {
            settingRepository.getInt(key)?.collect {
                when (key) {
                    MUSICVOLUME -> {
                        _musicVolume.value = it
                    }

                    REFRESHTERM -> {
                        _refreshTerm.value = it
                    }
                }
            }
        }
    }

    fun setSettingMode(key: String, value: Int) {
        lifecycleScope.launch {
            settingRepository.setInt(key, value)
        }
    }

    fun getSettingModeBool(key: String) {
        lifecycleScope.launch {
            settingRepository.getBoolean(key)?.collect {
                when (key) {
                    GUIDEMODE -> {
                        _guideMode.value = it
                    }

                    BASICMUSICMODE -> {
                        _basicMusicMode.value = it
                    }
                }
            }
        }
    }

    fun setSettingMode(key: String, value: Boolean) {
        lifecycleScope.launch {
            settingRepository.setBoolean(key, value)
        }
    }

    private val _allSettings = MutableLiveData(mutableListOf<Boolean>() to mutableListOf<Int>())
    fun getAllSetting() {
        lifecycleScope.launch {
            with(settingRepository) {
                if (this != null) {
                    this.getBoolean(GUIDEMODE)
                        .zip(this.getBoolean(BASICMUSICMODE)) { a, b -> mutableListOf(a, b) }
                        .zip(this.getInt(MUSICVOLUME)) { list, c -> list to mutableListOf(c) }
                        .zip(this.getInt(REFRESHTERM)) { pair, d ->
                            pair.second.add(d)
                            pair.first to pair.second
                        }.collect {
                            _allSettings.value = it
                        }
                }
            }
        }
    }

    // staticsRepositoryImpl ///////////////////////////////////////////////////////////////////////////////////////

    private val _allAnalyzeRecord = MutableLiveData<List<analyzeResultItem>>(emptyList())
    fun insertRecord(analyzeResult: analyzeResultItem) {
        lifecycleScope.launch() {
            analyzeResultRepository.insertRecord(analyzeResult)
        }
    }

    private val _analyzeRecord = MutableLiveData<analyzeResultItem>()

    fun getRecord(time: String) {
        lifecycleScope.launch(mainDispatcher) {
            analyzeResultRepository.getRecord(time)?.collect {
                _analyzeRecord.value = (it)
            }
        }
    }

    fun getRecord(id: Int) {
        lifecycleScope.launch(mainDispatcher) {
            analyzeResultRepository?.getRecord(id)?.collect {
                _analyzeRecord.value = (it)
            }
        }
    }

    var currentAnayzeResult: analyzeResultItem? = null

    private val _allAnayzeResult =
        MutableLiveData<Pair<List<winkResultItem>, List<drowsyResultItem>>>(
            emptyList<winkResultItem>() to emptyList<drowsyResultItem>()
        )

    var currentWinkCount = 0 //
    fun initWinkCount() {
        currentWinkCount = 0
    }

    fun countUpWinkCount() {
        currentWinkCount++
    }

    fun insertWinkCount(winkCount: winkResultItem) {
        lifecycleScope.launch() {
            analyzeResultRepository?.insertWinkCount(winkCount)
        }
    }

    private val _winkCount = MutableLiveData<List<winkResultItem>>(emptyList())

    var currentDrowsyCount = 0
    fun initDrowsyCount() {
        currentDrowsyCount = 0
    }

    fun countUpDrowsyCount() {
        currentDrowsyCount++
    }

    private val _drowsyCount = MutableLiveData<List<drowsyResultItem>>(emptyList())
    fun insertDrowsyCount(drowsyCount: drowsyResultItem) {
        lifecycleScope.launch() {
            analyzeResultRepository.insertDrowsyCount(drowsyCount)
        }
    }

    // 알림 관리 ***********************************************************************************************
    private fun startForeground() {
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
            .setSmallIcon(com.paradise.common_ui.R.drawable.icon_x).setContentTitle("WatchOut")
            .setContentText("졸음 감지 중").setContentIntent(getMainActivityPendingIntent())

    private fun getMainActivityPendingIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        199,
        Intent("com.example.app.ACTION").addCategory("android.intent.category.DEFAULT"),
        PendingIntent.FLAG_IMMUTABLE
    )
}
