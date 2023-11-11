package com.paradise.drowsydetector.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLot
import com.paradise.drowsydetector.data.remote.shelter.DrowsyShelter
import com.paradise.drowsydetector.data.remote.shelter.DrowsyShelterService
import com.paradise.drowsydetector.repository.RelaxRepository
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.ioDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import retrofit2.Response

data class temp(var tempLocation: Location, var s: String, var b: String)
class AnalyzeViewModel(private val repository: RelaxRepository) : ViewModel() {

    var temp = MutableLiveData<Response<DrowsyShelter>>()

    fun get(){
        viewModelScope.launch {
            temp.value = DrowsyShelterService.getRetrofitRESTInstance().getAllShelter(ctprvnNm= "경기도", signguNm="김포시")
        }
    }

    private val _allShelter: MutableStateFlow<ResponseState<DrowsyShelter>> =
        MutableStateFlow(ResponseState.Loading)
    val allShelter: StateFlow<ResponseState<DrowsyShelter>> get() = _allShelter.asStateFlow()
    fun getAllShelter(ctprvnNm: String, signguNm: String) = viewModelScope.launch {
        _allShelter.value = ResponseState.Loading
        repository.getAllShelter(ctprvnNm, signguNm)
            .catch { error ->
                _allShelter.value = ResponseState.Error(error)
            }
            .collect {
                _allShelter.value = it
            }
    }

    var checkDrowsy = true

    var location: temp? = null
    fun setLocation(tempLocation: Location, s: String, b: String) {
        location = temp(tempLocation, s, b)
    }


    private val _allParkingLot: MutableStateFlow<ResponseState<ParkingLot>> =
        MutableStateFlow(ResponseState.Loading)
    val allParkingLot: StateFlow<ResponseState<ParkingLot>> get() = _allParkingLot.asStateFlow()
    fun getAllParkingLot() =
        viewModelScope.launch(ioDispatcher) {
            _allParkingLot.value = ResponseState.Loading
            repository.getAllParkingLot()
                .catch { error ->
                    _allParkingLot.value = ResponseState.Error(error)
                }.collect {
                    Log.d("whatisthis","11")
                    _allParkingLot.value = it
                }
        }


    class AnalyzeViewModelFactory(private val repository: RelaxRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(AnalyzeViewModel::class.java)) {
                AnalyzeViewModel(repository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}