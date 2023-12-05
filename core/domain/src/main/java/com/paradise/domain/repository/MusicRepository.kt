package com.paradise.domain.repository

import com.core.model.musicItem

interface MusicRepository {
    suspend fun insertMusic(musicItem: musicItem)
    fun getAllMusic(): List<musicItem>
    suspend fun deleteMusic(id: Int)
    suspend fun updateMusic(musicItem: musicItem)
}