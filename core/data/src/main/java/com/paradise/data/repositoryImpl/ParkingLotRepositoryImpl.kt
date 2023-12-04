package com.paradise.data.repositoryImpl

import com.paradise.data.mapper.toDomainParkingLotItemCount
import com.paradise.data.mapper.toDomainParkingLotItemList
import com.paradise.domain.model.parkingLotItem
import com.paradise.domain.repository.ParkingLotRepository
import com.paradise.network.provider.ParkingLotDataProvider
import javax.inject.Inject

class ParkingLotRepositoryImpl @Inject constructor(private val parkingLotDataProvider: ParkingLotDataProvider) :
    ParkingLotRepository {
    override suspend fun getAllParkingLot(
        pageNo: Int,
        numOfRows: Int,
        type: String,
        parkingchrgeInfo: String,
    ): List<parkingLotItem> {
        return parkingLotDataProvider.getAllParkingLot(pageNo, numOfRows, type, parkingchrgeInfo)
            .toDomainParkingLotItemList()
    }

    override suspend fun getOneParkingLot(
        pageNo: Int,
        numOfRows: Int,
        type: String,
        parkingchrgeInfo: String,
    ): Int {
        return parkingLotDataProvider.getAllParkingLot(pageNo, numOfRows, type, parkingchrgeInfo)
            .toDomainParkingLotItemCount()
    }
}