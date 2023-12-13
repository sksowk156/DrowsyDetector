package com.paradise.data.repositoryImpl.local

import com.core.model.musicItem
import com.paradise.data.mapper.toDataMusic
import com.paradise.data.repository.MusicRepository
import com.paradise.database.mapper.toMusicItem
import com.paradise.database.room.dao.MusicDao
import com.paradise.database.room.model.Music
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(private val musicDataProvider: MusicDao) :
    MusicRepository {
    override suspend fun insertMusic(musicItem: musicItem) {
        musicDataProvider.insertMusic(musicItem.toDataMusic())
    }

    override fun getAllMusic(): Flow<List<musicItem>> =
        musicDataProvider.getAllMusic().map { it.map(Music::toMusicItem) }


    override suspend fun deleteMusic(id: Int) {
        musicDataProvider.deleteMusic(id)
    }

    override suspend fun updateMusic(musicItem: musicItem) {
        musicDataProvider.updateMusic(musicItem.toDataMusic())
    }
}