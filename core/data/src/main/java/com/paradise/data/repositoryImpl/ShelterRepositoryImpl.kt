package com.paradise.data.repositoryImpl

import com.paradise.data.mapper.toDomainShelterItemList
import com.paradise.domain.model.shelterItem
import com.paradise.domain.repository.ShelterRepository
import com.paradise.network.provider.ShelterDataProvider
import javax.inject.Inject

class ShelterRepositoryImpl @Inject constructor(private val shelterDataProvider: ShelterDataProvider) :
    ShelterRepository {
    override suspend fun getAllShelter(
        pageNo: Int,
        numOfRows: Int,
        type: String,
        ctprvnNm: String,
        signguNm: String,
    ): List<shelterItem> {
        return shelterDataProvider.getAllShelter(pageNo, numOfRows, type, ctprvnNm, signguNm)
            .toDomainShelterItemList()
    }
}