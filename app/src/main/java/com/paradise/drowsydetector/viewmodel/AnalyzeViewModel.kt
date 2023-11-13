package com.paradise.drowsydetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paradise.drowsydetector.repository.RelaxRepository
import com.paradise.drowsydetector.utils.BoundingBox
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.ioDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    var checkDrowsy = true

    private val _rests: MutableStateFlow<ResponseState<List<restItem>>> =
        MutableStateFlow(ResponseState.Loading)
    val rests: StateFlow<ResponseState<List<restItem>>> get() = _rests.asStateFlow()
    fun getNearRest(boundingBox: BoundingBox) = viewModelScope.launch {
        _rests.value = ResponseState.Loading
        relaxRepository.getAllRest(boundingBox)
            .catch { error ->
                _rests.value = ResponseState.Error(error)
            }
            .collect {
                _rests.value = it
            }
    }


    private val _shelters: MutableStateFlow<ResponseState<List<shelterItem>>> =
        MutableStateFlow(ResponseState.Loading)
    val shelters: StateFlow<ResponseState<List<shelterItem>>> get() = _shelters.asStateFlow()
    fun getNearShelter(boundingBox: BoundingBox) = viewModelScope.launch {
        _shelters.value = ResponseState.Loading
        relaxRepository.getAllShelter(boundingBox)
            .catch { error ->
                _shelters.value = ResponseState.Error(error)
            }
            .collect {
                _shelters.value = it
            }
    }

    private val _parkingLots: MutableStateFlow<ResponseState<List<parkingLotItem>>> =
        MutableStateFlow(ResponseState.Loading)
    val parkingLots: StateFlow<ResponseState<List<parkingLotItem>>> get() = _parkingLots.asStateFlow()
    fun getNearParkingLot(boundingBox: BoundingBox) {
        viewModelScope.launch(ioDispatcher) {
            combine(
                relaxRepository.getAllParkingLot(boundingBox, "무료", 15)
            ) { responses ->
                val combinedList = responses
                    .filterIsInstance<ResponseState.Success<List<parkingLotItem>>>()
                    .flatMap { it.data }
                ResponseState.Success(combinedList)
            }.catch { error ->
                _parkingLots.value = ResponseState.Error(error)
            }.collect {
                _parkingLots.value = it
            }
        }
    }

    /**
     * Get all parking lot2
     * 한 데이터를 받아와서 전체 데이터 개수를 파악한 뒤, 필요한만큼 코루틴 스코프를 생성해 모든 데이터를 한번에 불러오는 메서드(코루틴 스코프가 많을 수 있다.)
     * @param boundingBox
     */
    fun getNearParkingLot2(boundingBox: BoundingBox, numOfRows: Int) {
        viewModelScope.launch(ioDispatcher) {
            relaxRepository.getAllParkingLot2(boundingBox, "무료", numOfRows)
                .catch { error ->
                    _parkingLots.value = ResponseState.Error(error)
                }
                .collect {
                    if (it is ResponseState.Success) {
                        combine(
                            it.data
                        ) { responses ->
                            val combinedList = responses
                                .filterIsInstance<ResponseState.Success<List<parkingLotItem>>>()
                                .flatMap { it.data }
                            ResponseState.Success(combinedList)
                        }.catch { error ->
                            _parkingLots.value = ResponseState.Error(error)
                        }.collect {
                            _parkingLots.value = it
                        }
                    }
                }
        }
    }

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