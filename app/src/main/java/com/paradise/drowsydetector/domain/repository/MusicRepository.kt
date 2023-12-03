package com.paradise.drowsydetector.domain.repository

import com.paradise.drowsydetector.data.local.room.music.Music
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun insertMusic(music: Music)
    fun getAllMusic(): Flow<List<Music>>
    suspend fun updateMusic(music: Music)
    suspend fun deleteMusic(id: Int)
}