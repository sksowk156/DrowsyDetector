package com.paradise.drowsydetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.DrowsyRecord
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import com.paradise.drowsydetector.repository.StaticsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaticsViewModel(
    private val staticsRepository: StaticsRepository,
) : ViewModel() {

    private val _allDrowsyRecord = MutableStateFlow<List<DrowsyRecord>>(emptyList())
    val allDrowsyRecord: StateFlow<List<DrowsyRecord>> = _allDrowsyRecord.asStateFlow()
    fun insertRecord(drowsyRecord: DrowsyRecord) {
        viewModelScope.launch() {
            staticsRepository.insertRecord(drowsyRecord)
        }
    }

    fun getAllRecord() {
        viewModelScope.launch {
            staticsRepository.getAllRecord().collect {
                _allDrowsyRecord.value = it
            }
        }
    }

    private val _drowsyRecord = MutableSharedFlow<DrowsyRecord>()
    val drowsyRecord: SharedFlow<DrowsyRecord> = _drowsyRecord.asSharedFlow()

    fun getRecord(time: String) {
        viewModelScope.launch {
            staticsRepository.getRecord(time).collect {
                _drowsyRecord.emit(it)
            }
        }
    }

    fun getRecord(id: Int) {
        viewModelScope.launch {
            staticsRepository.getRecord(id).collect {
                _drowsyRecord.emit(it)
            }
        }
    }

    var currentDrowsyRecord: DrowsyRecord? = null

    /* D : 이벤트 삭제 메서드 */
    fun deleteRecord(drowsyRecord: DrowsyRecord) {
        viewModelScope.launch {
            staticsRepository.deleteRecord(drowsyRecord)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            staticsRepository.deleteAllRecords()
        }
    }


    var currentWinkCount = 0
    fun initWinkCount() {
        currentWinkCount = 0
    }


    fun insertWinkCount(winkCount: WinkCount) {
        viewModelScope.launch() {
            staticsRepository.insertWinkCount(winkCount)
        }
    }

    private val _winkCount = MutableStateFlow<List<WinkCount>>(emptyList())
    val winkCount: StateFlow<List<WinkCount>> = _winkCount.asStateFlow()
    fun getWinkCount(recordId: Int) {
        viewModelScope.launch {
            staticsRepository.getWinkCount(recordId).collect {
                _winkCount.value = it
            }
        }
    }

    fun getAllWinkCount() {
        viewModelScope.launch {
            staticsRepository.getAllWinkCount().collect {
                _winkCount.value = it
            }
        }
    }

    fun deleteAllWinkCount() {
        viewModelScope.launch {
            staticsRepository.deleteAllWinkCount()
        }
    }

    var currentDrowsyCount = 0
    fun initDrowsyCount() {
        currentDrowsyCount = 0
    }

    private val _drowsyCount = MutableStateFlow<List<DrowsyCount>>(emptyList())
    val drowsyCount: StateFlow<List<DrowsyCount>> = _drowsyCount.asStateFlow()
    fun insertDrowsyCount(drowsyCount: DrowsyCount) {
        viewModelScope.launch() {
            staticsRepository.insertDrowsyCount(drowsyCount)
        }
    }

    fun getDrowsyCount(recordId: Int) {
        viewModelScope.launch {
            staticsRepository.getDrowsyCount(recordId).collect {
                _drowsyCount.value = it
            }
        }
    }

    fun getAllDrowsyCount() {
        viewModelScope.launch {
            staticsRepository.getAllDrowsyCount().collect {
                _drowsyCount.value = it
            }
        }
    }

    fun deleteAllDrowsyCount() {
        viewModelScope.launch {
            staticsRepository.deleteAllDrowsyCount()
        }
    }

    class StaticsViewModelFactory(
        private val staticsRepository: StaticsRepository,
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(StaticsViewModel::class.java)) {
                StaticsViewModel(staticsRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}