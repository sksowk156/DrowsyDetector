package com.paradise.network.provider

import com.paradise.common.network.DEFAULT_NUM_OF_ROWS
import com.paradise.network.retrofit.shelter.ShelterService
import com.paradise.network.retrofit.shelter.model.ShelterModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShelterDataProvider @Inject constructor(
    private val shelterService: ShelterService,
) {
    suspend fun getAllShelter(
        pageNo: Int = 1,
        numOfRows: Int = DEFAULT_NUM_OF_ROWS,
        type: String = "json",
        ctprvnNm: String? = null,
        signguNm: String? = null,
    ): Flow<ShelterModel> =
        flow { shelterService.getAllShelter(pageNo, numOfRows, type, ctprvnNm, signguNm) }
}