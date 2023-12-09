package com.core.model
/**
 * Bounding box
 *
 * getBoundingBox의 결과 값
 * @property minLatitude
 * @property minLongitude
 * @property maxLatitude
 * @property maxLongitude
 * @constructor Create empty Bounding box
 */
data class BoundingBox(
    val minLatitude: Double,
    val minLongitude: Double,
    val maxLatitude: Double,
    val maxLongitude: Double,
)