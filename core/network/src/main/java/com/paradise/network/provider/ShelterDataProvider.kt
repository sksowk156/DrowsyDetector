package com.paradise.network.provider

import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.shelter.ShelterService
import com.paradise.network.retrofit.shelter.model.ShelterModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ShelterDataProvider(
    private val shelterService: ShelterService,
) {
    fun getAllShelter(
        pageNo: Int = 1,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
        ctprvnNm: String? = null,
        signguNm: String? = null,
    ): Flow<Response<ShelterModel>> =
        flow { emit(shelterService.getAllShelter(pageNo, numOfRows, type, ctprvnNm, signguNm)) }
}