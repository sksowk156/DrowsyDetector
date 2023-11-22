package com.paradise.drowsydetector.view.analyze

import android.location.Location
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.data.local.room.record.AnalyzeResult
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import com.paradise.drowsydetector.databinding.FragmentAnalyzeBinding
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import com.paradise.drowsydetector.utils.DEFAULT_RADIUSKM
import com.paradise.drowsydetector.utils.DROWSY_THREDHOLD
import com.paradise.drowsydetector.utils.LocationHelper
import com.paradise.drowsydetector.utils.MusicHelper
import com.paradise.drowsydetector.utils.NO_STANDARD
import com.paradise.drowsydetector.utils.OUT_OF_ANGLE
import com.paradise.drowsydetector.utils.OvalOverlayView
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.SMILE_THREDHOLD
import com.paradise.drowsydetector.utils.STANDARD_IN_ANGLE
import com.paradise.drowsydetector.utils.TIME_THREDHOLD
import com.paradise.drowsydetector.utils.calRatio
import com.paradise.drowsydetector.utils.checkHeadAngleInNoStandard
import com.paradise.drowsydetector.utils.checkHeadAngleInStandard
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.getBoundingBox
import com.paradise.drowsydetector.utils.getCurrentTime
import com.paradise.drowsydetector.utils.getDayType
import com.paradise.drowsydetector.utils.getRandomElement
import com.paradise.drowsydetector.utils.getTodayDate
import com.paradise.drowsydetector.utils.isInLeftRight
import com.paradise.drowsydetector.utils.launchWithRepeatOnLifecycle
import com.paradise.drowsydetector.utils.mainDispatcher
import com.paradise.drowsydetector.utils.showToast
import com.paradise.drowsydetector.viewmodel.AnalyzeViewModel
import com.paradise.drowsydetector.viewmodel.MusicViewModel
import com.paradise.drowsydetector.viewmodel.SettingViewModel
import com.paradise.drowsydetector.viewmodel.StaticsViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.paradise.drowsydetector.data.remote.parkinglot.Item as parkingLotItem
import com.paradise.drowsydetector.data.remote.rest.Item as restItem
import com.paradise.drowsydetector.data.remote.shelter.Item as shelterItem

class AnalyzeFragment :
    BaseViewbindingFragment<FragmentAnalyzeBinding>(FragmentAnalyzeBinding::inflate) {
    private val analyzeViewModel: AnalyzeViewModel by viewModels() {
        AnalyzeViewModel.AnalyzeViewModelFactory(getApplicationContext().relaxRepository)
    }
    private val settingViewModel: SettingViewModel by activityViewModels()
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val staticsViewModel: StaticsViewModel by activityViewModels()

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
        LocationHelper.getInstance(
            getApplicationContext().fusedLocationProviderClient, getApplicationContext().geocoder
        )
    }

    private lateinit var overlay: OvalOverlayView
    private var standard: Double? = null
    private var isInAngleRange = false
    private var isInDrowsyState = false
    private var timeCheckDrowsy: Long? = null
    private val standardRatioList = mutableListOf<Double>()
    private var eyeClosed: Boolean = false
    private var timerJob: Job? = null // 기본값 설정 타이머
    private var datajob: Job? = null // 눈 깜빡임 감지

    private var musicHelper: MusicHelper? = null
    private var jobSortedParkingLot: Job? = null
    private var jobSortedShelter: Job? = null
    private var jobSortedRest: Job? = null
    private var jobGetParkingLot: Job? = null
    private var jobGetShelter: Job? = null
    private var jobGetRest: Job? = null
    override fun onDestroyViewInFragMent() {
        musicHelper?.clearContext()
        musicHelper = null

        stopTimer()
        faceDetector.close()
        faceMesh.close()
        cameraExecutor.shutdown()
        jobList.forEach { it.cancel() }
    }

    override fun onViewCreated() {
        // 툴바 세팅
        binding.analyzeToolbar.setToolbarMenu("졸음 감지", true).inflateResetMenu {
            binding.analyzeTextGazerequest.visibility = View.VISIBLE
            standard = null
        }
        analyzeViewModel.checkDrowsy

        cameraExecutor = Executors.newSingleThreadExecutor()
        executorList.add(cameraExecutor)
        overlay = binding.analyzeOverlay
        musicHelper = MusicHelper.getInstance(requireContext())
        subscribeRecord()
        staticsViewModel.getRecord(getTodayDate())
        subscribeAllSetting()
    }

    /**
     * Subscribe all setting
     *
     * 설정이 변경될 때마다 졸음 감지시 동작이 달라진다.
     *
     * 이것을 camera 로직 내부에 subscribe해서 관찰해도 되지만, camera 로직 내부는 while(true)로 되어있어 졸음을 감지할 때마다 subscribe가 갱신된다.
     *
     * 이런 현상을 피하기 위해 콜백으로 설정해 한 번의 subscribe만 발생하도록 하였다.
     */
    private fun subscribeAllSetting() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            settingViewModel.allSettings.collect {
                val defaultMusic = it.first[1]
                val onGuide = it.first[0]
                val onRefresh = it.second[1]
                Log.d(
                    "whatisthis",
                    defaultMusic.toString() + " " + onGuide.toString() + " " + onRefresh.toString()
                )

                if (onGuide) {
                    // 내 위치를 중심으로 근처에 있는 데이터 관찰
                    subscribeParkingLotData()
                    subscribeShleterData()
                    subscribeRestData()

                    // 내 위치를 중심으로 가까운 순으로 정렬한 데이터 관찰
                    subscribeSortedParkingLots()
                    subscribeSortedShelterData()
                    subscribeSortedRests()
                }

                startCamera(startMusic = { startMusic(defaultMusic) },
                    startGuide = { startGuide(onGuide) },
                    startRefresh = { startRefresh(onRefresh) })
                initTimer(2000)
            }
        }
    }

    /**
     * Init music
     *
     * 알림음을 초기화 한다.
     * @param defaultMode
     */
    private fun startMusic(defaultMode: Boolean) {
        if (musicHelper != null) {
            musicHelper?.releaseMediaPlayer()
        }

        if (defaultMode) {
            musicHelper?.setResMusic(viewLifecycleOwner)
        } else {
            subscribeUserMusicList()
        }
    }

    /**
     * Subscribe user music list
     *
     * 사용자 음악을 사용할 경우, Room에 저장된 리스트를 관찰한다.
     */
    private fun subscribeUserMusicList() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            musicViewModel.music.collect { musicList ->
                val randomMusic = musicList.getRandomElement()!!
                musicHelper?.startMusic(randomMusic, viewLifecycleOwner)
            }
        }
    }

    private fun initJob() =
        viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher, CoroutineStart.LAZY) {
            while (this.isActive) {
                delay(1 * 60 * 1000)  // 30분 대기
                Log.d(
                    "whatisthis",
                    "counting : " + staticsViewModel.currentAnayzeResult!!.id.toString() + " " + staticsViewModel.currentWinkCount + " " + staticsViewModel.currentDrowsyCount
                )
                staticsViewModel.run {
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
        }

    /**
     * Start camera
     *
     * @param startMusic, 음악 설정에 따라 음악 정보가 달라지기 때문에 음악 설정 값을 콜백받아서 처리
     * @receiver
     */
    private fun startCamera(
        startMusic: () -> Unit,
        startGuide: () -> Unit,
        startRefresh: () -> Unit,
    ) {
        var cameraController = LifecycleCameraController(requireContext())
        val previewView: PreviewView = binding.analyzeViewFinder

        // 전면 카메라
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController.bindToLifecycle(this)
        // 이미지 분석
        previewView.controller = cameraController.apply {
            setImageAnalysisAnalyzer(cameraExecutor, MlKitAnalyzer(
                listOf(faceDetector, faceMesh),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(requireContext())
            ) { result: MlKitAnalyzer.Result? ->
                // 화면 회전 시 쓰레드가 종료되는 도중에 binding이 null이 되기 때문에 binding이 먼저 null 되면 쓰레드 내부가 동작하지 못하게 막는다.(nullpointexception 방지)
                if (this@AnalyzeFragment.isAdded && result != null) {
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

                    val upDownAngle = resultDetector.headEulerAngleX
                    val leftRightAngle = resultDetector.headEulerAngleY

                    val eyeRatio = calRatio(upDownAngle, leftRightAngle, resultMesh)

                    if (standard == null) {
                        checkHeadPoseInNoStandard(eyeRatio, leftRightAngle, upDownAngle)
                    } else {
                        val eyeState = eyeRatio / standard!! // 눈 상태
                        checkHeadPoseInStandard(leftRightAngle, upDownAngle)

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
                                        if (analyzeViewModel.checkDrowsy) { // 한번만 동작하게
                                            analyzeViewModel.checkDrowsy = false
                                            staticsViewModel.currentDrowsyCount++
                                            // 경고음
                                            startMusic()
                                            // 최단 거리에 있는 졸음 쉼터, 휴게소, 무료 주차장 안내
                                            startGuide()
                                            // 환기 요청
                                            startRefresh()
                                        }
                                        binding.analyzeTextDrowsycheck.visibility = View.VISIBLE
                                    }
                                }
                            }
                        } else {
                            setEyeStateInOpen()
                        }
                    }
                }
            })
        }
    }

    private fun checkHeadPoseInNoStandard(
        eyeRatio: Double,
        leftRightAngle: Float,
        upDownAngle: Float,
    ) {// 각도 임계값
        if (checkHeadAngleInNoStandard(upDownAngle, leftRightAngle)) {
            setHeadIn(eyeRatio)
        } else {
            setHeadOut(leftRightAngle, upDownAngle)
        }
    }

    private fun setHeadIn(eyeRatio: Double) {
        overlay.onZeroAngle(STANDARD_IN_ANGLE)
        if (!isInAngleRange) {
            startTimer()
            isInAngleRange = true
        } else {
            standardRatioList.add(eyeRatio)
        }
    }

    private fun setHeadOut(leftRightAngle: Float, upDownAngle: Float) {
        if (isInLeftRight(leftRightAngle)) {
            if (upDownAngle > 4) binding.analyzeTextGazerequest.text = "고개를 살짝만 내려 주세요"
            else if (upDownAngle < -4) binding.analyzeTextGazerequest.text = "고개를 살짝만 들어 주세요"
        } else {
            binding.analyzeTextGazerequest.text = "고개를 살짝만 돌려 주세요"
        }

        overlay.onZeroAngle(NO_STANDARD)
        if (isInAngleRange) {
            isInAngleRange = false
            stopTimer()
            standardRatioList.clear()
        }
    }

    private fun checkHeadPoseInStandard(leftRightAngle: Float, upDownAngle: Float) {// 각도 임계값
        if (checkHeadAngleInStandard(leftRightAngle, upDownAngle)) {
            // 카메라 정면 요청
            binding.analyzeTextScreenrequest.visibility = View.VISIBLE
            binding.analyzeTextDrowsycheck.visibility = View.INVISIBLE
            overlay.onZeroAngle(OUT_OF_ANGLE)
        } else {
            binding.analyzeTextScreenrequest.visibility = View.INVISIBLE
            overlay.onZeroAngle(STANDARD_IN_ANGLE)
        }
    }

    private fun setEyeStateInOpen() {
        eyeClosed = false
        if (isInDrowsyState) {
            analyzeViewModel.checkDrowsy = true
            binding.analyzeTextDrowsycheck.visibility = View.INVISIBLE
            isInDrowsyState = false
            timeCheckDrowsy = null
        }
    }

    private fun checkEyeWink(eyeState: Double, resultDetector: Face) {
        // 눈 깜빡임 카운팅
        if (eyeState <= 0.4) {
            val leftEyeOpen = resultDetector.leftEyeOpenProbability
            val righEyeOpen = resultDetector.rightEyeOpenProbability

            if (datajob != null && datajob!!.isActive) {
                if (leftEyeOpen != null && righEyeOpen != null) {
                    if (leftEyeOpen < 0.3 && righEyeOpen < 0.3 && !eyeClosed) {
                        eyeClosed = true // frame이 높아서 한번에 많이 처리될 수 있기 때문
                        // 눈이 감겨 있음
                        staticsViewModel.currentWinkCount++
                    }
                }
            }
        }
    }

    // 환기 요청
    private fun startRefresh(a: Int) {

    }

    // 최단 거리 정보 안내
    private fun startGuide(guideMode: Boolean) {
        if (guideMode) {
            requestRelaxData()
        }
    }

    private fun requestRelaxData() {
        analyzeViewModel.apply {
            initParkingLotRequest()
            repeatRequestWhenNull(parkingLotRequestTime, "ParkingLot")
            initShelterRequest()
            repeatRequestWhenNull(shelterRequestTime, "Shelter")
            initRestRequest()
            repeatRequestWhenNull(restRequestTime, "Rest")
        }
    }

    private fun repeatRequestWhenNull(count: Int, detectType: String) {
        val job = when (detectType) {
            "ParkingLot" -> jobGetParkingLot
            "Shelter" -> jobGetShelter
            "Rest" -> jobGetRest
            else -> throw IllegalArgumentException("Unsupported detectType: $detectType")
        }

        job?.let {
            if (it.isActive && !it.isCompleted) {
                handleRequestCompletion(it, detectType, count)
            } else {
                handleRequestCancellation(it, detectType, count)
            }
        } ?: run {
            handleFirstRequest(detectType, count)
        }

        Log.d("whatisthis", jobList.size.toString())
    }

    private fun handleRequestCompletion(job: Job, detectType: String, count: Int) {
        viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
            Log.d("whatisthis", "$detectType 정보 이미 요청됨**** ")
            job.join()
            jobList.remove(job)
        }
    }

    private fun handleRequestCancellation(job: Job, detectType: String, count: Int) {
        viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
            jobList.remove(job)
            job.cancelAndJoin()
            Log.d("whatisthis", "$detectType 정보 요청 종료후 재요청!!!!")
            mFusedLocationProviderClient.setLastLocationEventListener { location ->
                getJobForType(detectType, count, location)?.let {
                    jobList.add(it)
                }
            }
        }
    }

    private fun handleFirstRequest(detectType: String, count: Int) {
        Log.d("whatisthis", "$detectType 정보 첫요청1")
        mFusedLocationProviderClient.setLastLocationEventListener { location ->
            getJobForType(detectType, count, location)?.let {
                jobList.add(it)
            }
        }
    }

    private fun getJobForType(detectType: String, count: Int, location: Location): Job? {
        return when (detectType) {
            "ParkingLot" -> analyzeViewModel.getALLNearParkingLot(
                boundingBox = getBoundingBox(
                    location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
                ), day = getDayType(), nowTime = getCurrentTime()
            )

            "Shelter" -> analyzeViewModel.getNearShelter(
                boundingBox = getBoundingBox(
                    location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
                )
            )

            "Rest" -> analyzeViewModel.getNearRest(
                boundingBox = getBoundingBox(
                    location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
                )
            )

            else -> null
        }
    }

    private fun subscribeParkingLotData() = viewLifecycleOwner.launchWithRepeatOnLifecycle(
        dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
    ) {
        analyzeViewModel.parkingLots.collect {
            when (it) {
                is ResponseState.Uninitialized -> {}
                is ResponseState.Loading -> {
                    Log.d("whatisthis", "주차장 데이터 로딩")
                }

                is ResponseState.Success -> {
                    checkBackPress {
                        if (it.data.isNotEmpty()) {
                            requestSortingData(it.data)
                        } else {
                            Log.d("whatisthis", "근처에 주차장이 없음 반경을 늘려서 재요청")
                            if (analyzeViewModel.parkingLotRequestTime < 3) {
                                repeatRequestWhenNull(
                                    analyzeViewModel.parkingLotRequestTime++, "Shelter"
                                )
                            }
                        }
                    }
                }

                is ResponseState.Fail -> {
                    Log.d("whatisthis", it.message.toString() + it.code)
                }

                is ResponseState.Error -> {
                    Log.d("whatisthis", it.exception.toString())
                }
            }
        }
    }

    private fun subscribeSortedParkingLots() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.sortedParkingLots.collect {
                if (it.size > 0) Log.d("whatisthis", "최단 거리 주차장 " + it[0].toString())
            }
        }
    }

    private fun subscribeShleterData() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.shelters.collect {
                when (it) {
                    is ResponseState.Uninitialized -> {}
                    is ResponseState.Loading -> {
                        Log.d("whatisthis", "졸음 쉼터 데이터 로딩")
                    }

                    is ResponseState.Success -> {
                        checkBackPress {
                            if (it.data.isNotEmpty()) {
                                requestSortingData(it.data)
                            } else {
                                Log.d("whatisthis", "근처에 쉼터가 없음 반경을 늘려서 재요청")
                                if (analyzeViewModel.shelterRequestTime < 3) {
                                    repeatRequestWhenNull(
                                        analyzeViewModel.shelterRequestTime++, "Shelter"
                                    )
                                }
                            }
                        }
                    }

                    is ResponseState.Fail -> {
                        Log.d("whatisthis", it.message.toString() + it.code)
                    }

                    is ResponseState.Error -> {
                        Log.d("whatisthis", it.exception.toString())
                    }
                }
            }
        }
    }

    private fun subscribeSortedShelterData() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.sortedShelters.collect {
                if (it.size > 0) Log.d("whatisthis", "최단 거리 쉼터 " + it[0].toString())
            }
        }
    }

    private fun subscribeRestData() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.rests.collect {
                when (it) {
                    is ResponseState.Uninitialized -> {}
                    is ResponseState.Loading -> {
                        Log.d("whatisthis", "휴게소 데이터 로딩")
                    }

                    is ResponseState.Success -> {
                        checkBackPress {
                            if (it.data.isNotEmpty()) {
                                requestSortingData(it.data)
                            } else {
                                Log.d("whatisthis", "근처에 휴게소가 없음 반경을 늘려서 재요청")
                                if (analyzeViewModel.restRequestTime < 3) {
                                    repeatRequestWhenNull(
                                        analyzeViewModel.restRequestTime++, "Shelter"
                                    )
                                }
                            }
                        }
                    }

                    is ResponseState.Fail -> {
                        Log.d("whatisthis", it.message.toString() + it.code)
                    }

                    is ResponseState.Error -> {
                        Log.d("whatisthis", it.exception.toString())
                    }
                }
            }
        }
    }

    private fun subscribeSortedRests() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.sortedRests.collect {
                if (it.size > 0) Log.d("whatisthis", "최단 거리 휴게소 " + it[0].toString())
            }
        }
    }

    /**
     * Sort data
     *
     * 중복되는 코드를 없애기 위해 제너릭을 활용해 코드를 줄였다.
     *
     * 제너릭 T가 타입이 다 달라 이것을 구분하기 위해서는 함수 내부에서 제너릭 타입에 따른 분기 처리가 필요했다.
     *\
     * 제너릭 T에 대한 정보는 런타임 시 사라지기 때문에 이것을 유지하기 위해서 reified가 필요했고, reified를 사용하기 위해 inline 함수가 필요했다.
     *
     * @param T
     * @param data
     */

    private inline fun <reified T> requestSortingData(data: List<T>) {
        mFusedLocationProviderClient.setLastLocationEventListener { nowLocation ->
            when (T::class) { // reified T를 사용해 함수 내에서 실제 T::class를 호출한다.
                parkingLotItem::class -> {
                    val parkingLots = data as List<parkingLotItem>
                    if (jobSortedParkingLot != null) {
                        viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                            jobList.remove(jobSortedParkingLot!!)
                            jobSortedParkingLot!!.cancelAndJoin()
                            Log.d("whatisthis", "주차장(정렬) 정보 요청 종료후 재요청")
                            jobSortedParkingLot =
                                analyzeViewModel.sortParkingLots(nowLocation, parkingLots)
                            jobList.add(jobSortedParkingLot!!)
                        }
                    } else {
                        Log.d("whatisthis", "주차장(정렬) 정보 첫요청2")
                        jobSortedParkingLot =
                            analyzeViewModel.sortParkingLots(nowLocation, parkingLots)
                        jobList.add(jobSortedParkingLot!!)
                    }
                }

                shelterItem::class -> {
                    val shelters = data as List<shelterItem>
                    if (jobSortedShelter != null) {
                        viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                            jobList.remove(jobSortedShelter!!)
                            jobSortedShelter!!.cancelAndJoin()
                            Log.d("whatisthis", "쉼터(정렬) 정보 요청 종료후 재요청")
                            jobSortedShelter = analyzeViewModel.sortShelters(nowLocation, shelters)
                            jobList.add(jobSortedShelter!!)
                        }
                    } else {
                        Log.d("whatisthis", "쉼터(정렬) 정보 첫요청2")
                        jobSortedShelter = analyzeViewModel.sortShelters(nowLocation, shelters)
                        jobList.add(jobSortedShelter!!)
                    }
                }

                restItem::class -> {
                    val rests = data as List<restItem>
                    if (jobSortedRest != null) {
                        viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                            jobList.remove(jobSortedRest!!)
                            jobSortedRest!!.cancelAndJoin()
                            Log.d("whatisthis", "휴게소(정렬) 정보 요청 종료후 재요청")
                            jobSortedRest = analyzeViewModel.sortRests(nowLocation, rests)
                            jobList.add(jobSortedRest!!)
                        }
                    } else {
                        Log.d("whatisthis", "휴게소(정렬) 정보 첫요청2")
                        jobSortedRest = analyzeViewModel.sortRests(nowLocation, rests)
                        jobList.add(jobSortedRest!!)
                    }
                }

                else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
            }
        }
    }

    private fun initTimer(msFuture: Long) {
        binding.apply {
            analyzeProgress.setMax(msFuture.toInt())
        }
    }

    private fun startTimer() {
        timerJob = viewLifecycleOwner.lifecycleScope.launch(mainDispatcher) {
            binding.analyzeProgress.visibility = View.VISIBLE
            binding.analyzeTextGazerequest.text = "2초간 응시해주세요\n(눈 크기 측정 중)"
            delay(binding.analyzeProgress.max.toLong())

            binding.analyzeProgress.visibility = View.INVISIBLE
            binding.analyzeTextGazerequest.text = "얼굴을 정방향으로 유지해주세요"
            binding.analyzeTextGazerequest.visibility = View.INVISIBLE

            if (standardRatioList.size > 0) {
                standardRatioList.sort()
                val mid = standardRatioList.slice(3 until standardRatioList.size - 3)
                standard = mid.average()

                if (datajob == null) {
                    datajob = initJob()
                    jobList.add(datajob!!)
                } else {
                    datajob!!.cancel()
                    jobList.remove(datajob!!)
                    datajob = initJob()
                    jobList.add(datajob!!)
                }

                staticsViewModel.insertRecord(staticsViewModel.currentAnayzeResult!!)

                datajob!!.start()
                showToast("설정 완료")
            }
        }
    }

    private fun subscribeRecord() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
        ) {
            staticsViewModel.analyzeRecord.collect {
                if (it == null) {
                    staticsViewModel.currentAnayzeResult = AnalyzeResult(getTodayDate(), 1)
                } else {
                    staticsViewModel.currentAnayzeResult = it
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        binding.analyzeProgress.visibility = View.INVISIBLE
        binding.analyzeTextGazerequest.text = "얼굴을 정방향으로 유지해주세요"
    }

    private fun checkBackPress(block: () -> Unit) {
        if (inBackPress) {
            binding.layoutAnalyze.isClickable = false
            return
        } else {
            block()
        }
    }
}