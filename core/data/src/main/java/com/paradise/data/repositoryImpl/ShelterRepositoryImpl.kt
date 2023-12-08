package com.paradise.data.repositoryImpl

import com.core.model.shelterItem
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.data.repository.ShelterRepository
import com.paradise.network.Mapper.toShelterItemList
import com.paradise.network.provider.ShelterDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShelterRepositoryImpl @Inject constructor(private val shelterDataProvider: ShelterDataProvider) :
    ShelterRepository {
//    override suspend fun getAllShelter(
//        pageNo: Int,
//        numOfRows: Int,
//        type: String,
//        ctprvnNm: String?,
//        signguNm: String?,
//    ): Flow<List<shelterItem>> =
//        shelterDataProvider.getAllShelter(pageNo, numOfRows, type, ctprvnNm, signguNm)
//            .map { it.toShelterItemList() }
    override suspend fun getAllShelter(
        boundingBox: BoundingBox,
    ): Flow<List<shelterItem>> = flow<List<shelterItem>> {
        val nearShelter = mutableListOf<shelterItem>()
        shelterDataProvider.getAllShelter().map { result ->
            val resultConverted = result.toShelterItemList()
            resultConverted.asFlow().flowOn(defaultDispatcher).filter { item ->
                val lat = item.latitude.toDoubleOrNull()
                val lon = item.longitude.toDoubleOrNull()
                val inLatRange =
                    lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
                val inLonRange =
                    lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
                inLatRange && inLonRange
            }.collect { item ->
                nearShelter.add(item)
            }
            emit(nearShelter)
        }
    }.flowOn(ioDispatcher).cancellable()

}