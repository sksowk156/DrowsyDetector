package com.paradise.domain.repository

import com.core.model.shelterItem
import com.paradise.common.network.DEFAULT_NUM_OF_ROWS

interface ShelterRepository {
    suspend fun getAllShelter(
        pageNo: Int = 1,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
        ctprvnNm: String? = null,
        signguNm: String? = null,
    ): List<shelterItem>
}