package com.paradise.drowsydetector.data.model

data class Body(
    val items: List<Item>,
    val numOfRows: String,
    val pageNo: String,
    val totalCount: String
)