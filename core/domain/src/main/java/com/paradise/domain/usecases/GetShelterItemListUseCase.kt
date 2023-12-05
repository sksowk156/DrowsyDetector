package com.paradise.domain.usecases

import com.core.model.shelterItem
import com.paradise.common.UiState
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.domain.repository.ShelterRepository
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetShelterItemListUseCase @Inject constructor(private val shelterRepository: ShelterRepository) {
    operator fun invoke(boundingBox: BoundingBox) = flow<UiState<List<shelterItem>>> {
        emit(UiState.Loading)
        val nearShelter = mutableListOf<shelterItem>()
        shelterRepository.getAllShelter().asFlow().flowOn(defaultDispatcher).filter { item ->
            val lat = item.latitude.toDoubleOrNull()
            val lon = item.longitude.toDoubleOrNull()
            val inLatRange = lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
            val inLonRange =
                lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
            inLatRange && inLonRange
        }.collect { item ->
            nearShelter.add(item)
        }
        emit(UiState.Success(nearShelter))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()

}