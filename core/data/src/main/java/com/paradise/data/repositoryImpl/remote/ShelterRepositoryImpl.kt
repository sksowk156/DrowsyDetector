package com.paradise.data.repositoryImpl.remote

import com.core.model.BoundingBox
import com.core.model.shelterItem
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.data.repository.ShelterRepository
import com.paradise.network.Mapper.toShelterItem
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
    override suspend fun getAllShelter(
        boundingBox: BoundingBox,
    ): Flow<List<shelterItem>> = flow<List<shelterItem>> {
        val nearShelter = mutableListOf<shelterItem>()
        shelterDataProvider.getAllShelter().map { result ->
            result.response.body.items.asFlow().flowOn(defaultDispatcher).filter { item ->
                val lat = item.latitude.toDoubleOrNull()
                val lon = item.longitude.toDoubleOrNull()
                val inLatRange =
                    lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
                val inLonRange =
                    lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
                inLatRange && inLonRange
            }.collect { item ->
                nearShelter.add(item.toShelterItem())
            }
            emit(nearShelter)
        }
    }.flowOn(ioDispatcher).cancellable()

}