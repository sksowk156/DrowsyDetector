package com.paradise.common.helper

import android.annotation.SuppressLint
import android.location.Address
import android.location.Location

interface LocationHelper {
    @SuppressLint("MissingPermission")
    fun setLastLocationEventListener(lastLocationListener: (Location) -> Unit)

    fun getReverseGeocoding(
        latitude: Double,
        longitude: Double,
        reverseGeocoderListener: (Address) -> Unit,
    )
}