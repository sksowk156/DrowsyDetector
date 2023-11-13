package com.paradise.drowsydetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paradise.drowsydetector.repository.SettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingRepository: SettingRepository,
) : ViewModel() {

    private val _mode = MutableStateFlow<Boolean>(true)
    val mode: StateFlow<Boolean> = _mode.asStateFlow()

    /* R : 이벤트 전체 조회 메서드 */
    fun getSettingMode(key: String) {
        viewModelScope.launch {
            settingRepository.getBoolean(key).collect {
                _mode.value = it
            }
        }
    }

    fun setSettingMode(key: String, value: Boolean) {
        viewModelScope.launch {
            settingRepository.setBoolean(key, value)
        }
    }

    class SettingViewModelFactory(
        private val settingRepository: SettingRepository,
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
                SettingViewModel(settingRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}