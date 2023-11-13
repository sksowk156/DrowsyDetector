package com.paradise.drowsydetector.data.remote.rest

data class Rest(
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
//    val busTrnsitYn: String,
//    val crrpwrkYn: String,
//    val elctyYn: String,
//    val entrpsNm: String,
//    val etcCvntl: String,
//    val insttCode: String,
    val latitude: String,
    val longitude: String,
//    val lpgYn: String,
//    val nrsgYn: String,
//    val ocpatAr: String,
//    val oltYn: String,
//    val operCloseHhmm: String,
//    val operOpenHhmm: String,
//    val parmacyYn: String,
//    val phoneNumber: String,
//    val prkplceCo: String,
//    val referenceDate: String,
//    val restAreaType: String,
//    val roadKnd: String,
    val roadRouteDrc: String, // 도로 노선 방향
    val roadRouteNm: String, // 도로 노선명
//    val roadRouteNo: String,
//    val rprsntvRstrt: String,
//    val rstrtYn: String,
//    val shltrYn: String,
//    val shopYn: String,
//    val toiletYn: String,
)