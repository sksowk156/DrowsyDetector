//package com.paradise.drowsydetector.ui.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.paradise.drowsydetector.data.local.room.music.Music
//import com.paradise.drowsydetector.domain.repository.MusicRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class MusicViewModel @Inject constructor(
//    private val musicRepository: MusicRepository,
//) : ViewModel() {
//
//    /* C : 이벤트 등록 메서드 */
//    fun insertMusic(music: Music) {
//        viewModelScope.launch() {
//            musicRepository.insertMusic(music)
//        }
//    }
//
//    private val _music = MutableStateFlow<List<Music>>(emptyList())
//    val music: StateFlow<List<Music>> = _music.asStateFlow()
//
//    /* R : 이벤트 전체 조회 메서드 */
//    fun getAllMusic() {
//        viewModelScope.launch {
//            musicRepository.getAllMusic().collect {
//                _music.value = it
//            }
//        }
//    }
//
//    /* U : 이벤트 수정 메서드 */
//    fun updateMusic(music: Music) {
//        viewModelScope.launch {
//            musicRepository.updateMusic(music)
//        }
//    }
//
//    /* D : 이벤트 삭제 메서드 */
//    fun deleteMusic(id: Int) {
//        viewModelScope.launch {
//            musicRepository.deleteMusic(id)
//        }
//    }
//
////    class MusicViewModelFactory(
////        private val musicRepository: MusicRepository,
////    ) :
////        ViewModelProvider.Factory {
////        override fun <T : ViewModel> create(modelClass: Class<T>): T {
////            return if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
////                MusicViewModel(musicRepository) as T
////            } else {
////                throw IllegalArgumentException()
////            }
////        }
////    }
//}