package com.paradise.drowsydetector.service

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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.data.local.room.record.AnalyzeResult
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import com.paradise.drowsydetector.repository.MusicRepository
import com.paradise.drowsydetector.repository.SettingRepository
import com.paradise.drowsydetector.repository.StaticsRepository
import com.paradise.drowsydetector.utils.ACTION_SHOW_TRACKING_FRAGMENT
import com.paradise.drowsydetector.utils.ApplicationClass
import com.paradise.drowsydetector.utils.BASICMUSICMODE
import com.paradise.drowsydetector.utils.DROWSY_THREDHOLD
import com.paradise.drowsydetector.utils.GUIDEMODE
import com.paradise.drowsydetector.utils.MUSICVOLUME
import com.paradise.drowsydetector.utils.MusicHelper
import com.paradise.drowsydetector.utils.NOTIFICATION_CHANNEL_ID
import com.paradise.drowsydetector.utils.NOTIFICATION_CHANNEL_NAME
import com.paradise.drowsydetector.utils.NOTIFICATION_ID
import com.paradise.drowsydetector.utils.REFRESHTERM
import com.paradise.drowsydetector.utils.SMILE_THREDHOLD
import com.paradise.drowsydetector.utils.TIME_THREDHOLD
import com.paradise.drowsydetector.utils.calRatio
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.getTodayDate
import com.paradise.drowsydetector.utils.mainDispatcher
import com.paradise.drowsydetector.view.MainActivity
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

class AnalyzeService : LifecycleService() {

    inner class MyBinder : Binder() {
        fun getService(): AnalyzeService {
            return this@AnalyzeService
        }
    }

    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null

    private var musicHelper: MusicHelper? = null

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
    private var musicRepository: MusicRepository? = null
    private var settingRepository: SettingRepository? = null
    private var staticsRepository: StaticsRepository? = null

    var standard: Double? = null
    private var isInDrowsyState = false
    private var timeCheckDrowsy: Long? = null
    private var eyeClosed: Boolean = false

    var datajob: Job? = null // 눈 깜빡임 감지
    private var musicList = mutableListOf<Music>()
    var checkDrowsy: Boolean = true
        set(value) {
            field = value
        }
        get() = field

    private val binder = MyBinder()
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        killForegroundService()
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        musicRepository = ApplicationClass.getApplicationContext().musicRepository
        settingRepository = ApplicationClass.getApplicationContext().settingRepository
        staticsRepository = ApplicationClass.getApplicationContext().staticRepository

        musicHelper = MusicHelper.getInstance(this@AnalyzeService, this@AnalyzeService)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // 서비스가 시작될 때 수행할 작업을 여기에 작성합니다.
        intent?.let {
            startForegroundService2()
            // floating view
            mFloatingView =
                LayoutInflater.from(this@AnalyzeService).inflate(R.layout.floating_view, null)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            mWindowManager!!.addView(mFloatingView, params)

            // 종료 버튼
            val closeButtonCollapsed =
                mFloatingView!!.findViewById<ImageView>(R.id.iv_floating_close)

            closeButtonCollapsed.setOnClickListener {
                stopSelf() // service를 종료한다
            }

            // 플로팅 뷰 전체
            val comebackFloatingView = mFloatingView!!.findViewById<View>(R.id.layout_floating)
            comebackFloatingView.setOnTouchListener(object : View.OnTouchListener {
                private var initialX: Int = 0
                private var initialY: Int = 0
                private var initialTouchX: Float = 0.toFloat()
                private var initialTouchY: Float = 0.toFloat()
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX  // 터치 다운 시의 좌표 저장
                            initialTouchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            val upX = event.rawX
                            val upY = event.rawY
                            if (isAClick(initialTouchX, upX, initialTouchY, upY)) {
                                // 클릭 동작이 감지됨
                                val intent =
                                    this@AnalyzeService.packageManager.getLaunchIntentForPackage(
                                        this@AnalyzeService.packageName
                                    )
                                // 새로운 Activity를 실행하는 것이 아니라 기존에 있던 것을 실행한다.
                                intent?.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                this@AnalyzeService.startActivity(intent)

                                // 다시 fragment로 돌아왔기 때문에 foreground service를 먼저 정지한다
                                if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
                                mFloatingView = null
                                stopForeground(STOP_FOREGROUND_REMOVE)

                                return true
                            }
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            mWindowManager!!.updateViewLayout(mFloatingView, params)
                            return true
                        }
                    }
                    return false
                }
            })
            subscribeRecord()
            subscribeAllSetting(mFloatingView!!.findViewById<PreviewView>(R.id.preview_floating))
            subscribeUserMusicList()

            getRecord(getTodayDate()) // 오늘 눈 깜빡임 정보와, 졸음 횟수 정보를 저장할 Record 객체 생성
            getAllMusic() // 사용자 설정 음악을 사용할 경우, 모든 음악 정보를 들고 온다.
            getAllSetting()
        }
        return START_NOT_STICKY
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

    fun killForegroundService() {
        faceMesh.close()
        faceDetector.close()
        cameraProvider = null
        musicHelper?.clearContext()
        musicHelper = null
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
        mFloatingView = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun startRecording() {
        if (datajob == null) {
            datajob = initJob()
            datajob!!.start()
        }
    }

    fun subscribeRecord() {
        _analyzeRecord.observe(this@AnalyzeService) {
            if (it == null) {
                currentAnayzeResult = AnalyzeResult(getTodayDate(), 1)
            } else {
                currentAnayzeResult = it
            }
            insertRecord(currentAnayzeResult!!)
        }
    }

    private fun subscribeAllSetting(previewView: PreviewView) {
        _allSettings.observe(this@AnalyzeService) {
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
        _music.observe(this@AnalyzeService) { musicList ->
            if (musicList.isNotEmpty()) this.musicList.addAll(musicList)
        }
    }

    private fun startMusic(defaultMode: Boolean) {
        if (musicHelper != null) musicHelper?.releaseMediaPlayer()

        if (defaultMode) {
            musicHelper?.setResMusic()
        } else {
            if (musicList.size > 0) musicHelper?.setMyMusic(musicList)
        }
    }

    private fun initJob() = lifecycleScope.launch(defaultDispatcher, CoroutineStart.LAZY) {
        while (this.isActive) {
            delay(1 * 60 * 1000)  // 30분 대기
            insertWinkCount(
                WinkCount(
                    recordId = currentAnayzeResult!!.id, value = currentWinkCount
                )
            )
            insertDrowsyCount(
                DrowsyCount(
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@AnalyzeService)

        // 카메라 미리보기 설정
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // 이미지 분석 설정
        val imageAnalysis =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        // 이미지 분석기 설정
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this@AnalyzeService)) { imageProxy ->
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

        // 카메라 바인딩
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                this@AnalyzeService, cameraSelector, preview, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this@AnalyzeService))
    }

    fun startCamera(
        startMusic: () -> Unit,
        startRefresh: () -> Unit,
        floatingPreviewView: PreviewView,
    ) {
        startAnalyze(floatingPreviewView) { resultDetector, resultMesh ->
            val upDownAngle = resultDetector.headEulerAngleX
            val leftRightAngle = resultDetector.headEulerAngleY
            startRecording()
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
                        currentWinkCount++
                    }
                }
            }
        }
    }

    // 환기 요청
    private fun startRefresh(a: Int) {

    }

    // musicRepository ///////////////////////////////////////////////////////////////////////////////////////
    private val _music = MutableLiveData<List<Music>>(emptyList())

    /* R : 이벤트 전체 조회 메서드 */
    fun getAllMusic() {
        lifecycleScope.launch {
            musicRepository?.getAllMusic()?.collect {
                _music.value = it
            }
        }
    }

    // settingRepository ///////////////////////////////////////////////////////////////////////////////////////
    private val _guideMode = MutableLiveData<Boolean>(true)

    private val _basicMusicMode = MutableLiveData<Boolean>(true)

    private val _musicVolume = MutableLiveData<Int>(0)

    private val _refreshTerm = MutableLiveData<Int>(0)
    fun getSettingModeInt(key: String) {
        lifecycleScope.launch {
            settingRepository?.getInt(key)?.collect {
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
            settingRepository?.setInt(key, value)
        }
    }

    fun getSettingModeBool(key: String) {
        lifecycleScope.launch {
            settingRepository?.getBoolean(key)?.collect {
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
            settingRepository?.setBoolean(key, value)
        }
    }

    private val _allSettings = MutableLiveData<Pair<MutableList<Boolean>, MutableList<Int>>>(
        mutableListOf<Boolean>() to mutableListOf<Int>()
    )

    fun getAllSetting() {
        lifecycleScope.launch() {
            with(settingRepository) {
                if (this != null) {
                    this.getBoolean(GUIDEMODE)
                        .zip(this.getBoolean(BASICMUSICMODE)) { a, b -> mutableListOf(a, b) }
                        .zip(this.getInt(MUSICVOLUME)) { list, c -> list to mutableListOf(c) }
                        .zip(this.getInt(REFRESHTERM)) { pair, d ->
                            pair.second.add(d)
                            pair.first to pair.second
                        }.collect {
                            Log.d("whatisthis", it.toString())
                            _allSettings.value = it
                        }
                }
            }
        }
    }

    // staticsRepository ///////////////////////////////////////////////////////////////////////////////////////

    private val _allAnalyzeRecord = MutableLiveData<List<AnalyzeResult>>(emptyList())
    fun insertRecord(analyzeResult: AnalyzeResult) {
        lifecycleScope.launch() {
            staticsRepository?.insertRecord(analyzeResult)
        }
    }

    private val _analyzeRecord = MutableLiveData<AnalyzeResult>()

    fun getRecord(time: String) {
        lifecycleScope.launch(mainDispatcher) {
            staticsRepository?.getRecord(time)?.collect {
                _analyzeRecord.value = (it)
            }
        }
    }

    fun getRecord(id: Int) {
        lifecycleScope.launch(mainDispatcher) {
            staticsRepository?.getRecord(id)?.collect {
                _analyzeRecord.value = (it)
            }
        }
    }

    var currentAnayzeResult: AnalyzeResult? = null

    private val _allAnayzeResult = MutableLiveData<Pair<List<WinkCount>, List<DrowsyCount>>>(
        emptyList<WinkCount>() to emptyList<DrowsyCount>()
    )

    var currentWinkCount = 0 //
    fun initWinkCount() {
        currentWinkCount = 0
    }

    fun insertWinkCount(winkCount: WinkCount) {
        lifecycleScope.launch() {
            staticsRepository?.insertWinkCount(winkCount)
        }
    }

    private val _winkCount = MutableLiveData<List<WinkCount>>(emptyList())

    var currentDrowsyCount = 0
    fun initDrowsyCount() {
        currentDrowsyCount = 0
    }

    private val _drowsyCount = MutableLiveData<List<DrowsyCount>>(emptyList())
    fun insertDrowsyCount(drowsyCount: DrowsyCount) {
        lifecycleScope.launch() {
            staticsRepository?.insertDrowsyCount(drowsyCount)
        }
    }

    // 알림 관리 ***********************************************************************************************
    private fun startForegroundService2() {
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

    private fun getMainActivityPendingIntent(): PendingIntent = PendingIntent.getActivity(
        this, 199, Intent(this, MainActivity::class.java).apply {
            this.action = ACTION_SHOW_TRACKING_FRAGMENT
        }, PendingIntent.FLAG_IMMUTABLE
    )
}


//    // 최단 거리 정보 안내
//    private fun startGuide(guideMode: Boolean) {
//        if (guideMode) {
//            requestRelaxData()
//        }
//    }
//
//    private fun requestRelaxData() {
//        initParkingLotRequest()
//        repeatRequestWhenNull(parkingLotRequestTime, "ParkingLot")
//        initShelterRequest()
//        repeatRequestWhenNull(shelterRequestTime, "Shelter")
//        initRestRequest()
//        repeatRequestWhenNull(restRequestTime, "Rest")
//    }
//
//    private fun repeatRequestWhenNull(count: Int, detectType: String) {
//        val job = when (detectType) {
//            "ParkingLot" -> jobGetParkingLot
//            "Shelter" -> jobGetShelter
//            "Rest" -> jobGetRest
//            else -> throw IllegalArgumentException("Unsupported detectType: $detectType")
//        }
//
//        job?.let {
//            if (it.isActive && !it.isCompleted) {
//                handleRequestCompletion(it, detectType)
//            } else {
//                handleRequestCancellation(it, detectType, count)
//            }
//        } ?: run {
//            handleFirstRequest(detectType, count)
//        }
//    }
//
//    private fun handleRequestCompletion(job: Job, detectType: String) {
//        lifecycleScope.launch(defaultDispatcher) {
//            Log.d("whatisthis", "$detectType 정보 이미 요청됨**** ")
//            job.join()
//        }
//    }
//
//    private fun handleRequestCancellation(job: Job, detectType: String, count: Int) {
//        lifecycleScope.launch(defaultDispatcher) {
//            job.cancelAndJoin()
//            Log.d("whatisthis", "$detectType 정보 요청 종료후 재요청!!!!")
//            mFusedLocationProviderClient.setLastLocationEventListener { location ->
//                getJobForType(detectType, count, location)?.let {}
//            }
//        }
//    }
//
//    private fun handleFirstRequest(detectType: String, count: Int) {
//        Log.d("whatisthis", "$detectType 정보 첫요청1")
//        mFusedLocationProviderClient.setLastLocationEventListener { location ->
//            getJobForType(detectType, count, location)?.let {}
//        }
//    }
//
//    private fun getJobForType(detectType: String, count: Int, location: Location): Job? {
//        return when (detectType) {
//            "ParkingLot" -> getALLNearParkingLot(
//                boundingBox = getBoundingBox(
//                    location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
//                ), day = getDayType(), nowTime = getCurrentTime()
//            )
//
//            "Shelter" -> getNearShelter(
//                boundingBox = getBoundingBox(
//                    location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
//                )
//            )
//
//            "Rest" -> getNearRest(
//                boundingBox = getBoundingBox(
//                    location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
//                )
//            )
//
//            else -> null
//        }
//    }
//
//    private fun subscribeParkingLotData() {
//        _parkingLots.observe(this@AnalyzeService) {
//            when (it) {
//                is ResponseState.Uninitialized -> {}
//                is ResponseState.Loading -> {
//                    Log.d("whatisthis", "주차장 데이터 로딩")
//                }
//
//                is ResponseState.Success -> {
//                    if (it.data.isNotEmpty()) {
//                        requestSortingData(it.data)
//                    } else {
//                        Log.d("whatisthis", "근처에 주차장이 없음 반경을 늘려서 재요청")
//                        if (parkingLotRequestTime < 3) {
//                            repeatRequestWhenNull(
//                                parkingLotRequestTime++, "Shelter"
//                            )
//                        }
//                    }
//                }
//
//                is ResponseState.Fail -> {
//                    Log.d("whatisthis", it.message.toString() + it.code)
//                }
//
//                is ResponseState.Error -> {
//                    Log.d("whatisthis", it.exception.toString())
//                }
//            }
//        }
//    }
//
//    private fun subscribeSortedParkingLots() {
//        _sortedParkingLots.observe(this@AnalyzeService) {
//            if (it.size > 0) Log.d("whatisthis", "최단 거리 주차장 " + it[0].toString())
//        }
//
//    }
//
//    private fun subscribeShleterData() {
//        _shelters.observe(this@AnalyzeService) {
//            when (it) {
//                is ResponseState.Uninitialized -> {}
//                is ResponseState.Loading -> {
//                    Log.d("whatisthis", "졸음 쉼터 데이터 로딩")
//                }
//
//                is ResponseState.Success -> {
//                    if (it.data.isNotEmpty()) {
//                        requestSortingData(it.data)
//                    } else {
//                        Log.d("whatisthis", "근처에 쉼터가 없음 반경을 늘려서 재요청")
//                        if (shelterRequestTime < 3) {
//                            repeatRequestWhenNull(
//                                shelterRequestTime++, "Shelter"
//                            )
//                        }
//                    }
//                }
//
//                is ResponseState.Fail -> {
//                    Log.d("whatisthis", it.message.toString() + it.code)
//                }
//
//                is ResponseState.Error -> {
//                    Log.d("whatisthis", it.exception.toString())
//                }
//            }
//        }
//
//    }
//
//    private fun subscribeSortedShelterData() {
//        _sortedShelters.observe(this@AnalyzeService) {
//            if (it.size > 0) Log.d("whatisthis", "최단 거리 쉼터 " + it[0].toString())
//        }
//
//    }
//
//    private fun subscribeRestData() {
//        _rests.observe(this@AnalyzeService) {
//            when (it) {
//                is ResponseState.Uninitialized -> {}
//                is ResponseState.Loading -> {
//                    Log.d("whatisthis", "휴게소 데이터 로딩")
//                }
//
//                is ResponseState.Success -> {
//                    if (it.data.isNotEmpty()) {
//                        requestSortingData(it.data)
//                    } else {
//                        Log.d("whatisthis", "근처에 휴게소가 없음 반경을 늘려서 재요청")
//                        if (restRequestTime < 3) {
//                            repeatRequestWhenNull(
//                                restRequestTime++, "Shelter"
//                            )
//                        }
//                    }
//                }
//
//                is ResponseState.Fail -> {
//                    Log.d("whatisthis", it.message.toString() + it.code)
//                }
//
//                is ResponseState.Error -> {
//                    Log.d("whatisthis", it.exception.toString())
//                }
//            }
//
//
//        }
//    }
//
//    private fun subscribeSortedRests() {
//        _sortedRests.observe(this@AnalyzeService) {
//            if (it.size > 0) Log.d("whatisthis", "최단 거리 휴게소 " + it[0].toString())
//        }
//
//    }
//
//    private inline fun <reified T> requestSortingData(data: List<T>) {
//        mFusedLocationProviderClient.setLastLocationEventListener { nowLocation ->
//            when (T::class) { // reified T를 사용해 함수 내에서 실제 T::class를 호출한다.
//                parkingLotItem::class -> {
//                    val parkingLots = data as List<parkingLotItem>
//                    if (jobSortedParkingLot != null) {
//                        lifecycleScope.launch(defaultDispatcher) {
//                            jobSortedParkingLot!!.cancelAndJoin()
//                            Log.d("whatisthis", "주차장(정렬) 정보 요청 종료후 재요청")
//                            jobSortedParkingLot = sortParkingLots(nowLocation, parkingLots)
//                        }
//                    } else {
//                        Log.d("whatisthis", "주차장(정렬) 정보 첫요청2")
//                        jobSortedParkingLot = sortParkingLots(nowLocation, parkingLots)
//                    }
//                }
//
//                shelterItem::class -> {
//                    val shelters = data as List<shelterItem>
//                    if (jobSortedShelter != null) {
//                        lifecycleScope.launch(defaultDispatcher) {
//                            jobSortedShelter!!.cancelAndJoin()
//                            Log.d("whatisthis", "쉼터(정렬) 정보 요청 종료후 재요청")
//                            jobSortedShelter = sortShelters(nowLocation, shelters)
//                        }
//                    } else {
//                        Log.d("whatisthis", "쉼터(정렬) 정보 첫요청2")
//                        jobSortedShelter = sortShelters(nowLocation, shelters)
//                    }
//                }
//
//                restItem::class -> {
//                    val rests = data as List<restItem>
//                    if (jobSortedRest != null) {
//                        lifecycleScope.launch(defaultDispatcher) {
//                            jobSortedRest!!.cancelAndJoin()
//                            Log.d("whatisthis", "휴게소(정렬) 정보 요청 종료후 재요청")
//                            jobSortedRest = sortRests(nowLocation, rests)
//                        }
//                    } else {
//                        Log.d("whatisthis", "휴게소(정렬) 정보 첫요청2")
//                        jobSortedRest = sortRests(nowLocation, rests)
//                    }
//                }
//
//                else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
//            }
//        }
//    }

//    // relaxRepository ///////////////////////////////////////////////////////////////////////////////////////
//    var shelterRequestTime: Int = 0
//    fun initShelterRequest() {
//        shelterRequestTime = 0
//    }
//
//    var restRequestTime: Int = 0
//    fun initRestRequest() {
//        restRequestTime = 0
//    }
//
//    var parkingLotRequestTime: Int = 0
//    fun initParkingLotRequest() {
//        parkingLotRequestTime = 0
//    }
//
//    var checkDrowsy: Boolean = true
//
//    // 모든 휴게소 데이터
//    private val _rests: MutableLiveData<ResponseState<List<restItem>>> =
//        MutableLiveData(ResponseState.Uninitialized)
//
//    fun getNearRest(boundingBox: BoundingBox) = lifecycleScope.launch(ioDispatcher) {
//        _rests.postValue(ResponseState.Loading)
//        try {
//            relaxRepository?.getAllRest(boundingBox)?.collect {
//                _rests.postValue(it)
//            }
//        } catch (error: Throwable) {
//            _rests.postValue(ResponseState.Error(error))
//        }
//    }
//
//    private val _sortedRests: MutableLiveData<List<restItem>> = MutableLiveData()
//    fun sortRests(nowLocation: Location, rests: List<restItem>) =
//        lifecycleScope.launch(defaultDispatcher) {
//            _sortedRests.postValue((rests.sortedBy {// 정렬
//                nowLocation.calculateDistance(
//                    it.latitude.toDouble(), it.longitude.toDouble()
//                )
//            }))
//        }
//
//
//    // 모든 쉼터 데이터
//    private val _shelters: MutableLiveData<ResponseState<List<shelterItem>>> =
//        MutableLiveData(ResponseState.Uninitialized)
//
//    fun getNearShelter(boundingBox: BoundingBox) = lifecycleScope.launch(ioDispatcher) {
//        _shelters.postValue(ResponseState.Loading)
//        try {
//            relaxRepository?.getAllShelter(boundingBox)?.collect {
//                _shelters.postValue(it)
//            }
//        } catch (error: Throwable) {
//            _shelters.postValue(ResponseState.Error(error))
//        }
//    }
//
//    private val _sortedShelters: MutableLiveData<List<shelterItem>> = MutableLiveData()
//    fun sortShelters(nowLocation: Location, shelters: List<shelterItem>) =
//        lifecycleScope.launch(defaultDispatcher) {
//            _sortedShelters.postValue((shelters.sortedBy {// 정렬
//                nowLocation.calculateDistance(
//                    it.latitude.toDouble(), it.longitude.toDouble()
//                )
//            }))
//        }
//
//    private val _parkingLots: MutableLiveData<ResponseState<List<parkingLotItem>>> =
//        MutableLiveData(ResponseState.Uninitialized)
//
//    fun getALLNearParkingLot(
//        boundingBox: BoundingBox,
//        parkingchargeInfo: String = "무료",
//        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
//        day: DAY,
//        nowTime: String,
//    ) = lifecycleScope.launch(defaultDispatcher) {
//        _parkingLots.postValue(ResponseState.Loading)
//        try {
//            relaxRepository?.getAllParkingLot(
//                boundingBox, parkingchargeInfo, numOfRows, day, nowTime
//            )?.catch { error ->
//                _parkingLots.postValue(ResponseState.Error(error))
//            }?.collect {
//                if (it is ResponseState.Success) {
//                    combine(it.data) { responses ->
//                        val combinedList =
//                            responses.filterIsInstance<ResponseState.Success<List<parkingLotItem>>>()
//                                .flatMap { it.data }
//                        ResponseState.Success(combinedList)
//                    }.cancellable().catch { error ->
//                        _parkingLots.postValue(ResponseState.Error(error))
//                    }.collectLatest { listItem ->
//                        _parkingLots.postValue(listItem)
//                    }
//                } else if (it is ResponseState.Fail) {
//                    _parkingLots.postValue(ResponseState.Fail(it.code, it.message))
//                }
//            }
//        } catch (error: Throwable) {
//            _parkingLots.postValue(ResponseState.Error(error))
//        }
//    }
//
//    private val _sortedParkingLots: MutableLiveData<List<parkingLotItem>> = MutableLiveData()
//    fun sortParkingLots(nowLocation: Location, parkingLots: List<parkingLotItem>) =
//        lifecycleScope.launch(defaultDispatcher) {
//            _sortedParkingLots.postValue((parkingLots.sortedBy {// 정렬
//                nowLocation.calculateDistance(
//                    it.latitude.toDouble(), it.longitude.toDouble()
//                )
//            }))
//        }