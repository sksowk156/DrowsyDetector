//package com.paradise.drowsydetector.data.repository
//
//import com.paradise.drowsydetector.data.local.room.music.Music
//import com.paradise.drowsydetector.data.local.room.music.MusicDao
//import com.paradise.drowsydetector.domain.repository.MusicRepository
//import javax.inject.Inject
//
//class MusicRepositoryImpl @Inject constructor(
//    private val musicDao: MusicDao,
//) : MusicRepository {
//    companion object {
//        @Volatile
//        private var instance: MusicRepositoryImpl? = null
//
//        fun getInstance(musicDao: MusicDao) = instance ?: synchronized(this) {
//            instance ?: MusicRepositoryImpl(musicDao).also { instance = it }
//        }
//    }
//
//    /* C : 이벤트 등록 메서드 */
//    override suspend fun insertMusic(music: Music) {
//        musicDao.insertMusic(music)
//    }
//
//    override fun getAllMusic() = musicDao.getAllMusic()
//
//    override suspend fun updateMusic(music: Music) {
//        musicDao.updateMusic(music)
//    }
//
//    /* D : 이벤트 삭제 메서드 */
//    override suspend fun deleteMusic(id: Int) {
//        musicDao.deleteMusic(id)
//    }
//}