package com.paradise.domain.usecases

import com.paradise.common.UiState
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.ioDispatcher
import com.paradise.domain.model.restItem
import com.paradise.domain.repository.RestRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetRestItemListUseCase @Inject constructor(private val restRepository: RestRepository) {
//    operator fun invoke() = flow<UiState<List<restItem>>> {
//
//    }
//
//    override suspend fun getAllRest(
//        boundingBox: BoundingBox,
//    ): Flow<ResponseState<List<restItem>>> = flow {
//        try {
//            val response = restInterface.getAllRest()
//            if (response.isSuccessful) {
//                response.body()?.let {
//                    val nearRest = mutableListOf<restItem>()
//                    it.response.body.items.asFlow().flowOn(defaultDispatcher).filter { item ->
//                        val lat = item.latitude.toDoubleOrNull()
//                        val lon = item.longitude.toDoubleOrNull()
//                        val inLatRange =
//                            lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
//                        val inLonRange =
//                            lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
//                        inLatRange && inLonRange
//                    }.collect { item ->
//                        nearRest.add(item)
//                    }
//                    emit(ResponseState.Success(nearRest))
//                }
//            } else {
//                emit(ResponseState.Fail(response.code(), response.message()))
//            }
//        } catch (e: Exception) {
//            emit(ResponseState.Error(e))
//        } finally {
//            currentCoroutineContext().cancel()
//        } as Unit
//    }.flowOn(ioDispatcher).cancellable()

}