package com.paradise.domain.repository

import com.core.model.restItem
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS

interface RestRepository {
    suspend fun getAllRest(
        pageNo: Int = 1,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
    ): List<restItem>
}