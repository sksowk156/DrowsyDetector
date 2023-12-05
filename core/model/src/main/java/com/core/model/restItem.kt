package com.core.model

data class restItem(
    val latitude: String,
    val longitude: String,
    val roadRouteDrc: String, // 도로 노선 방향
    val roadRouteNm: String, // 도로 노선명
)
