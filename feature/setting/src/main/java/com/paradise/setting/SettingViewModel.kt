package com.paradise.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.model.musicItem
import com.paradise.common.network.BASICMUSICMODE
import com.paradise.common.network.GUIDEMODE
import com.paradise.common.network.MUSICVOLUME
import com.paradise.common.network.REFRESHTERM
import com.paradise.data.repository.MusicRepository
import com.paradise.data.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

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
}