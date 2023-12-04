package com.paradise.network.provider

import com.paradise.network.retrofit.shelter.ShelterService
import javax.inject.Inject

class ShelterDataProvider @Inject constructor(
    private val shelterService: ShelterService,
) {
    suspend fun getAllShelter(
        pageNo: Int,
        numOfRows: Int,
        type: String,
        ctprvnNm: String,
        signguNm: String,
    ) = shelterService.getAllShelter(pageNo, numOfRows, type, ctprvnNm, signguNm)
}