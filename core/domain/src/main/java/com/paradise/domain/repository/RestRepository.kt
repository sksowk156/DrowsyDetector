package com.paradise.domain.repository

import com.paradise.domain.model.restItem

interface RestRepository {
    suspend fun getAllRest(
        pageNo: Int,
        numOfRows: Int,
        type: String = "json",
    ): List<restItem>
}