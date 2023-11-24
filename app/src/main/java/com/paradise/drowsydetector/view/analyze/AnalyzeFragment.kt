package com.paradise.drowsydetector.view.analyze

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import com.paradise.drowsydetector.databinding.FragmentAnalyzeBinding
import com.paradise.drowsydetector.service.AnalyzeService
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
import com.paradise.drowsydetector.data.remote.parkinglot.Item as parkingLotItem
import com.paradise.drowsydetector.data.remote.rest.Item as restItem
import com.paradise.drowsydetector.data.remote.shelter.Item as shelterItem

class AnalyzeFragment :
    BaseViewbindingFragment<FragmentAnalyzeBinding>(FragmentAnalyzeBinding::inflate) {
    private val analyzeViewModel: AnalyzeViewModel by viewModels {
        AnalyzeViewModel.AnalyzeViewModelFactory(getApplicationContext().relaxRepository)
    }
    private val settingViewModel: SettingViewModel by activityViewModels()
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val staticsViewModel: StaticsViewModel by activityViewModels()

    private val mFusedLocationProviderClient by lazy {
        LocationHelper.getInstance(
            getApplicationContext().fusedLocationProviderClient, getApplicationContext().geocoder
        )
    }
    private var musicHelper: MusicHelper? = null
    private lateinit var overlay: OvalOverlayView

    private var isInAngleRange = false
    private var isInDrowsyState = false
    private var timeCheckDrowsy: Long? = null
    private val standardRatioList = mutableListOf<Double>()
    private var eyeClosed: Boolean = false
    private var musicList = mutableListOf<Music>()

    private var timerJob: Job? = null // 기본값 설정 타이머
    private var jobSortedParkingLot: Job? = null
    private var jobSortedShelter: Job? = null
    private var jobSortedRest: Job? = null
    private var jobGetParkingLot: Job? = null
    private var jobGetShelter: Job? = null
    private var jobGetRest: Job? = null
    private var myService: AnalyzeService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AnalyzeService.MyBinder
            myService = binder.getService()
            myService?.subscribeRecord()
            myService?.getRecord(getTodayDate())
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            overlay = binding.analyzeOverlay
            musicHelper = MusicHelper.getInstance(requireContext(), viewLifecycleOwner)

            Intent(activity, AnalyzeService::class.java).also { intent ->
                activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
            Log.d("whatisthis", myService?.standard.toString())

            subscribeAllSetting()
            settingViewModel.getAllSetting()
            subscribeUserMusicList()
            musicViewModel.getAllMusic()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isAppInBackground() && isBound && myService?.standard != null) { // 홈버튼을 눌렀고, service가 bind 되어 있느냐
            sendCommandToService("")
            requireActivity().unbindService(connection)
            stopTimer()
            jobList.forEach { it.cancel() }
            isBound = false
        }
    }

    override fun onDestroyViewInFragMent() {
        musicHelper?.clearContext()
        musicHelper = null
        stopTimer()
        jobList.forEach { it.cancel() }
    }

    override fun onViewCreated() { // 홈 버튼에서 돌아올 때는 실행안됨
        // 툴바 세팅
        binding.analyzeToolbar.setToolbarMenu("졸음 감지", true).inflateResetMenu {
            binding.analyzeTextGazerequest.visibility = View.VISIBLE
        }
        // statdard는 처음에 한번 초기화
        myService?.standard = null
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
                if (it.first.size > 0 && it.second.size > 0) {
                    val defaultMusic = it.first[1]
                    val onGuide = it.first[0]
                    val onRefresh = it.second[1]

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

                    if (isBound) {
                        startCamera(startMusic = { startMusic(defaultMusic) },
                            startGuide = { startGuide(onGuide) },
                            startRefresh = { startRefresh(onRefresh) })
                        initTimer(2000)
                    }
                }
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
            musicHelper?.setResMusic()
        } else {
            if (musicList.size > 0) musicHelper?.setMyMusic(musicList)
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
                if (musicList.isNotEmpty()) this.musicList.addAll(musicList)
            }
        }
    }

    private fun initJob() =
        viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher, CoroutineStart.LAZY) {
            while (this.isActive) {
                delay(1 * 60 * 1000)  // 30분 대기
                Log.d(
                    "whatisthis",
                    "Winkcount : " + staticsViewModel.currentAnayzeResult!!.id.toString() + " DrowsyCount :" + staticsViewModel.currentWinkCount + " " + staticsViewModel.currentDrowsyCount
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
        myService?.startAnalyze(binding.analyzeViewFinder) { resultDetector, resultMesh ->
            if (this@AnalyzeFragment.isAdded) {
                val upDownAngle = resultDetector.headEulerAngleX
                val leftRightAngle = resultDetector.headEulerAngleY

                val eyeRatio = calRatio(upDownAngle, leftRightAngle, resultMesh)

                if (myService?.standard == null) {
                    checkHeadPoseInNoStandard(eyeRatio, leftRightAngle, upDownAngle)
                } else {
                    myService!!.startRecording()
                    val eyeState = eyeRatio / myService?.standard!! // 눈 상태
                    checkHeadPoseInStandard(leftRightAngle, upDownAngle)

                    // 비율이 제한을 벗어났을 때
                    if (eyeState <= DROWSY_THREDHOLD) {
                        // 눈 깜빡임 카운팅
                        myService?.checkEyeWink(eyeState, resultDetector)

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
        overlay.onZeroAngle(NO_STANDARD)
        if (isInAngleRange) {
            isInAngleRange = false
            stopTimer()
            standardRatioList.clear()
        }
        if (isInLeftRight(leftRightAngle)) {
            if (upDownAngle > 4) binding.analyzeTextGazerequest.text = "고개를 살짝만 내려 주세요"
            else if (upDownAngle < -4) binding.analyzeTextGazerequest.text = "고개를 살짝만 들어 주세요"
        } else {
            binding.analyzeTextGazerequest.text = "고개를 살짝만 돌려 주세요"
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

        if (job == null) {
            Log.d("whatisthis", "33")
            handleFirstRequest(detectType, count)
        } else {
            if (job.isActive && !job.isCompleted) {
                Log.d("whatisthis", "11")
                handleRequestCompletion(job, detectType)
            } else {
                Log.d("whatisthis", "22")
                handleRequestCancellation(job, detectType, count)
            }
        }

        Log.d("whatisthis", jobList.size.toString())
    }

    private fun handleRequestCompletion(job: Job, detectType: String) {
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
                getJobForType(detectType, count, location)
            }
        }
    }

    private fun handleFirstRequest(detectType: String, count: Int) {
        Log.d("whatisthis", "$detectType 정보 첫요청1")
        mFusedLocationProviderClient.setLastLocationEventListener { location ->
            getJobForType(detectType, count, location)
        }
    }

    private fun getJobForType(detectType: String, count: Int, location: Location) {
        when (detectType) {
            "ParkingLot" -> {
                jobGetParkingLot = analyzeViewModel.getALLNearParkingLot(
                    boundingBox = getBoundingBox(
                        location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
                    ), day = getDayType(), nowTime = getCurrentTime()
                )
                jobList.add(jobGetParkingLot!!)
            }

            "Shelter" -> {
                jobGetShelter = analyzeViewModel.getNearShelter(
                    boundingBox = getBoundingBox(
                        location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
                    )
                )
                jobList.add(jobGetShelter!!)
            }

            "Rest" -> {
                jobGetRest = analyzeViewModel.getNearRest(
                    boundingBox = getBoundingBox(
                        location.latitude, location.longitude, DEFAULT_RADIUSKM + (5.0 * count)
                    )
                )
                jobList.add(jobGetRest!!)
            }

            else -> {
                null
            }
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
            analyzeProgress.max = msFuture.toInt()
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
                Log.d("whatisthis", "standard : " + mid.average().toString())
                myService?.standard = mid.average()
                staticsViewModel.insertRecord(myService?.currentAnayzeResult!!)
                showToast("설정 완료")
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

    private fun isAppInBackground(): Boolean {
        val am = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.processName == requireContext().packageName) {
                return processInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
        return true
    }

    private fun sendCommandToService(action: String) {
        val intent = Intent(requireContext(), AnalyzeService::class.java)
        requireContext().startForegroundService(intent)
    }
}


//        cameraHelper?.startCamera(binding.analyzeViewFinder) { resultDetector, resultMesh ->
//            if (this@AnalyzeFragment.isAdded) {
//                val upDownAngle = resultDetector.headEulerAngleX
//                val leftRightAngle = resultDetector.headEulerAngleY
//
//                val eyeRatio = calRatio(upDownAngle, leftRightAngle, resultMesh)
//
//                if (analyzeViewModel.standard == null) {
//                    checkHeadPoseInNoStandard(eyeRatio, leftRightAngle, upDownAngle)
//                } else {
//                    val eyeState = eyeRatio / analyzeViewModel.standard!! // 눈 상태
//                    checkHeadPoseInStandard(leftRightAngle, upDownAngle)
//
//                    // 비율이 제한을 벗어났을 때
//                    if (eyeState <= DROWSY_THREDHOLD) {
//
//                        // 눈 깜빡임 카운팅
//                        checkEyeWink(eyeState, resultDetector)
//
//                        // 웃지 않을 때 (웃을 때 눈 웃음 때문에 눈 작아짐)
//                        if (resultDetector.smilingProbability!! <= SMILE_THREDHOLD) {
//                            if (!isInDrowsyState) {
//                                isInDrowsyState = true
//                                timeCheckDrowsy = Date().time
//                            }
//
//                            if (timeCheckDrowsy != null) {
//                                val maintainTime = Date().time - timeCheckDrowsy!!
//
//                                // 졸음 감지!!!!!!!!!!!!!!!!!!!!!!!!!!
//                                if (maintainTime > TIME_THREDHOLD) {
//                                    if (analyzeViewModel.checkDrowsy) { // 한번만 동작하게
//                                        analyzeViewModel.checkDrowsy = false
//                                        staticsViewModel.currentDrowsyCount++
//                                        // 경고음
//                                        startMusic()
//                                        // 최단 거리에 있는 졸음 쉼터, 휴게소, 무료 주차장 안내
//                                        startGuide()
//                                        // 환기 요청
//                                        startRefresh()
//                                    }
//                                    binding.analyzeTextDrowsycheck.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                    } else {
//                        setEyeStateInOpen()
//                    }
//                }
//            }
//        }