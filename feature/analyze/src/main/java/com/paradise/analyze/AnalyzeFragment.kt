package com.paradise.analyze

import android.app.ActivityManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.core.model.musicItem
import com.core.model.parkingLotItem
import com.core.model.restItem
import com.core.model.shelterItem
import com.paradise.analyze.databinding.FragmentAnalyzeBinding
import com.paradise.common.helper.CameraHelper
import com.paradise.common.helper.LocationHelper
import com.paradise.common.helper.MusicHelper
import com.paradise.common.helper.SttTtsController
import com.paradise.common.helper.SttTtsService
import com.paradise.common.helper.ToastHelper
import com.paradise.common.helper.VolumeHelper
import com.paradise.common.network.BASICMUSICMODE
import com.paradise.common.network.CHECKUSESTTSERVICE
import com.paradise.common.network.DROWSY_THREDHOLD
import com.paradise.common.network.GUIDEMODE
import com.paradise.common.network.NO_STANDARD
import com.paradise.common.network.OUT_OF_ANGLE
import com.paradise.common.network.SMILE_THREDHOLD
import com.paradise.common.network.STANDARD_IN_ANGLE
import com.paradise.common.network.STT_THREDHOLD
import com.paradise.common.network.TIME_THREDHOLD
import com.paradise.common.network.calculateDistance
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.getCurrentTime
import com.paradise.common.network.getDayType
import com.paradise.common.result.UiState
import com.paradise.common.utils.DEFAULT_RADIUSKM
import com.paradise.common.utils.getBoundingBox
import com.paradise.common.utils.launchWithRepeatOnLifecycle
import com.paradise.common_ui.base.BaseFragment
import com.paradise.common_ui.designsystem.PreviewOverlayView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@WithFragmentBindings
@AndroidEntryPoint
class AnalyzeFragment : BaseFragment<FragmentAnalyzeBinding>(FragmentAnalyzeBinding::inflate),
    SttTtsService {
    @Inject
    lateinit var musicHelper: MusicHelper

    @Inject
    lateinit var locationHelper: LocationHelper

    @Inject
    lateinit var sttTtsController: SttTtsController

    @Inject
    lateinit var volumeHelper: VolumeHelper

    @Inject
    lateinit var cameraHelper: CameraHelper

    @Inject
    lateinit var toastHelper: ToastHelper

    private val analyzeViewModel: AnalyzeViewModel by viewModels()
    private var analyzeService: AnalyzeService? = null

    lateinit var overlay: PreviewOverlayView

    private var isServiceBounded = false
    private var isInAngleRange = false
    private var isInDrowsyState = false
    private var timeCheckDrowsy: Long? = null

    private var subscribeJobList = mutableListOf<Job>()
    private var jobSortedAll: Job? = null
    private var jobSortedParkingLot: Job? = null
    private var jobSortedShelter: Job? = null
    private var jobSortedRest: Job? = null
    private var jobGetParkingLot: Job? = null
    private var jobGetShelter: Job? = null
    private var jobGetRest: Job? = null

    private val standardRatioList = mutableListOf<Double>()
    private var musicList = mutableListOf<musicItem>()
    private var timerJob: Job? = null // 기본값 설정 타이머

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("whatisthis", "onServiceConnected")
            val binder = service as AnalyzeService.MyBinder
            analyzeService = binder.getService()
            isServiceBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("whatisthis", "onServiceDisconnected")
            analyzeService = null
            isServiceBounded = false
        }
    }

    override fun onViewCreated() { // 홈 버튼에서 돌아올 때는 실행안됨
        // 툴바 세팅
        with(binding) {
            analyzeToolbar.setToolbarMenu("졸음 감지", true).inflateResetMenu {
                analyzeService?.standard = null
                analyzeService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                analyzeTextGazerequest.visibility = View.VISIBLE
            }
            musicHelper.initMusicHelper()
            volumeHelper.initVolumeHelper()
            sttTtsController.initSttTtsController()
            cameraHelper.initCameraHelper()
            overlay = binding.analyzeOverlay

            subscribeSortResult()
            subscribeAllSetting()
            subscribeUserMusicList()
            subscribeMusicVolume()
        }
    }

    fun subscribeMusicVolume() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(Lifecycle.State.STARTED) {
            analyzeViewModel.musicVolume.collect { myvolume ->
                volumeHelper.setVolume(myvolume)
            }
        }

    override fun onStart() {
        super.onStart()
        Log.d("whatisthis", "onStart")
        // bindService, 서비스 등록
        analyzeService?.stopForegroundInBackground() // Notification을 눌렀을 경우
        Intent(requireContext(), AnalyzeService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        overlay = binding.analyzeOverlay
        analyzeViewModel.getAllSetting()
        analyzeViewModel.getAllMusic()
    }

    override fun onStop() {
        Log.d("whatisthis", "onStop")
        // fragment의 previewView 동작을 멈추기 위함
        cameraHelper.stopCameraHelper()
        // 재생 중인 음악을 멈춤
        musicHelper.releaseMediaPlayer()

        stopTimer()
        // subscribe 했던 job 전부 해제, 휴식 공간 요청 job 전부 해제
        CoroutineScope(defaultDispatcher).launch {
            val jobListCopy = mutableListOf<Job>()
            jobListCopy.addAll(jobList)
            for (i in jobListCopy) {
                if (i.isCancelled) continue // 종료된거면 종료 X
                i.cancelAndJoin() // 진행이 끝났을 때 종료
            }
            for (i in subscribeJobList) {
                if (i.isCancelled) continue // 종료된거면 종료 X
                i.cancelAndJoin() // 진행이 끝났을 때 종료
            }
            jobList.clear()
            subscribeJobList.clear()
            Log.d("whatisthis", " job 취소 완료")
        }
        // 만약 bind되어 있다면 -> standard 설정이 되어 있다면 -> 분석을 시작했다면
        if (isAppInBackground() && analyzeService?.standard != null) {
            analyzeService?.startForegroundInBackground()
            requireContext().unbindService(connection) // bind 해제
            isServiceBounded = false
        }

        super.onStop()
    }

    override fun onDestroyViewInFragMent() { // 홈 버튼 누를 땐 동작 X
        Log.d("whatisthis", "onDestroyViewInFragMent()")
        sortResult.removeObservers(viewLifecycleOwner)
        sttTtsController.releaseSttTtsController()
        // 서비스를 종료한다.
        requireContext().unbindService(connection) // bind 해제
        val intent = Intent(requireContext(), AnalyzeService::class.java)
        requireContext().stopService(intent)
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
    private fun subscribeAllSetting() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            analyzeViewModel.allSettings.collect {
                for (i in subscribeJobList) {
                    if (i.isCancelled) continue // 종료된거면 종료 X
                    i.cancelAndJoin() // 진행이 끝났을 때 종료
                }
                subscribeJobList.clear()

                if (it.first.size > 0 && it.second.size > 0) {
                    val defaultMusic = it.first[1]
                    val onGuide = it.first[0]
                    val onRefresh = it.second[1]
                    Log.d("whatisthis", it.toString())

                    if (onGuide) {
                        // 내 위치를 중심으로 근처에 있는 데이터 관찰
                        val subjob1 = subscribeParkingLotData()
                        subscribeJobList.add(subjob1)
                        val subjob2 = subscribeShleterData()
                        subscribeJobList.add(subjob2)
                        val subjob3 = subscribeRestData()
                        subscribeJobList.add(subjob3)

                        val subjob4 = subscribeSortedAll()
                        subscribeJobList.add(subjob4)
                    }

                    Log.d("whatisthis", "subscribe is On, startCamera")
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
        musicHelper.releaseMediaPlayer()
        if (sttTtsController.checkTtsSttHelperReady()) { // 안내가 안나올 때
            if (defaultMode) musicHelper.setResMusic()
            else {
                if (musicList.size > 0) musicHelper.setMyMusic(musicList)
            }
        }
    }

    /**
     * Subscribe user music list
     *
     * 사용자 음악을 사용할 경우, Room에 저장된 리스트를 관찰한다.
     */
    private fun subscribeUserMusicList() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(state = Lifecycle.State.STARTED) {
            analyzeViewModel.music.collect { musicList ->
                if (musicList.isNotEmpty()) this.musicList.addAll(musicList)
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
        cameraHelper.startAnalyze(binding.analyzeViewFinder) { resultDetector, resultMesh ->
            if (this@AnalyzeFragment.isAdded) {
                val upDownAngle = resultDetector.headEulerAngleX
                val leftRightAngle = resultDetector.headEulerAngleY

                val eyeRatio = cameraHelper.calRatio(upDownAngle, leftRightAngle, resultMesh)
                if (analyzeService?.standard == null) {
                    checkHeadPoseInNoStandard(eyeRatio, leftRightAngle, upDownAngle)
                } else {
                    analyzeService?.startRecording()
                    val eyeState = eyeRatio / analyzeService?.standard!! // 눈 상태
                    checkHeadPoseInStandard(leftRightAngle, upDownAngle)
                    // 비율이 제한을 벗어났을 때
                    if (eyeState <= DROWSY_THREDHOLD) {
                        // 눈 깜빡임 카운팅
                        analyzeService?.checkEyeWink(eyeState, resultDetector)

                        // 웃지 않을 때 (웃을 때 눈 웃음 때문에 눈 작아짐), 음성 안내가 안나올 때, 음악이 안나올 때
                        if (resultDetector.smilingProbability!! <= SMILE_THREDHOLD && !(musicHelper.isPrepared?.value)!! && sttTtsController.checkTtsSttHelperReady()!!) {
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
                                        analyzeService?.countUpDrowsyCount()
                                        // 경고음
                                        startMusic()
                                        // 최단 거리에 있는 졸음 쉼터, 휴게소, 무료 주차장 안내
                                        startGuide()
                                    }
                                    binding.analyzeTextDrowsycheck.visibility = View.VISIBLE
                                }
                            }
                        }
                    } else if (eyeState >= STT_THREDHOLD) {
                        sttTtsController.request.value = CHECKUSESTTSERVICE
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
        if (cameraHelper.checkHeadAngleInNoStandard(upDownAngle, leftRightAngle)) {
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
        if (cameraHelper.isInLeftRight(leftRightAngle)) {
            if (upDownAngle > 4) binding.analyzeTextGazerequest.text = "고개를 살짝만 내려 주세요"
            else if (upDownAngle < -4) binding.analyzeTextGazerequest.text = "고개를 살짝만 들어 주세요"
        } else {
            binding.analyzeTextGazerequest.text = "고개를 살짝만 돌려 주세요"
        }
    }

    private fun checkHeadPoseInStandard(leftRightAngle: Float, upDownAngle: Float) {// 각도 임계값
        if (cameraHelper.checkHeadAngleInStandard(leftRightAngle, upDownAngle)) {
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
        analyzeService?.eyeClosed = false
        if (isInDrowsyState) {
            analyzeViewModel.checkDrowsy = true
            binding.analyzeTextDrowsycheck.visibility = View.INVISIBLE
            isInDrowsyState = false
            timeCheckDrowsy = null
        }
    }

    // 환기 요청
    private fun startRefresh(a: Int) {}

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
            requestSortedAll()
        }
    }

    private fun requestSortedAll() {
        if (jobSortedAll == null) {
            Log.d("whatisthis", "모든 정렬 정보 요청")
            jobSortedAll = analyzeViewModel.sortAll()
            jobList.add(jobSortedAll!!)
        } else {
            if (jobSortedAll!!.isActive && !jobSortedAll!!.isCompleted) {
                val subscribeJob = viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                    Log.d("whatisthis", "모든 정렬 정보 이미 요청됨**** ")
                    jobSortedAll!!.join()
                    jobList.remove(jobSortedAll)
                }
                subscribeJobList.add(subscribeJob)
            } else {
                val subscribeJob = viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                    jobList.remove(jobSortedAll!!)
                    jobSortedAll!!.cancelAndJoin()
                    Log.d("whatisthis", "모든 정렬 정보 재요청")
                    jobSortedAll = analyzeViewModel.sortAll()
                    jobList.add(jobSortedAll!!)
                }
                subscribeJobList.add(subscribeJob)
            }
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
            handleFirstRequest(detectType, count)
        } else {
            if (job.isActive && !job.isCompleted) {
                handleRequestCompletion(job, detectType)
            } else {
                handleRequestCancellation(job, detectType, count)
            }
        }
    }

    private fun handleFirstRequest(detectType: String, count: Int) {
        Log.d("whatisthis", "$detectType 정보 첫요청1")
        locationHelper.setLastLocationEventListener { location ->
            getJobForType(detectType, count, location)
        }
    }

    private fun handleRequestCompletion(job: Job, detectType: String) {
        val subscribeJob = viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
            Log.d("whatisthis", "$detectType 정보 이미 요청됨**** ")
            job.join()
            jobList.remove(job)
        }
        subscribeJobList.add(subscribeJob)
    }

    private fun handleRequestCancellation(job: Job, detectType: String, count: Int) {
        val subscribeJob = viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
            jobList.remove(job)
            job.cancelAndJoin()
            Log.d("whatisthis", "$detectType 정보 요청 재요청!!!! ${count}회 요청 중")
            locationHelper.setLastLocationEventListener { location ->
                getJobForType(detectType, count, location)
            }
        }
        subscribeJobList.add(subscribeJob)
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
                Log.d("whatisthis", (DEFAULT_RADIUSKM + (5.0 * count)).toString())
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

            else -> {}
        }
    }

    private val sortResult = MutableLiveData<String>("")

    private fun subscribeSortResult() {
        sortResult.observe(viewLifecycleOwner) {
            musicHelper.releaseMediaPlayer()
            Log.d("whatisthis", "okay")
            sttTtsController.speakOutTtsHelper(it)
        }
    }

    private fun subscribeSortedAll() = viewLifecycleOwner.launchWithRepeatOnLifecycle(
        dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
    ) {
        analyzeViewModel.sortedAll.collect { items ->
            var result = ""
            locationHelper.setLastLocationEventListener { nowLocation ->
                Log.d("whatisthis", nowLocation.toString())
                if (items.first.isNotEmpty()) {
                    val nearestRest = items.first[0]
                    val dist = (nowLocation.calculateDistance(
                        nearestRest.latitude!!.toDouble(), nearestRest.longitude!!.toDouble()
                    ) / 1000.0).toInt()
                    result += "가장 가까운 휴게소는 근방 ${dist}km에 " + nearestRest.roadRouteNm + " " + nearestRest.roadRouteDrc + "입니다. "
                }
                if (items.second.isNotEmpty()) {
                    val nearestShelter = items.second[0]
                    val dist = (nowLocation.calculateDistance(
                        nearestShelter.latitude!!.toDouble(), nearestShelter.longitude!!.toDouble()
                    ) / 1000.0).toInt()
                    result += ("가장 가까운 쉼터는 근방 ${dist}km에 " + nearestShelter.roadRouteDrc + "입니다. ")
                }
                if (items.third.isNotEmpty()) {
                    val nearestParkingLot = items.third[0]
                    val dist = (nowLocation.calculateDistance(
                        nearestParkingLot.latitude!!.toDouble(),
                        nearestParkingLot.longitude!!.toDouble()
                    ) / 1000.0).toInt()
                    result += ("가장 가까운 주차장은 근방 ${dist}km에 " + nearestParkingLot.lnmadr + "입니다. ")
                }
                sortResult.value = result
            }
        }
    }

    private fun subscribeParkingLotData() = viewLifecycleOwner.launchWithRepeatOnLifecycle(
        dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
    ) {
        analyzeViewModel.parkingLots.collect {
            when (it) {
                is UiState.Loading -> {
                    Log.d("whatisthis", "주차장 데이터 로딩")
                }

                is UiState.Success -> {
                    checkBackPress {
                        if (it.data.isNotEmpty()) {
                            Log.d("whatisthis", it.data.toString())
                            requestSortingData(it.data)
                        } else {
                            Log.d("whatisthis", "근처에 주차장이 없음 반경을 늘려서 재요청")
                            if (analyzeViewModel.parkingLotRequestTime < 3) {
                                repeatRequestWhenNull(
                                    analyzeViewModel.parkingLotRequestTime++, "Shelter"
                                )
                            } else {
                                requestSortingData(it.data)
                            }
                        }
                    }
                }

                is UiState.Fail -> {
                    Log.d("whatisthis", it.message.toString())
                }

                is UiState.Error -> {
                    Log.d("whatisthis", it.exception.toString())
                }
            }
        }
    }

    private fun subscribeShleterData() = viewLifecycleOwner.launchWithRepeatOnLifecycle(
        dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
    ) {
        analyzeViewModel.shelters.collect {
            when (it) {
                is UiState.Loading -> {
                    Log.d("whatisthis", "졸음 쉼터 데이터 로딩")
                }

                is UiState.Success -> {
                    checkBackPress {
                        if (it.data.isNotEmpty()) {
                            requestSortingData(it.data)
                        } else {
                            Log.d("whatisthis", "근처에 쉼터가 없음 반경을 늘려서 재요청")
                            if (analyzeViewModel.shelterRequestTime < 3) {
                                repeatRequestWhenNull(
                                    analyzeViewModel.shelterRequestTime++, "Shelter"
                                )
                            } else {
                                requestSortingData(it.data)
                            }
                        }
                    }
                }

                is UiState.Fail -> {
                    Log.d("whatisthis", it.message.toString())
                }

                is UiState.Error -> {
                    Log.d("whatisthis", it.exception.toString())
                }
            }
        }
    }

    private fun subscribeRestData() = viewLifecycleOwner.launchWithRepeatOnLifecycle(
        dispatcher = defaultDispatcher, state = Lifecycle.State.STARTED
    ) {
        analyzeViewModel.rests.collect {
            when (it) {
                is UiState.Loading -> {
                    Log.d("whatisthis", "휴게소 데이터 로딩")
                }

                is UiState.Success -> {
                    checkBackPress {
                        if (it.data.isNotEmpty()) {
                            requestSortingData(it.data)
                        } else {
                            Log.d("whatisthis", "근처에 휴게소가 없음 반경을 늘려서 재요청")
                            if (analyzeViewModel.restRequestTime < 3) {
                                repeatRequestWhenNull(
                                    analyzeViewModel.restRequestTime++, "Shelter"
                                )
                            } else {
                                requestSortingData(it.data)
                            }
                        }
                    }
                }

                is UiState.Fail -> {
                    Log.d("whatisthis", it.message.toString())
                }

                is UiState.Error -> {
                    Log.d("whatisthis", it.exception.toString())
                }
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
     *
     * @param T
     * @param data
     */
    private inline fun <reified T> requestSortingData(data: List<T>) {
        locationHelper.setLastLocationEventListener { nowLocation ->
            when (T::class) {
                parkingLotItem::class -> {
                    jobSortedParkingLot = handleDataSorting(jobSortedParkingLot) {
                        analyzeViewModel.sortParkingLots(
                            nowLocation, data as List<parkingLotItem>
                        )
                    }
                }

                shelterItem::class -> {
                    jobSortedShelter = handleDataSorting(jobSortedShelter) {
                        analyzeViewModel.sortShelters(
                            nowLocation, data as List<shelterItem>
                        )
                    }
                }

                restItem::class -> {
                    jobSortedRest = handleDataSorting(jobSortedRest) {
                        analyzeViewModel.sortRests(nowLocation, data as List<restItem>)
                    }
                }

                else -> return@setLastLocationEventListener
            }
        }
    }

    private fun handleDataSorting(job: Job?, sorting: () -> Job): Job? {
        var temp: Job? = null
        if (job == null) {
            temp = sorting()
            jobList.add(temp)
        } else {
            if (job.isActive && !job.isCompleted) {
                val subscribeJob = viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                    job.join()
                    temp = job
                    jobList.remove(job)
                }
                subscribeJobList.add(subscribeJob)
            } else {
                val subscribeJob = viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                    job.cancelAndJoin()
                    jobList.remove(job)
                    temp = sorting()
                    jobList.add(temp!!)
                }
                subscribeJobList.add(subscribeJob)
            }
        }
        return temp
    }

    private fun initTimer(msFuture: Long) {
        binding.apply {
            analyzeProgress.max = msFuture.toInt()
        }
    }

    private fun startTimer() {
        timerJob = viewLifecycleOwner.lifecycleScope.launch() {
            with(binding) {
                analyzeProgress.visibility = android.view.View.VISIBLE
                analyzeTextGazerequest.text = "2초간 응시해주세요\n(눈 크기 측정 중)"
                delay(analyzeProgress.max.toLong())

                analyzeProgress.visibility = android.view.View.INVISIBLE
                analyzeTextGazerequest.text = "얼굴을 정방향으로 유지해주세요"
                analyzeTextGazerequest.visibility = android.view.View.INVISIBLE

                if (standardRatioList.size > 0) {
                    standardRatioList.sort()
                    val mid = standardRatioList.slice(3 until standardRatioList.size - 3)
                    val avg = mid.average()
                    android.util.Log.d("whatisthis", "standard : $avg")
                    if (!avg.isNaN()) {
                        musicHelper.setStandardMusic()
                        analyzeService?.standard = avg
                        // startForegroundService 서비스 실행
                        Intent(requireContext(), AnalyzeService::class.java).also { intent ->
                            requireContext().startForegroundService(intent)
                        }

                        toastHelper.showToast("설정 완료")
                        Log.d("whatisthis", analyzeService?.currentAnayzeResult.toString())
                        analyzeService?.insertRecord(analyzeService?.currentAnayzeResult!!)
                    } else {
                        toastHelper.showToast("다시 설정해 주세요")
                        analyzeTextGazerequest.visibility = android.view.View.VISIBLE
                    }
                    standardRatioList.clear()
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
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

    override fun baseMusic() {
        analyzeViewModel.setSettingMode(BASICMUSICMODE, true)
    }

    override fun userMusic() {
        analyzeViewModel.setSettingMode(BASICMUSICMODE, false)
    }

    override fun guideOff() {
        analyzeViewModel.setSettingMode(GUIDEMODE, false)
    }

    override fun guideOn() {
        analyzeViewModel.setSettingMode(GUIDEMODE, true)
    }

    override fun relaxData() {
        requestRelaxData()
    }

    override fun recentRelaxData() {
        if (sortResult.value != null) {
            sttTtsController.speakOutTtsHelper(sortResult.value!!)
        }
    }

    override fun cancleAnalyze() {
        requireActivity().finish()
    }
}
