package com.paradise.drowsydetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.paradise.drowsydetector.data.local.music.Music
import com.paradise.drowsydetector.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(private val repository: MusicRepository) : ViewModel() {

    /* C : 이벤트 등록 메서드 */
    fun insertMusic(music: Music) {
        viewModelScope.launch() {
            repository.insertMusic(music)
        }
    }

    private val _music = MutableStateFlow<List<Music>>(emptyList())
    val music: StateFlow<List<Music>> = _music.asStateFlow()

    /* R : 이벤트 전체 조회 메서드 */
    fun getAllMusic() {
        viewModelScope.launch {
            repository.getAllMusic().collect {
                _music.value = it
            }
        }
    }

    /* U : 이벤트 수정 메서드 */
    fun updateMusic(music: Music) {
        viewModelScope.launch {
            repository.updateMusic(music)
        }
    }

    /* D : 이벤트 삭제 메서드 */
    fun deleteMusic(id: Int) {
        viewModelScope.launch {
            repository.deleteMusic(id)
        }
    }

    class SettingViewModelFactory(private val repository: MusicRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
                SettingViewModel(repository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}