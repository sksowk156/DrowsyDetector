package com.paradise.data.repository

import com.core.model.BoundingBox
import com.core.model.shelterItem
import kotlinx.coroutines.flow.Flow

interface ShelterRepository {
    suspend fun getAllShelter(
        boundingBox: BoundingBox,
        ): Flow<List<shelterItem>>
}