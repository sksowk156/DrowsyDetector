//package com.paradise.drowsydetector.ui.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import com.paradise.drowsydetector.domain.repository.SettingRepository
//import com.paradise.drowsydetector.utils.BASICMUSICMODE
//import com.paradise.drowsydetector.utils.GUIDEMODE
//import com.paradise.drowsydetector.utils.MUSICVOLUME
//import com.paradise.drowsydetector.utils.REFRESHTERM
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.zip
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class SettingViewModel @Inject constructor(
//    private val settingRepository: SettingRepository,
//) : ViewModel() {
//
//    private val _guideMode = MutableStateFlow<Boolean>(true)
//    val guideMode: StateFlow<Boolean> = _guideMode.asStateFlow()
//
//    private val _basicMusicMode = MutableStateFlow<Boolean>(true)
//    val basicMusicMode: StateFlow<Boolean> = _basicMusicMode.asStateFlow()
//
//    private val _musicVolume = MutableStateFlow<Int>(0)
//    val musicVolume: StateFlow<Int> = _musicVolume.asStateFlow()
//
//    private val _refreshTerm = MutableStateFlow<Int>(0)
//    val refreshTerm: StateFlow<Int> = _refreshTerm.asStateFlow()
//
//    fun getSettingModeInt(key: String) = viewModelScope.launch {
//        settingRepository.getInt(key).collect {
//            when (key) {
//                MUSICVOLUME -> {
//                    _musicVolume.value = it
//                }
//
//                REFRESHTERM -> {
//                    _refreshTerm.value = it
//                }
//            }
//        }
//    }
//
//
//    fun setSettingMode(key: String, value: Int) = viewModelScope.launch {
//        settingRepository.setInt(key, value)
//    }
//
//
//    fun getSettingModeBool(key: String) = viewModelScope.launch {
//        settingRepository.getBoolean(key).collect {
//            when (key) {
//                GUIDEMODE -> {
//                    _guideMode.value = it
//                }
//
//                BASICMUSICMODE -> {
//                    _basicMusicMode.value = it
//                }
//            }
//        }
//    }
//
//
//    fun setSettingMode(key: String, value: Boolean) = viewModelScope.launch {
//        settingRepository.setBoolean(key, value)
//    }
//
//
//    private val _allSettings = MutableStateFlow<Pair<MutableList<Boolean>, MutableList<Int>>>(
//        mutableListOf<Boolean>() to mutableListOf<Int>()
//    )
//    val allSettings: StateFlow<Pair<MutableList<Boolean>, MutableList<Int>>> =
//        _allSettings.asStateFlow()
//
//    fun getAllSetting() = viewModelScope.launch {
//        with(settingRepository) {
//            this.getBoolean(GUIDEMODE)
//                .zip(this.getBoolean(BASICMUSICMODE)) { a, b -> mutableListOf(a, b) }
//                .zip(this.getInt(MUSICVOLUME)) { list, c -> list to mutableListOf(c) }
//                .zip(this.getInt(REFRESHTERM)) { pair, d ->
//                    pair.second.add(d)
//                    pair.first to pair.second
//                }.collect {
//                    _allSettings.value = it
//                }
//        }
//    }
//
//
////    class SettingViewModelFactory(
////        private val settingRepository: SettingRepository,
////    ) :
////        ViewModelProvider.Factory {
////        override fun <T : ViewModel> create(modelClass: Class<T>): T {
////            return if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
////                SettingViewModel(settingRepository) as T
////            } else {
////                throw IllegalArgumentException()
////            }
////        }
////    }
//}