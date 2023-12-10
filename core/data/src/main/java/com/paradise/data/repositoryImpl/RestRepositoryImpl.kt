package com.paradise.data.repositoryImpl

import com.core.model.BoundingBox
import com.core.model.restItem
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.data.repository.RestRepository
import com.paradise.network.Mapper.toRestItem
import com.paradise.network.provider.RestDataProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RestRepositoryImpl @Inject constructor(private val restDataProvider: RestDataProvider) :
    RestRepository {
    override suspend fun getAllRest(
        boundingBox: BoundingBox,
    ): Flow<List<restItem>> = flow<List<restItem>> {
        val nearRest = mutableListOf<restItem>()
        restDataProvider.getAllRest().collect { result ->
            if (result.isSuccessful && result.body() != null) {
                result.body()!!.response.body.items.asFlow().flowOn(defaultDispatcher)
                    .filter { item ->
                        val lat = item.latitude.toDoubleOrNull()
                        val lon = item.longitude.toDoubleOrNull()
                        val inLatRange =
                            lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
                        val inLonRange =
                            lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
                        inLatRange && inLonRange
                    }.collect { item ->
                        nearRest.add(item.toRestItem())
                    }
                emit(nearRest)
            }

        }
    }.flowOn(ioDispatcher).cancellable()

}