package com.paradise.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.model.analyzeResultItem
import com.core.model.drowsyResultItem
import com.core.model.winkResultItem
import com.paradise.common.network.defaultDispatcher
import com.paradise.data.repository.AnalyzerResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaticsViewModel @Inject constructor(
    private val analyzerResultRepository: AnalyzerResultRepository,
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
                _allAnalyzeRecord.emit(it)
            }
        }
    }

    private val _analyzeRecord = MutableSharedFlow<analyzeResultItem>()
    val analyzeRecord: SharedFlow<analyzeResultItem> get() = _analyzeRecord

    fun getRecord(time: String) {
        viewModelScope.launch() {
            analyzerResultRepository.getRecord(time).collect {
                _analyzeRecord.emit(it)
            }
        }
    }

    fun getRecord(id: Int) {
        viewModelScope.launch() {
            analyzerResultRepository.getRecord(id).collect {
                _analyzeRecord.emit(it)
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
        MutableStateFlow<Pair<List<winkResultItem>, List<drowsyResultItem>>>(
            emptyList<winkResultItem>() to emptyList<drowsyResultItem>()
        )
    val allAnayzeResult: StateFlow<Pair<List<winkResultItem>, List<drowsyResultItem>>> =
        _allAnayzeResult.asStateFlow()

    /**
     * Get analyze result
     *
     * zip은 combine과 다르게 두 Flow에서 각각 값이 다 방출되야지 나온다.
     *
     * 결과를 한번에 보여줘야 하기 때문에 동시에 나오게 만들었다.
     * @param recordId, 검색할 recordId == AnaylzeResult의 id
     */
    fun getAnalyzeResult(recordId: Int) = viewModelScope.launch(defaultDispatcher) {
        analyzerResultRepository.getWinkCount(recordId)
            .zip(analyzerResultRepository.getDrowsyCount(recordId)) { value1, value2 -> value1 to value2 }
            .collect {
                _allAnayzeResult.value = it
            }
    }

    var currentWinkCount = 0 //
    fun initWinkCount() {
        currentWinkCount = 0
    }


    fun insertWinkCount(winkCount: winkResultItem) {
        viewModelScope.launch() {
            analyzerResultRepository.insertWinkCount(winkCount)
        }
    }

    private val _winkCount = MutableStateFlow<List<winkResultItem>>(emptyList())
    val winkCount: StateFlow<List<winkResultItem>> = _winkCount.asStateFlow()
    fun getWinkCount(recordId: Int) {
        viewModelScope.launch {
            analyzerResultRepository.getWinkCount(recordId).collect {
                _winkCount.value = it
            }
        }
    }

    fun getAllWinkCount() {
        viewModelScope.launch {
            analyzerResultRepository.getAllWinkCount().collect {
                _winkCount.value = it
            }
        }
    }

    fun deleteAllWinkCount() {
        viewModelScope.launch {
            analyzerResultRepository.deleteAllWinkCount()
        }
    }

    var currentDrowsyCount = 0
    fun initDrowsyCount() {
        currentDrowsyCount = 0
    }

    private val _drowsyCount = MutableStateFlow<List<drowsyResultItem>>(emptyList())
    val drowsyCount: StateFlow<List<drowsyResultItem>> = _drowsyCount.asStateFlow()
    fun insertDrowsyCount(drowsyCount: drowsyResultItem) {
        viewModelScope.launch() {
            analyzerResultRepository.insertDrowsyCount(drowsyCount)
        }
    }

    fun getDrowsyCount(recordId: Int) {
        viewModelScope.launch {
            analyzerResultRepository.getDrowsyCount(recordId).collect {
                _drowsyCount.value = it
            }
        }
    }

    fun getAllDrowsyCount() {
        viewModelScope.launch {
            analyzerResultRepository.getAllDrowsyCount().collect {
                _drowsyCount.value = it
            }
        }
    }

    fun deleteAllDrowsyCount() {
        viewModelScope.launch {
            analyzerResultRepository.deleteAllDrowsyCount()
        }
    }

    //    var standard: Double?= null
//
//    fun initStandard(){
//        standard = null
//    }

//    var datajob: Job? = null // 눈 깜빡임 감지
//    var eyeClosed : Boolean = false
//    var currentAnayzeResult: AnalyzeResult? = null
//
//    fun startRecording() {
//        if (datajob == null) {
//            datajob = initJob()
//            datajob!!.start()
//        }
//    }
//
//    private fun initJob() = viewModelScope.launch(defaultDispatcher, CoroutineStart.LAZY) {
//        while (this.isActive) {
//            delay(1 * 60 * 1000)  // 30분 대기
//            Log.d("whatisthis", currentWinkCount.toString()+ " " + currentDrowsyCount.toString())
//            insertWinkCount(
//                WinkCount(
//                    recordId = currentAnayzeResult!!.id, value = currentWinkCount
//                )
//            )
//            insertDrowsyCount(
//                DrowsyCount(
//                    recordId = currentAnayzeResult!!.id, value = currentDrowsyCount
//                )
//            )
//            // 초기화
//            initWinkCount()
//            initDrowsyCount()
//        }
//    }
//
//    fun checkEyeWink(eyeState: Double, resultDetector: Face) {
//        // 눈 깜빡임 카운팅
//        if (eyeState <= 0.4) {
//            val leftEyeOpen = resultDetector.leftEyeOpenProbability
//            val righEyeOpen = resultDetector.rightEyeOpenProbability
//
//            if (datajob != null && datajob!!.isActive) {
//                if (leftEyeOpen != null && righEyeOpen != null) {
//                    if (leftEyeOpen < 0.3 && righEyeOpen < 0.3 && !eyeClosed) {
//                        eyeClosed = true // frame이 높아서 한번에 많이 처리될 수 있기 때문
//                        // 눈이 감겨 있음
//                        currentWinkCount++
//                        Log.d("whatisthis",currentWinkCount.toString())
//                    }
//                }
//            }
//        }
//    }
}