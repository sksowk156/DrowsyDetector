package com.paradise.data.repository

import com.core.model.shelterItem
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import kotlinx.coroutines.flow.Flow

interface ShelterRepository {
    suspend fun getAllShelter(
        boundingBox: BoundingBox,
        ): Flow<List<shelterItem>>
}