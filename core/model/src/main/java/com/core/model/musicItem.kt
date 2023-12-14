package com.core.model

data class musicItem(
    val title: String? = "",
    val newPath: String? = "",
    var startTime: Long = 0L, // 초기엔 0초부터 시작
    var durationTime: Long = 5000L, // 기본 3초 -> 후에 변경 가능하게 변경할 예정
    val originalPath: String? = "",
    val id: Int = 0,    // 음원 자체의 ID
)