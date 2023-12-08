package com.paradise.network.Mapper

import com.core.model.parkingLotItem
import com.core.model.restItem
import com.core.model.shelterItem
import com.paradise.network.retrofit.parkinglot.model.ParkingLotModel
import com.paradise.network.retrofit.rest.model.RestModel
import com.paradise.network.retrofit.shelter.model.ShelterModel

fun ParkingLotModel.toParkingLotItemCount(): Int {
    return this.response.body.totalCount.toInt()
}

fun ParkingLotModel.toParkingLotItemList(): List<parkingLotItem> {
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

fun ShelterModel.toShelterItemList(): List<shelterItem> {
    return this.response.body.items.map {
        shelterItem(
            it.latitude, it.longitude, it.lnmadr, it.roadRouteDrc, it.signguNm
        )
    }
}

fun RestModel.toRestItemList(): List<restItem> {
    return this.response.body.items.map {
        restItem(
            it.latitude, it.longitude, it.roadRouteNm, it.roadRouteDrc
        )
    }
}