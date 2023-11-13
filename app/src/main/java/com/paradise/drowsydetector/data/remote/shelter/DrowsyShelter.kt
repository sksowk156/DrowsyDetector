package com.paradise.drowsydetector.data.remote.shelter

data class DrowsyShelter(
    val response: Response,
)

data class Response(
    val body: Body,
    val header: Header,
)

data class Body(
    val items: List<Item>,
    val numOfRows: String,
    val pageNo: String,
    val totalCount: String,
)

data class Header(
    val resultCode: String,
    val resultMsg: String,
    val type: String,
)

data class Item(
    val cctvCo: String,
    val ctprvnNm: String,
    val etcCvntl: String,
    val institutionNm: String,
    val insttCode: String,
    val latitude: String,
    val lnmadr: String,
    val longitude: String,
    val phoneNumber: String,
    val prkplceCo: String,
    val rdnmadr: String,
    val referenceDate: String,
    val roadKnd: String,
    val roadRouteDrc: String,
    val roadRouteNm: String,
    val roadRouteNo: String,
    val shltrNm: String,
    val signguNm: String,
    val toiletYn: String,
    val totEt: String,
)