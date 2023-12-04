package com.paradise.domain.repository

import com.paradise.domain.model.shelterItem

interface ShelterRepository {
    suspend fun getAllShelter(
        pageNo: Int,
        numOfRows: Int,
        type: String = "json",
        ctprvnNm: String,
        signguNm: String,
    ): List<shelterItem>
}