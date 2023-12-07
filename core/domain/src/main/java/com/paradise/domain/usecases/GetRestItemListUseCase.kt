package com.paradise.domain.usecases

import com.core.model.restItem
import com.paradise.common.result.UiState
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.domain.repository.RestRepository
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetRestItemListUseCase @Inject constructor(private val restRepository: RestRepository) {
    operator fun invoke(boundingBox: BoundingBox) = flow<UiState<List<restItem>>> {
        emit(UiState.Loading)
        val nearRest = mutableListOf<restItem>()
        restRepository.getAllRest().asFlow().flowOn(defaultDispatcher).filter { item ->
            val lat = item.latitude.toDoubleOrNull()
            val lon = item.longitude.toDoubleOrNull()
            val inLatRange = lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
            val inLonRange =
                lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
            inLatRange && inLonRange
        }.collect { item ->
            nearRest.add(item)
        }
        emit(UiState.Success(nearRest))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()

}