package com.paradise.domain.usecases

import com.core.model.BoundingBox
import com.core.model.parkingLotItem
import com.paradise.common.network.DAY
import com.paradise.common.network.defaultDispatcher
import com.paradise.data.repository.ParkingLotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetParkingLostItemListUseCase @Inject constructor(private val parkingLotRepository: ParkingLotRepository) {
    operator fun invoke(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfRows: Int, day: DAY, nowTime: String,
    ) = flow<List<Flow<List<parkingLotItem>>>> {
        parkingLotRepository.getOneParkingLot(
            pageNo = 1, numOfRows = 1, parkingchrgeInfo = "무료"
        ).collect { totalCount ->
            var numOfCoroutineRequired = totalCount / numOfRows
            if (totalCount % numOfRows != 0) numOfCoroutineRequired++
            emit(
                getParkingLots1(
                    boundingBox = boundingBox,
                    parkingchargeInfo = parkingchargeInfo,
                    numOfCoroutineRequired = numOfCoroutineRequired,
                    day = day,
                    nowTime = nowTime
                )
            )
        }
    }.flowOn(defaultDispatcher).cancellable()

    suspend fun getParkingLots1(
        boundingBox: BoundingBox,
        parkingchargeInfo: String,
        numOfCoroutineRequired: Int,
        day: DAY,
        nowTime: String,
    ) = (1..numOfCoroutineRequired).map {
        parkingLotRepository.getAllParkingLot(
            pageNo = it,
            numOfRows = DEFAULT_BUFFER_SIZE,
            boundingBox = boundingBox,
            parkingchargeInfo = parkingchargeInfo,
            day = day,
            nowTime = nowTime,
            parkingchrgeInfo = "무료"
        )
    }
}