package com.paradise.data.repositoryImpl

//class RelaxRepositoryImpl @Inject constructor(private val networkDataProvider: NetworkDataProvider) :
//    RelaxRepository {
//    override suspend fun getAllRest(
//        pageNo: Int,
//        numOfRows: Int,
//        type: String,
//    ): List<restItem> {
//        return networkDataProvider.getAllRest(pageNo, numOfRows, type).toDomainRestItemList()
//    }
//
//    override suspend fun getAllParkingLot(
//        pageNo: Int,
//        numOfRows: Int,
//        type: String,
//        parkingchrgeInfo: String,
//    ): List<parkingLotItem> {
//        return networkDataProvider.getAllParkingLot(pageNo, numOfRows, type, parkingchrgeInfo)
//            .toDomainParkingLotItemList()
//    }
//
//    override suspend fun getAllShelter(
//        pageNo: Int,
//        numOfRows: Int,
//        type: String,
//        ctprvnNm: String,
//        signguNm: String,
//    ): List<shelterItem> {
//        return networkDataProvider.getAllShelter(pageNo, numOfRows, type, ctprvnNm, signguNm)
//            .toDomainShelterItemList()
//    }
//}