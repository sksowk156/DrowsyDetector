package com.paradise.network.mapper

import com.core.model.parkingLotItem
import com.core.model.restItem
import com.core.model.shelterItem
import com.paradise.network.retrofit.parkinglot.model.Item as parkingLotitem
import com.paradise.network.retrofit.rest.model.Item as restitem
import com.paradise.network.retrofit.shelter.model.Item as shelteritem
fun parkingLotitem.toParkingLotItem() = parkingLotItem(
    holidayCloseOpenHhmm = this.holidayCloseOpenHhmm,
    holidayOperOpenHhmm = this.holidayOperOpenHhmm,
    latitude = this.latitude,
    lnmadr = this.lnmadr,
    longitude = this.longitude,
    parkingchrgeInfo = this.parkingchrgeInfo,
    prkplceNm = this.prkplceNm,
    satOperCloseHhmm = this.satOperCloseHhmm,
    satOperOperOpenHhmm = this.satOperOperOpenHhmm,
    weekdayOperColseHhmm = this.weekdayOperColseHhmm,
    weekdayOperOpenHhmm = this.weekdayOperOpenHhmm,
)

fun shelteritem.toShelterItem() = shelterItem(
    latitude = this.latitude,
    longitude = this.longitude,
    lnmadr = this.lnmadr,
    roadRouteDrc = this.roadRouteDrc,
    signguNm = this.signguNm,
)

fun restitem.toRestItem() = restItem(
    latitude = this.latitude,
    longitude = this.longitude,
    roadRouteNm = this.roadRouteNm,
    roadRouteDrc = this.roadRouteDrc
)

//fun ParkingLotModel.toParkingLotItemList(): List<parkingLotItem> {
//    return this.response.body.items.map {
//        it.toParkingLotItem()
//    }
//}
//
//fun ShelterModel.toShelterItemList(): List<shelterItem> {
//    return this.response.body.items.map {
//        it.toShelterItem()
//    }
//}
//
//fun RestModel.toRestItemList(): List<restItem> {
//    return this.response.body.items.map {
//        it.toRestItem()
//    }
//}
