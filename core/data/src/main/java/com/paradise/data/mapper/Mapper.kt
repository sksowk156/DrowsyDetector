package com.paradise.data.mapper

import com.paradise.domain.model.parkingLotItem
import com.paradise.domain.model.restItem
import com.paradise.domain.model.shelterItem
import com.paradise.network.retrofit.parkinglot.model.ParkingLotModel
import com.paradise.network.retrofit.rest.model.RestModel
import com.paradise.network.retrofit.shelter.model.ShelterModel

fun ParkingLotModel.toDomainParkingLotItemCount(): Int {
    return this.response.body.totalCount.toInt()
}

fun ParkingLotModel.toDomainParkingLotItemList(): List<parkingLotItem> {
    return this.response.body.items.map {
        parkingLotItem(
            it.holidayCloseOpenHhmm,
            it.holidayOperOpenHhmm,
            it.latitude,
            it.lnmadr,
            it.longitude,
            it.parkingchrgeInfo,
            it.prkplceNm,
            it.satOperCloseHhmm,
            it.satOperOperOpenHhmm,
            it.weekdayOperColseHhmm,
            it.weekdayOperOpenHhmm,
        )
    }
}

fun ShelterModel.toDomainShelterItemList(): List<shelterItem> {
    return this.response.body.items.map {
        shelterItem(
            it.latitude,
            it.longitude,
            it.lnmadr,
            it.roadRouteDrc,
            it.signguNm
        )
    }
}

fun RestModel.toDomainRestItemList(): List<restItem> {
    return this.response.body.items.map {
        restItem(
            it.latitude,
            it.longitude,
            it.roadRouteNm,
            it.roadRouteDrc
        )
    }
}