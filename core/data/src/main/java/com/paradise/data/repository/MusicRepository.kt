package com.paradise.data.repository

import com.core.model.musicItem
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun insertMusic(musicItem: musicItem)
    fun getAllMusic(): Flow<List<musicItem>>
    suspend fun deleteMusic(id: Int)
    suspend fun updateMusic(musicItem: musicItem)
}