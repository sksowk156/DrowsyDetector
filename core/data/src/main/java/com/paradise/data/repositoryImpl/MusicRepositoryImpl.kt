package com.paradise.data.repositoryImpl

import com.core.model.musicItem
import com.paradise.data.mapper.toDataMusic
import com.paradise.data.mapper.toDomainMusicItem
import com.paradise.database.provider.MusicDataProvider
import com.paradise.domain.repository.MusicRepository
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(private val musicDataProvider: MusicDataProvider) :
    MusicRepository {
    override suspend fun insertMusic(musicItem: musicItem) {
        musicDataProvider.insertMusic(musicItem.toDataMusic())
    }

    override fun getAllMusic(): List<musicItem> =
        musicDataProvider.getAllMusic().map { it.toDomainMusicItem() }


    override suspend fun deleteMusic(id: Int) {
        musicDataProvider.deleteMusic(id)
    }

    override suspend fun updateMusic(musicItem: musicItem) {
        musicDataProvider.updateMusic(musicItem.toDataMusic())
    }
}