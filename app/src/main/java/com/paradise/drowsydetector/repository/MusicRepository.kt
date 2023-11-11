package com.paradise.drowsydetector.repository

import com.paradise.drowsydetector.data.local.music.Music
import com.paradise.drowsydetector.data.local.music.MusicDao

class MusicRepository(
    private val musicDao: MusicDao,
) {
    companion object {
        @Volatile
        private var instance: MusicRepository? = null

        fun getInstance(musicDao: MusicDao) =
            instance ?: synchronized(this) {
                instance ?: MusicRepository(musicDao).also { instance = it }
            }
    }

    /* C : 이벤트 등록 메서드 */
    suspend fun insertMusic(music: Music) {
        musicDao.insertMusic(music)
    }

    fun getAllMusic() = musicDao.getAllMusic()

    suspend fun updateMusic(music: Music) {
        musicDao.updateMusic(music)
    }

    /* D : 이벤트 삭제 메서드 */
    suspend fun deleteMusic(id: Int) {
        musicDao.deleteMusic(id)
    }
}