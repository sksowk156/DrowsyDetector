package com.paradise.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.winkResultItem
import com.paradise.common.network.defaultDispatcher
import com.paradise.data.repository.AnalyzerResultRepository
import com.paradise.domain.usecases.GetAnalyzeResultItemListUseCase
import com.paradise.domain.usecases.GetAnalyzeResultItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val analyzerResultRepository: AnalyzerResultRepository,
    private val getAnalyzeResultItemListUseCase: GetAnalyzeResultItemListUseCase,
    private val getAnalyzeResultItemUseCase: GetAnalyzeResultItemUseCase,
) : ViewModel() {
    private val _allAnalyzeRecord = MutableSharedFlow<List<analyzeResultItem>>()
    val allAnalyzeRecord: SharedFlow<List<analyzeResultItem>> get() = _allAnalyzeRecord.asSharedFlow()
    fun insertRecord(analyzeResult: analyzeResultItem) {
        viewModelScope.launch() {
            analyzerResultRepository.insertRecord(analyzeResult)
        }
    }

    fun getAllRecord() {
        viewModelScope.launch() {
            analyzerResultRepository.getAllRecord().collect {
                if (it != null) _allAnalyzeRecord.emit(it)
            }
        }
    }

    private val _analyzeRecord = MutableSharedFlow<analyzeResultItem>()
    val analyzeRecord: SharedFlow<analyzeResultItem> get() = _analyzeRecord

    fun getRecord(time: String) {
        viewModelScope.launch() {
            getAnalyzeResultItemListUseCase(time).collect {
                if (it != null) _analyzeRecord.emit(it)
            }
        }
    }

    fun getRecord(id: Int) {
        viewModelScope.launch() {
            getAnalyzeResultItemListUseCase(id).collect {
                if (it != null) _analyzeRecord.emit(it)
            }
        }
    }

    /* D : 이벤트 삭제 메서드 */
    fun deleteRecord(analyzeResult: analyzeResultItem) {
        viewModelScope.launch {
            analyzerResultRepository.deleteRecord(analyzeResult)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            analyzerResultRepository.deleteAllRecords()
        }
    }

    private val _allAnayzeResult =
        MutableStateFlow<Pair<List<winkResultItem>?, List<drowsyResultItem>?>>(
            emptyList<winkResultItem>() to emptyList<drowsyResultItem>()
        )
    val allAnayzeResult: StateFlow<Pair<List<winkResultItem>?, List<drowsyResultItem>?>> =
        _allAnayzeResult.asStateFlow()

    /**
     * Get analyze result
     *
     * zip은 combine과 다르게 두 Flow에서 각각 값이 다 방출되야지 나온다.
     *
     * 결과를 한번에 보여줘야 하기 때문에 동시에 나오게 만들었다.
     * @param recordId, 검색할 recordId == AnaylzeResult의 id
     */
//    fun getAnalyzeResult(recordId: Int) = viewModelScope.launch(defaultDispatcher) {
//        getWinkItemListUseCase(recordId)
//            .zip(getDrowsyItemListUseCase(recordId)) { value1, value2 -> value1 to value2 }
//            .collect {
//                _allAnayzeResult.value = it
//            }
//    }
    fun getAnalyzeResult(recordId: Int) = viewModelScope.launch(defaultDispatcher) {
        getAnalyzeResultItemUseCase(recordId).collect {
                _allAnayzeResult.value = it
            }
    }
}