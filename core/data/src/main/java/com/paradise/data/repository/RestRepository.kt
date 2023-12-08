package com.paradise.data.repository

import com.core.model.restItem
import com.paradise.common.network.BoundingBox
import kotlinx.coroutines.flow.Flow

interface RestRepository {
    suspend fun getAllRest(
        boundingBox: BoundingBox,
    ): Flow<List<restItem>>
}