package com.paradise.network.Mapper

import com.core.model.parkingLotItem
import com.core.model.restItem
import com.core.model.shelterItem
import com.paradise.network.retrofit.parkinglot.model.ParkingLotModel
import com.paradise.network.retrofit.rest.model.RestModel
import com.paradise.network.retrofit.shelter.model.ShelterModel
import com.paradise.network.retrofit.parkinglot.model.Item as parkingLotitem
import com.paradise.network.retrofit.rest.model.Item as restitem
import com.paradise.network.retrofit.shelter.model.Item as shelteritem

fun ParkingLotModel.toParkingLotItemCount(): Int {
    return this.response.body.totalCount.toInt()
}

fun ParkingLotModel.toParkingLotItemList(): List<parkingLotItem> {
    return this.response.body.items.map {
        it.toParkingLotItem()
    }
}

fun parkingLotitem.toParkingLotItem() = parkingLotItem(
    this.holidayCloseOpenHhmm,
    this.holidayOperOpenHhmm,
    this.latitude,
    this.lnmadr,
    this.longitude,
    this.parkingchrgeInfo,
    this.prkplceNm,
    this.satOperCloseHhmm,
    this.satOperOperOpenHhmm,
    this.weekdayOperColseHhmm,
    this.weekdayOperOpenHhmm,
)

fun ShelterModel.toShelterItemList(): List<shelterItem> {
    return this.response.body.items.map {
        it.toShelterItem()
    }
}

fun shelteritem.toShelterItem() = shelterItem(
    this.latitude, this.longitude, this.lnmadr, this.roadRouteDrc, this.signguNm
)

fun RestModel.toRestItemList(): List<restItem> {
    return this.response.body.items.map {
        it.toRestItem()
    }
}

fun restitem.toRestItem() = restItem(
    this.latitude, this.longitude, this.roadRouteNm, this.roadRouteDrc
)