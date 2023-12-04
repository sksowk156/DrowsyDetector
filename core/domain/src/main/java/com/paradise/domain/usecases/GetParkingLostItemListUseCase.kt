package com.paradise.domain.usecases

import com.paradise.common.UiState
import com.paradise.common.network.BoundingBox
import com.paradise.common.network.DAY
import com.paradise.common.network.isInTime
import com.paradise.domain.model.parkingLotItem
import com.paradise.domain.repository.ParkingLotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetParkingLostItemListUseCase @Inject constructor(private val parkingLotRepository: ParkingLotRepository) {
//    operator fun invoke(
//        boundingBox: BoundingBox,
//        parkingchargeInfo: String,
//        numOfRows: Int, day: DAY, nowTime: String,
//    ) = flow<UiState<List<Flow<UiState<List<parkingLotItem>>>>>> {
//        emit(UiState.Loading)
//        val response = parkingLotRepository.getOneParkingLot(
//            pageNo = 1, numOfRows = 1, parkingchrgeInfo = "무료"
//        )
//        val totalCount = response
//        var numOfCoroutineRequired = totalCount / numOfRows
//        if (totalCount % numOfRows != 0) numOfCoroutineRequired++
//        emit(
//            UiState.Success(
//                getParkingLots1(
//                    boundingBox = boundingBox,
//                    parkingchargeInfo = parkingchargeInfo,
//                    numOfCoroutineRequired = numOfCoroutineRequired,
//                    day = day,
//                    nowTime = nowTime
//                )
//            )
//        )
//    }.catch {
//        emit(UiState.Error(it))
//    }.flowOn(Dispatchers.Default).cancellable()
//
//    fun getParkingLots1(
//        boundingBox: BoundingBox,
//        parkingchargeInfo: String,
//        numOfCoroutineRequired: Int,
//        day: DAY,
//        nowTime: String,
//    ) = (1..numOfCoroutineRequired).map {
//        getParkingLot1(it, boundingBox, parkingchargeInfo, day, nowTime)
//    }
//
//    fun getParkingLot1(
//        pageNo: Int, boundingBox: BoundingBox, parkingchargeInfo: String, day: DAY, nowTime: String,
//    ) = flow<UiState<List<parkingLotItem>>> {
//        val response = parkingLotRepository.getAllParkingLot(
//            pageNo = pageNo, parkingchrgeInfo = "무료"
//        )
//        val freeParkingLot = mutableListOf<parkingLotItem>()
//        response.asFlow().flowOn(Dispatchers.Default).filter { item ->
//            val lat = item.latitude.toDoubleOrNull()
//            val lon = item.longitude.toDoubleOrNull()
//            val inLatRange = lat != null && lat in boundingBox.minLatitude..boundingBox.maxLatitude
//            val inLonRange =
//                lon != null && lon in boundingBox.minLongitude..boundingBox.maxLongitude
//            val inTime = isInTime(day = day, nowTime = nowTime, item = item)
//            inLatRange && inLonRange && inTime
//        }.collect { item ->
//            freeParkingLot.add(item)
//        }
//        emit(UiState.Success(freeParkingLot))
//    }.catch {
//        emit(UiState.Error(it))
//    }.flowOn(Dispatchers.IO).cancellable()
}