package com.paradise.drowsydetector.view.analyze

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.databinding.FragmentAnalyzeBinding
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import com.paradise.drowsydetector.utils.DEFAULT_MUSIC_DURATION
import com.paradise.drowsydetector.utils.DEFAULT_RADIUSKM
import com.paradise.drowsydetector.utils.DROWSY_THREDHOLD
import com.paradise.drowsydetector.utils.LEFT_RIGHT_ANGLE_THREDHOLD
import com.paradise.drowsydetector.utils.LocationHelper
import com.paradise.drowsydetector.utils.MusicHelper
import com.paradise.drowsydetector.utils.NO_STANDARD
import com.paradise.drowsydetector.utils.OUT_OF_ANGLE
import com.paradise.drowsydetector.utils.OvalOverlayView
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.SMILE_THREDHOLD
import com.paradise.drowsydetector.utils.STANDARD_IN_ANGLE
import com.paradise.drowsydetector.utils.TIME_THREDHOLD
import com.paradise.drowsydetector.utils.UP_DOWN_ANGLE_THREDHOLD
import com.paradise.drowsydetector.utils.calRatio
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.getBoundingBox
import com.paradise.drowsydetector.utils.getCurrentTime
import com.paradise.drowsydetector.utils.getDayType
import com.paradise.drowsydetector.utils.getRandomElement
import com.paradise.drowsydetector.utils.inflateResetMenu
import com.paradise.drowsydetector.utils.launchWithRepeatOnLifecycle
import com.paradise.drowsydetector.utils.showToast
import com.paradise.drowsydetector.viewmodel.AnalyzeViewModel
import com.paradise.drowsydetector.viewmodel.MusicViewModel
import com.paradise.drowsydetector.viewmodel.SettingViewModel
import kotlinx.coroutines.Job
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.paradise.drowsydetector.data.remote.parkinglot.Item as parkingLotItem
import com.paradise.drowsydetector.data.remote.rest.Item as restItem
import com.paradise.drowsydetector.data.remote.shelter.Item as shelterItem

class AnalyzeFragment :
    BaseViewbindingFragment<FragmentAnalyzeBinding>(FragmentAnalyzeBinding::inflate) {
    private val analyzeViewModel: AnalyzeViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()
    private val musicViewModel: MusicViewModel by activityViewModels()

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
    private lateinit var timer: CountDownTimer
    var isInAngleRange = false
    var isInDrowsyState = false
    var timeCheckDrowsy: Long? = null
    val standardRatioList = mutableListOf<Double>()
//    private val record = mutableListOf<Double>()

    private var musicHelper: MusicHelper? = null

    override fun onDestroyViewInFragMent() {
        musicHelper = null
        stopTimer()
        faceDetector.close()
        faceMesh.close()
        cameraExecutor.shutdown()
    }

    override fun onViewCreated() {
        // 툴바 세팅
        binding.analyzeToolbar.setToolbarMenu("졸음 감지", true).inflateResetMenu {
            binding.analyzeTextGazerequest.visibility = View.VISIBLE
            standard = null
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        overlay = binding.analyzeOverlay

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
        subscribeMusicSetting { defaultMusic ->
            subscribeGuideSetting { onGuide ->
                subscribeRefreshSetting { onRefresh ->
                    startCamera() {
                        startMusic(defaultMusic)
                    }
                    initTimer(2000)
                }
            }
        }
    }

    /**
     * Subscribe music setting
     *
     * 음악 설정을 관찰한다.(true : 기본 음악, false : 사용자 음악)
     */
    private fun subscribeMusicSetting(block: (Boolean) -> Unit) {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            settingViewModel.mode.collect {
                block(it)
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
        if (defaultMode) {
            setResMusic()
        } else {
            subscribeUserMusicList()
        }
    }

    /**
     * Start res music
     *
     * Res에 저장된 음악 리스트에서 음악을 랜덤으로 뽑아 MusicHelper.Builder()에 저장한다.
     */
    private fun setResMusic() {
        val randomMusic = listOf<Int>(
            (R.raw.alert1), (R.raw.alert2), (R.raw.alert3), (R.raw.alert4), (R.raw.alert5)
        ).getRandomElement()!!

        musicHelper = MusicHelper.Builder().setMusic(requireContext(), resId = randomMusic)
            .setTime(viewLifecycleOwner, duration = DEFAULT_MUSIC_DURATION).startMusic()
    }

    /**
     * Subscribe user music list
     *
     * 사용자 음악을 사용할 경우, Room에 저장된 리스트를 관찰한다.
     */
    private fun subscribeUserMusicList() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            musicViewModel.music.collect { musicList ->
                if (musicList != null) {
                    setRoomMusic(musicList)
                }
            }
        }
    }

    /**
     * Start room music
     *
     * Room에 저장된 음악 리스트에서 음악을 랜덤으로 뽑아 MusicHelper.Builder()에 저장한다.
     * @param musicList
     */
    private fun setRoomMusic(musicList: List<Music>) {
        val randomMusic = musicList.getRandomElement()!!
        musicHelper = MusicHelper.Builder().setMusic(requireContext(), music = randomMusic).setTime(
            viewLifecycleOwner,
            startTime = randomMusic.startTime.toInt(),
            duration = randomMusic.durationTime
        ).startMusic()
    }

    /**
     * Subscribe guide setting
     *
     * 안내 설정을 관찰한다. (true : 졸음 감지시 최단 거리 졸음 쉼터, 주차장, 휴게소에 대한 정보를 안내 받음, false : 안받음)
     */
    private fun subscribeGuideSetting(block: (Boolean) -> Unit) {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            settingViewModel.mode.collect {
                block(it)
                if (it) {
                    // 내 위치를 중심으로 근처에 있는 데이터 관찰
                    subscribeParkingLotData()
                    subscribeShleterData()
                    subscribeRestData()
                    // 내 위치를 중심으로 가까운 순으로 정렬한 데이터 관찰
                    subscribeSortedParkingLots()
                    subscribeSortedShelterData()
                    subscribeSortedRests()
                }
            }
        }
    }

    private fun subscribeRefreshSetting(block: (Boolean) -> Unit) {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            settingViewModel.mode.collect {
                block(it)
            }
        }
    }

    private fun initTimer(msFuture: Long) {
        binding.apply {
            analyzeProgress.setMax(msFuture.toInt())
            timer = object : CountDownTimer(msFuture, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(msUntilFinished: Long) {
                    analyzeProgress.visibility = View.VISIBLE
                    analyzeTextGazerequest.text = "2초간 응시해주세요\n(눈 크기 측정 중)"
                }

                override fun onFinish() {
                    analyzeProgress.visibility = View.INVISIBLE
                    analyzeTextGazerequest.text = "얼굴을 정방향으로 유지해주세요"
                    analyzeTextGazerequest.visibility = View.INVISIBLE

                    if (standardRatioList.size > 0) {
                        standardRatioList.sort()
                        val mid = standardRatioList.slice(3 until standardRatioList.size - 3)
                        standard = mid.average()
                        showToast("설정 완료")
                    }
                }
            }
        }
    }

    /**
     * Start camera
     *
     * @param startMusic, 음악 설정에 따라 음악 정보가 달라지기 때문에 음악 설정 값을 콜백받아서 처리
     * @receiver
     */
    private fun startCamera(startMusic: () -> Unit) {
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
//                        record.add(eyeRatio)

                    if (standard == null) {
                        if (upDownAngle < 4 && upDownAngle > -4 && leftRightAngle < 4 && leftRightAngle > -4) {
                            overlay.onZeroAngle(STANDARD_IN_ANGLE)
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

                            overlay.onZeroAngle(NO_STANDARD)
                            if (isInAngleRange) {
                                isInAngleRange = false
                                stopTimer()
                                standardRatioList.clear()
                            }
                        }
                    } else {
                        val eyeState = eyeRatio / standard!! // 눈 상태
                        // 각도 임계값
                        if (leftRightAngle < -LEFT_RIGHT_ANGLE_THREDHOLD || leftRightAngle > LEFT_RIGHT_ANGLE_THREDHOLD || upDownAngle < -UP_DOWN_ANGLE_THREDHOLD || upDownAngle > UP_DOWN_ANGLE_THREDHOLD) {
                            // 카메라 정면 요청
                            binding.analyzeTextScreenrequest.visibility = View.VISIBLE
                            binding.analyzeTextDrowsycheck.visibility = View.INVISIBLE
                            overlay.onZeroAngle(OUT_OF_ANGLE)
                        } else {
                            binding.analyzeTextScreenrequest.visibility = View.INVISIBLE
                            overlay.onZeroAngle(STANDARD_IN_ANGLE)
                        }

                        // 비율이 제한을 벗어나거나 웃지 않을 때 (웃을 때 눈 웃음 때문에 눈 작아짐)
                        if (eyeState <= DROWSY_THREDHOLD && resultDetector.smilingProbability!! <= SMILE_THREDHOLD) {
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

                                        // 경고음
                                        startMusic()
                                        // 환기 요청
                                        requestRefresh()
                                        // 최단 거리에 있는 졸음 쉼터, 휴게소, 무료 주차장 안내
                                        guideToNearestRelaxZone()
                                    }
                                    binding.analyzeTextDrowsycheck.visibility = View.VISIBLE
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

    // 환기 요청
    private fun requestRefresh() {

    }

    // 최단 거리 정보 안내
    private fun guideToNearestRelaxZone() {
        requestRelaxData() //
    }

    private fun requestRelaxData() {
//        repeatRequestWhenNull(0, "ParkingLot")
        repeatRequestWhenNull(0, "Shelter")
        repeatRequestWhenNull(0, "Rest")
    }

    var job1: Job? = null
    var job2: Job? = null
    var job3: Job? = null

    private fun repeatRequestWhenNull(count: Int, detectType: String) {
        mFusedLocationProviderClient.setLastLocationEventListener {
            when (detectType) {
                "ParkingLot" -> {
                    if (job1 != null) {
                        job1!!.cancel()
                        job1 = null
                    }
                    job1 = analyzeViewModel.getALLNearParkingLot(
                        boundingBox = getBoundingBox(
                            it.latitude,
                            it.longitude,
                            DEFAULT_RADIUSKM + (5.0 * count)
                        ),
                        day = getDayType(),
                        nowTime = getCurrentTime()
                    )
                }

                "Shelter" -> {
                    if (job2 != null) {
                        job2!!.cancel()
                        job2 = null
                    }
                    job2 = analyzeViewModel.getNearShelter(
                        boundingBox = getBoundingBox(
                            it.latitude,
                            it.longitude,
                            DEFAULT_RADIUSKM + (5.0 * count)
                        )
                    )
                }

                "Rest" -> {
                    if (job3 != null) {
                        job3!!.cancel()
                        job3 = null
                    }
                    job3 = analyzeViewModel.getNearRest(
                        boundingBox = getBoundingBox(
                            it.latitude,
                            it.longitude,
                            DEFAULT_RADIUSKM + (5.0 * count)
                        )
                    )
                }

                else -> throw IllegalArgumentException("Unsupported detectType: $detectType")
            }
        }
    }

    private fun subscribeParkingLotData() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher,
            state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.parkingLots.collect {
                when (it) {
                    is ResponseState.Loading -> {
                        Log.d("whatisthis", "주차장 데이터 로딩")
                    }

                    is ResponseState.Success -> {
                        if (it.data.isNotEmpty()) {
                            requestSortingData(it.data)
                            if (job1 != null) {
                                job1!!.cancel()
                                job1 = null
                            }
                        } else {
                            Log.d("whatisthis", "데이터 재요청")
                            if (analyzeViewModel.parkingLotRequestTime < 3) {
                                repeatRequestWhenNull(
                                    analyzeViewModel.parkingLotRequestTime++,
                                    "Shelter"
                                )
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
            dispatcher = defaultDispatcher,
            state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.sortedParkingLots.collect {
                if (it.size > 0) Log.d("whatisthis", "최단 거리 주차장 " + it[0].toString())
            }
        }
    }

    private fun subscribeShleterData() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher,
            state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.shelters.collect {
                when (it) {
                    is ResponseState.Loading -> {
                        Log.d("whatisthis", "졸음 쉼터 데이터 로딩")
                    }

                    is ResponseState.Success -> {
                        if (it.data.isNotEmpty()) {
                            requestSortingData(it.data)
                            if (job2 != null) {
                                job2!!.cancel()
                                job2 = null
                            }
                        } else {
                            Log.d("whatisthis", "데이터 재요청")
                            if (analyzeViewModel.shelterRequestTime < 3) {
                                repeatRequestWhenNull(
                                    analyzeViewModel.shelterRequestTime++,
                                    "Shelter"
                                )
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
            dispatcher = defaultDispatcher,
            state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.sortedShelters.collect {
                if (it.size > 0) Log.d("whatisthis", "최단 거리 쉼터 " + it[0].toString())
            }
        }
    }

    private fun subscribeRestData() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            dispatcher = defaultDispatcher,
            state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.rests.collect {
                when (it) {
                    is ResponseState.Loading -> {
                        Log.d("whatisthis", "휴게소 데이터 로딩")
                    }

                    is ResponseState.Success -> {
                        if (it.data.isNotEmpty()) {
                            requestSortingData(it.data)
                            if (job3 != null) {
                                job3!!.cancel()
                                job3 = null
                            }
                        } else {
                            Log.d("whatisthis", "데이터 재요청")
                            if (analyzeViewModel.restRequestTime < 3) {
                                repeatRequestWhenNull(analyzeViewModel.restRequestTime++, "Shelter")
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
            dispatcher = defaultDispatcher,
            state = Lifecycle.State.STARTED
        ) {
            analyzeViewModel.sortedShelters.collect {
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
     *
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
                    if(jobSortedParkingLot!=null){
                        jobSortedParkingLot!!.cancel()
                        jobSortedParkingLot = null
                    }
                    jobSortedParkingLot = analyzeViewModel.sortParkingLots(nowLocation, parkingLots)
                }

                shelterItem::class -> {
                    val shelters = data as List<shelterItem>
                    if(jobSortedShelter!=null){
                        jobSortedShelter!!.cancel()
                        jobSortedShelter = null
                    }
                    jobSortedShelter = analyzeViewModel.sortShelters(nowLocation, shelters)
                }

                restItem::class -> {
                    val rests = data as List<restItem>
                    if(jobSortedRest!=null){
                        jobSortedRest!!.cancel()
                        jobSortedRest = null
                    }
                    jobSortedRest = analyzeViewModel.sortRests(nowLocation, rests)
                }

                else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
            }
        }
    }

    private var jobSortedParkingLot : Job ?=null
    private var jobSortedShelter : Job ?=null
    private var jobSortedRest : Job ?=null

    private fun startTimer() {
        timer.start()
    }

    private fun stopTimer() {
        timer.cancel()
        binding.analyzeProgress.visibility = View.INVISIBLE
        binding.analyzeTextGazerequest.text = "얼굴을 정방향으로 유지해주세요"
    }

}