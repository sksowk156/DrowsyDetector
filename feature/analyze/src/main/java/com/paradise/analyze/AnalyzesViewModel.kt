package com.paradise.analyze

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.model.BoundingBox
import com.core.model.musicItem
import com.core.model.parkingLotItem
import com.core.model.restItem
import com.core.model.shelterItem
import com.paradise.common.network.BASICMUSICMODE
import com.paradise.common.network.DAY
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.common.network.GUIDEMODE
import com.paradise.common.network.MUSICVOLUME
import com.paradise.common.network.REFRESHTERM
import com.paradise.common.network.calculateDistance
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.defaultScope
import com.paradise.common.network.ioDispatcher
import com.paradise.common.result.UiState
import com.paradise.data.repository.MusicRepository
import com.paradise.data.repository.RestRepository
import com.paradise.data.repository.SettingRepository
import com.paradise.data.repository.ShelterRepository
import com.paradise.domain.usecases.GetParkingLostItemListUseCase
import com.paradise.domain.usecases.GetSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.replay
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyzesViewModel @Inject constructor(
    private val getParkingLostItemListUseCase: GetParkingLostItemListUseCase,
    private val shelterRepository: ShelterRepository,
    private val restRepository: RestRepository,
    private val musicRepository: MusicRepository,
    private val settingRepository: SettingRepository,
    private val getSettingUseCase: GetSettingUseCase,
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


    private val _sortedAll: MutableSharedFlow<Triple<List<restItem>, List<shelterItem>, List<parkingLotItem>>> =
        MutableSharedFlow(replay = 0)
    val sortedAll: SharedFlow<Triple<List<restItem>, List<shelterItem>, List<parkingLotItem>>> get() = _sortedAll

    fun sortAll() = viewModelScope.launch(defaultDispatcher) {
        sortedRests.zip(sortedShelters) { rest, shelter -> Pair(rest, shelter) }
            .zip(sortedParkingLots) { pair, parkinglot ->
                Triple(
                    pair.first, pair.second, parkinglot
                )
            }.take(1)
            .collect {
                _sortedAll.emit(it)
            }
    }


    // 모든 휴게소 데이터
    private val _rests: MutableStateFlow<UiState<List<restItem>>> =
        MutableStateFlow(UiState.Uninitialized)
    val rests: StateFlow<UiState<List<restItem>>> get() = _rests.asStateFlow()
    fun getNearRest(boundingBox: BoundingBox) = viewModelScope.launch(ioDispatcher) {
        _rests.value = UiState.Loading
        try {
            restRepository.getAllRest(boundingBox).collect {
                if (it.isNotEmpty()) _rests.value = UiState.Success(it)
                else _rests.value = UiState.Fail("데이터 없음")
            }
        } catch (error: Throwable) {
            _rests.value = UiState.Error(error)
        }
    }

    private val _sortedRests: MutableSharedFlow<List<restItem>> = MutableSharedFlow(replay = 0)
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
    private val _shelters: MutableStateFlow<UiState<List<shelterItem>>> =
        MutableStateFlow(UiState.Uninitialized)
    val shelters: StateFlow<UiState<List<shelterItem>>> get() = _shelters.asStateFlow()
    fun getNearShelter(boundingBox: BoundingBox) = viewModelScope.launch(ioDispatcher) {
        _shelters.value = UiState.Loading
        try {
            shelterRepository.getAllShelter(boundingBox).collect {
                if (it.isNotEmpty()) _shelters.value = UiState.Success(it)
                else _shelters.value = UiState.Fail("데이터 없음")
            }
        } catch (error: Throwable) {
            _shelters.value = UiState.Error(error)
        }
    }

    private val _sortedShelters: MutableSharedFlow<List<shelterItem>> =
        MutableSharedFlow(replay = 0)
    val sortedShelters: SharedFlow<List<shelterItem>> get() = _sortedShelters
    fun sortShelters(nowLocation: Location, shelters: List<shelterItem>) =
        viewModelScope.launch(defaultDispatcher) {
            _sortedShelters.emit(shelters.sortedBy {// 정렬
                nowLocation.calculateDistance(
                    it.latitude.toDouble(), it.longitude.toDouble()
                )
            })
        }

    private val _parkingLots: MutableStateFlow<UiState<List<parkingLotItem>>> =
        MutableStateFlow(UiState.Uninitialized)
    val parkingLots: StateFlow<UiState<List<parkingLotItem>>> get() = _parkingLots.asStateFlow()

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
        _parkingLots.value = UiState.Loading

        getParkingLostItemListUseCase(
            boundingBox = boundingBox,
            parkingchargeInfo = parkingchargeInfo,
            numOfRows = numOfRows,
            day = day,
            nowTime = nowTime
        ).catch { error ->
            _parkingLots.value = UiState.Error(error)
        }.collect {
            combine(it) { responses ->
                responses.flatMap { it }
            }.cancellable().catch { error ->
                _parkingLots.value = UiState.Error(error)
            }.collectLatest { listItem ->
                if (listItem.isNotEmpty()) _parkingLots.value = UiState.Success(listItem)
                else _parkingLots.value = UiState.Fail("데이터 없음")
            }
        }
    }


    private val _sortedParkingLots: MutableSharedFlow<List<parkingLotItem>> =
        MutableSharedFlow(replay = 0)
    val sortedParkingLots: SharedFlow<List<parkingLotItem>> get() = _sortedParkingLots

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


    /* C : 이벤트 등록 메서드 */
    fun insertMusic(music: musicItem) {
        viewModelScope.launch() {
            musicRepository.insertMusic(music)
        }
    }

    private val _music = MutableStateFlow<List<musicItem>>(emptyList())
    val music: StateFlow<List<musicItem>> = _music.asStateFlow()
    fun getAllMusic() {
        viewModelScope.launch {
            musicRepository.getAllMusic().collect {
                _music.value = it
            }
        }
    }

    /* U : 이벤트 수정 메서드 */
    fun updateMusic(music: musicItem) {
        viewModelScope.launch {
            musicRepository.updateMusic(music)
        }
    }

    /* D : 이벤트 삭제 메서드 */
    fun deleteMusic(id: Int) {
        viewModelScope.launch {
            musicRepository.deleteMusic(id)
        }
    }

    private val _guideMode = MutableStateFlow<Boolean>(true)
    val guideMode: StateFlow<Boolean> = _guideMode.asStateFlow()

    private val _basicMusicMode = MutableStateFlow<Boolean>(true)
    val basicMusicMode: StateFlow<Boolean> = _basicMusicMode.asStateFlow()

    private val _musicVolume = MutableStateFlow<Int>(0)
    val musicVolume: StateFlow<Int> = _musicVolume.asStateFlow()

    private val _refreshTerm = MutableStateFlow<Int>(0)
    val refreshTerm: StateFlow<Int> = _refreshTerm.asStateFlow()

    fun getSettingModeInt(key: String) = viewModelScope.launch {
        settingRepository.getInt(key).collect {
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

    fun setSettingMode(key: String, value: Int) = viewModelScope.launch {
        settingRepository.setInt(key, value)
    }

    fun getSettingModeBool(key: String) = viewModelScope.launch {
        settingRepository.getBoolean(key).collect {
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

    fun setSettingMode(key: String, value: Boolean) = viewModelScope.launch {
        settingRepository.setBoolean(key, value)
    }


    private val _allSettings = MutableStateFlow<Pair<MutableList<Boolean>, MutableList<Int>>>(
        mutableListOf<Boolean>() to mutableListOf<Int>()
    )
    val allSettings: StateFlow<Pair<MutableList<Boolean>, MutableList<Int>>> =
        _allSettings.asStateFlow()

    fun getAllSetting() = viewModelScope.launch {
        getSettingUseCase().collect {
            _allSettings.value = it
        }
    }


}