package com.paradise.database.provider

import com.paradise.database.room.dao.MusicDao
import com.paradise.database.room.model.Music
import javax.inject.Inject

class MusicDataProvider @Inject constructor(
    private val musicDao: MusicDao,
) {
    suspend fun insertMusic(music: Music) {
        musicDao.insertMusic(music)
    }

    fun getAllMusic() = musicDao.getAllMusic()

    suspend fun deleteMusic(id: Int) {
        musicDao.deleteMusic(id)
    }

    suspend fun updateMusic(music: Music) {
        musicDao.updateMusic(music)
    }
}