package com.paradise.domain.usecases

import com.paradise.common.UiState
import com.paradise.domain.model.shelterItem
import com.paradise.domain.repository.ShelterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetShelterItemListUseCase @Inject constructor(private val shelterRepository: ShelterRepository) {

//    operator fun invoke() = flow<UiState<List<shelterItem>>> {
//
//    }
//
//    override suspend fun getAllShelter(
//        boundingBox: BoundingBox,
//    ): Flow<ResponseState<List<shelterItem>>> = flow {
//        try {
//            val response = shelterInterface.getAllShelter()
//            if (response.isSuccessful) {
//                response.body()?.let {
//                    val nearShelter = mutableListOf<shelterItem>()
//                    it.response.body.items.asFlow().flowOn(defaultDispatcher).filter { item ->
//                        val lat = item.latitude.toDoubleOrNull()
//                        val lon = item.longitude.toDoubleOrNull()
//                        val inLatRange =
//                            lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
//                        val inLonRange =
//                            lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
//                        inLatRange && inLonRange
//                    }.collect { item ->
//                        nearShelter.add(item)
//                    }
//                    emit(ResponseState.Success(nearShelter))
//                }
//            } else {
//
//
//                emit(ResponseState.Fail(response.code(), response.message()))
//            }
//        } catch (e: Exception) {
//            emit(ResponseState.Error(e))
//        } as Unit
//    }.flowOn(ioDispatcher).cancellable()
}