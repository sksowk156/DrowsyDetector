package com.paradise.drowsydetector.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paradise.drowsydetector.data.local.room.record.AnalyzeResult
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import com.paradise.drowsydetector.repository.StaticsRepository
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.mainDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class StaticsViewModel(
    private val staticsRepository: StaticsRepository,
) : ViewModel() {

    private val _allAnalyzeRecord = MutableStateFlow<List<AnalyzeResult>>(emptyList())
    val allAnalyzeRecord: StateFlow<List<AnalyzeResult>> = _allAnalyzeRecord.asStateFlow()
    fun insertRecord(analyzeResult: AnalyzeResult) {
        viewModelScope.launch() {
            staticsRepository.insertRecord(analyzeResult)
        }
    }

    fun getAllRecord() {
        viewModelScope.launch(mainDispatcher) {
            staticsRepository.getAllRecord().collect {
                _allAnalyzeRecord.value = it
            }
        }
    }

    private val _analyzeRecord = MutableSharedFlow<AnalyzeResult>()
    val analyzeRecord: SharedFlow<AnalyzeResult> = _analyzeRecord.asSharedFlow()

    fun getRecord(time: String) {
        viewModelScope.launch(mainDispatcher) {
            staticsRepository.getRecord(time).collect {
                _analyzeRecord.emit(it)
            }
        }
    }

    fun getRecord(id: Int) {
        viewModelScope.launch(mainDispatcher) {
            staticsRepository.getRecord(id).collect {
                _analyzeRecord.emit(it)
            }
        }
    }

    var currentAnayzeResult: AnalyzeResult? = null

    /* D : 이벤트 삭제 메서드 */
    fun deleteRecord(analyzeResult: AnalyzeResult) {
        viewModelScope.launch {
            staticsRepository.deleteRecord(analyzeResult)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            staticsRepository.deleteAllRecords()
        }
    }

    private val _allAnayzeResult = MutableStateFlow<Pair<List<WinkCount>, List<DrowsyCount>>>(
        emptyList<WinkCount>() to emptyList<DrowsyCount>()
    )
    val allAnayzeResult: StateFlow<Pair<List<WinkCount>, List<DrowsyCount>>> =
        _allAnayzeResult.asStateFlow()

    /**
     * Get analyze result
     *
     * zip은 combine과 다르게 두 Flow에서 각각 값이 다 방출되야지 나온다.
     *
     * 결과를 한번에 보여줘야 하기 때문에 동시에 나오게 만들었다.
     * @param recordId, 검색할 recordId == AnaylzeResult의 id
     */
    fun getAnalyzeResult(recordId: Int) =
        viewModelScope.launch(defaultDispatcher) {
            staticsRepository.getWinkCount(recordId)
                .zip(staticsRepository.getDrowsyCount(recordId)) { value1, value2 -> value1 to value2 }
                .collect {
                    _allAnayzeResult.value = it
                }
        }

    var currentWinkCount = 0 //
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