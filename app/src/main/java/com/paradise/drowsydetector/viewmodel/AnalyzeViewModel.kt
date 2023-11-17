package com.paradise.drowsydetector.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paradise.drowsydetector.repository.RelaxRepository
import com.paradise.drowsydetector.utils.BoundingBox
import com.paradise.drowsydetector.utils.DAY
import com.paradise.drowsydetector.utils.DEFAULT_NUM_OF_ROWS
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.calculateDistance
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.ioDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.paradise.drowsydetector.data.remote.parkinglot.Item as parkingLotItem
import com.paradise.drowsydetector.data.remote.rest.Item as restItem
import com.paradise.drowsydetector.data.remote.shelter.Item as shelterItem

class AnalyzeViewModel(
    private val relaxRepository: RelaxRepository,
) : ViewModel() {

    var shelterRequestTime: Int = 0
    fun initShelterRequest() {
        shelterRequestTime = 0
    }

    var restRequestTime: Int = 0
    fun initRestRequest() {
        restRequestTime = 0
    }

    var parkingLotRequestTime: Int = 0
    fun initParkingLotRequest() {
        parkingLotRequestTime = 0
    }

    var checkDrowsy: Boolean = true
        set(value) {
            field = value
        }
        get() = field


    // 모든 휴게소 데이터
    private val _rests: MutableStateFlow<ResponseState<List<restItem>>> =
        MutableStateFlow(ResponseState.Uninitialized)
    val rests: StateFlow<ResponseState<List<restItem>>> get() = _rests.asStateFlow()
    fun getNearRest(boundingBox: BoundingBox) = viewModelScope.launch(ioDispatcher) {
        _rests.value = ResponseState.Loading
        try {
            relaxRepository.getAllRest(boundingBox).collect {
                _rests.value = it
            }
        } catch (error: Throwable) {
            _rests.value = ResponseState.Error(error)
        }
    }

    private val _sortedRests: MutableSharedFlow<List<restItem>> = MutableSharedFlow()
    val sortedRests: SharedFlow<List<restItem>> get() = _sortedRests
    fun sortRests(nowLocation: Location, rests: List<restItem>) =
        viewModelScope.launch(defaultDispatcher) {
            _sortedRests.emit(rests.sortedBy {// 정렬
                nowLocation.calculateDistance(
                    it.latitude.toDouble(), it.longitude.toDouble()
                )
            })
        }


    // 모든 쉼터 데이터
    private val _shelters: MutableStateFlow<ResponseState<List<shelterItem>>> =
        MutableStateFlow(ResponseState.Uninitialized)
    val shelters: StateFlow<ResponseState<List<shelterItem>>> get() = _shelters.asStateFlow()
    fun getNearShelter(boundingBox: BoundingBox) = viewModelScope.launch(ioDispatcher) {
        _shelters.value = ResponseState.Loading
        try {
            relaxRepository.getAllShelter(boundingBox).collect {
                _shelters.value = it
            }
        } catch (error: Throwable) {
            _shelters.value = ResponseState.Error(error)
        }
    }

    private val _sortedShelters: MutableSharedFlow<List<shelterItem>> = MutableSharedFlow()
    val sortedShelters: SharedFlow<List<shelterItem>> get() = _sortedShelters.asSharedFlow()
    fun sortShelters(nowLocation: Location, shelters: List<shelterItem>) =
        viewModelScope.launch(defaultDispatcher) {
            _sortedShelters.emit(shelters.sortedBy {// 정렬
                nowLocation.calculateDistance(
                    it.latitude.toDouble(), it.longitude.toDouble()
                )
            })
        }

    private val _parkingLots: MutableStateFlow<ResponseState<List<parkingLotItem>>> =
        MutableStateFlow(ResponseState.Uninitialized)
    val parkingLots: StateFlow<ResponseState<List<parkingLotItem>>> get() = _parkingLots.asStateFlow()


    var job: Job? = null

    private val _jobList: MutableStateFlow<Job> = MutableStateFlow(Job())
    val jobList: StateFlow<Job> get() = _jobList.asStateFlow()

    /**
     * Get a l l near parking lot
     *
     * Repository에서 먼저 총 데이터 개수를 확인하고 필요한 flow 개수만큼 생성한 뒤 List로 받는다.
     * @param boundingBox
     * @param parkingchargeInfo
     * @param numOfRows
     * @param day
     * @param nowTime
     */
    fun getALLNearParkingLot(
        boundingBox: BoundingBox,
        parkingchargeInfo: String = "무료",
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        day: DAY,
        nowTime: String,
    ) = viewModelScope.launch(defaultDispatcher) {
        _parkingLots.value = ResponseState.Loading
        try {
            relaxRepository.getAllParkingLot(
                boundingBox, parkingchargeInfo, numOfRows, day, nowTime
            ).collect {
                if (it is ResponseState.Success) {
                    combine(it.data) { responses ->
                        val combinedList =
                            responses.filterIsInstance<ResponseState.Success<List<parkingLotItem>>>()
                                .flatMap { it.data }
                        ResponseState.Success(combinedList)
                    }.catch { error ->
                        _parkingLots.value = ResponseState.Error(error)
                    }.collect { listItem ->
                        _parkingLots.value = listItem
                    }
                }
            }
        } catch (error: Throwable) {
            _parkingLots.value = ResponseState.Error(error)
        }
    }


    private val _sortedParkingLots: MutableSharedFlow<List<parkingLotItem>> = MutableSharedFlow()
    val sortedParkingLots: SharedFlow<List<parkingLotItem>> get() = _sortedParkingLots.asSharedFlow()

    /**
     * Sort parking lots
     *
     * 데이터를 정렬한 뒤 emit(statflow를 쓸 경우 데이터가 같으면 emit되지 않아서 sharedFlow를 활용함)
     * @param nowLocation
     * @param parkingLots
     */
    fun sortParkingLots(nowLocation: Location, parkingLots: List<parkingLotItem>) =
        viewModelScope.launch(defaultDispatcher) {
            _sortedParkingLots.emit(parkingLots.sortedBy {// 정렬
                nowLocation.calculateDistance(
                    it.latitude.toDouble(), it.longitude.toDouble()
                )
            })
        }

    /**
     * Get near parking lot2
     *
     * Viewmodel에서 먼저 데이터 하나를 요청해 전체 데이터 개수 정보를 확인하고, 그것을 한번에 가져오는 데이터 크기만큼 나눠 몇 번 요청해야하는지 계산한다.
     *
     * 요청 횟수를 계산하고 해당 횟수만큼 코루틴 스코프를 내려 연산을 요청한다.
     * @param boundingBox, 최대 최소 위 경도 정보
     * @param parkingchargeInfo, 주차장 요금 정보(유료, 무료, 혼합)
     * @param numOfRows, 한번에 가져오는 데이터 개수
     * @param day, 오늘이 평일인지, 토요일인지, 일요일인지
     * @param nowTime, 현재 몇시인지(운영 시간이 다르므로)
     */
//    fun getALLNearParkingLot2(
//        boundingBox: BoundingBox,
//        parkingchargeInfo: String = "무료",
//        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
//        day: DAY,
//        nowTime: String,
//    ) = viewModelScope.launch(defaultDispatcher) {
//        _parkingLots.value = ResponseState.Loading
//        try {
//            relaxRepository.getOneParkingLot(
//                boundingBox, parkingchargeInfo, numOfRows, day, nowTime
//            ).collect { response ->
//                if (response.isSuccessful) {
//                    response.body()?.let {
//                        val totalCount = it.response.body.totalCount.toInt()
//                        var numOfCoroutineRequired = totalCount / numOfRows
//                        if (totalCount % numOfRows != 0) numOfCoroutineRequired++
//                        val ListOfFlow = (1..numOfCoroutineRequired).map {
//                            relaxRepository.getParkingLot1(
//                                it, boundingBox, parkingchargeInfo, day, nowTime
//                            )
//                        }
//                        combine(
//                            ListOfFlow
//                        ) { responses ->
//                            val combinedList =
//                                responses.filterIsInstance<ResponseState.Success<List<parkingLotItem>>>()
//                                    .flatMap { it.data }
//                            ResponseState.Success(combinedList)
//                        }.catch { error ->
//                            _parkingLots.value = ResponseState.Error(error)
//                        }.collect { listItem ->
//                            _parkingLots.value = listItem
//                        }
//                    }
//                } else {
//                    _parkingLots.value = (ResponseState.Fail(response.code(), response.message()))
//                }
//            }
//        } catch (error: Throwable) {
//            _parkingLots.value = ResponseState.Error(error)
//        }
//    }

    /**
     * Get near parking lot
     *
     * 정해진 데이터 개수(DEFAULT_NUM_OF_ROWS)만큼 몇 개의 코루틴 스코프를 내릴지 정한 뒤 데이터를 요청한다.
     * @param boundingBox, 최대 최소 위 경도 정보
     * @param parkingchargeInfo, 주차장 요금 정보(유료, 무료, 혼합)
     * @param numOfCoroutineRequired, 코루틴 스코프 개수
     * @param day
     * @param nowTime
     */
//    fun getPartNearParkingLot(
//        boundingBox: BoundingBox,
//        parkingchargeInfo: String = "무료",
//        numOfCoroutineRequired: Int = 10,
//        day: DAY,
//        nowTime: String,
//    ) = viewModelScope.launch(ioDispatcher) {
//        combine(
//            relaxRepository.getParkingLots(
//                boundingBox, parkingchargeInfo, numOfCoroutineRequired, day, nowTime
//            )
//        ) { responses ->
//            val combinedList =
//                responses.filterIsInstance<ResponseState.Success<List<parkingLotItem>>>()
//                    .flatMap { it.data }
//            ResponseState.Success(combinedList)
//        }.catch { error ->
//            _parkingLots.value = ResponseState.Error(error)
//        }.collect {
//            _parkingLots.value = it
//        }
//    }

    class AnalyzeViewModelFactory(private val relaxRepository: RelaxRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(AnalyzeViewModel::class.java)) {
                AnalyzeViewModel(relaxRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}