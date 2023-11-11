package com.paradise.drowsydetector.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class LocationService(context: Context) {

    companion object {
        @Volatile
        private var instance: LocationService? = null

        private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
        private lateinit var geocoder: Geocoder
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                // LocationSercie 객체를 생성할 때 같이 한번만 객체를 생성한다.
                geocoder = Geocoder(context, Locale.KOREA)
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)
                instance ?: LocationService(context).also { instance = it }
            }

    }

    /**
     * Set last location event listener
     * 지금 위치 확인 후 이벤트 처리
     * @param lastLocationListener
     * @receiver
     */
    @SuppressLint("MissingPermission")
    fun setLastLocationEventListener(lastLocationListener: (Location) -> Unit) {
        checkPermissions( // 위치 권한이 되어 있는지 확인
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? -> //
                    if (location != null) {
//                        Log.d("whatisthis", location.toString())
                        lastLocationListener(location)
                    }
                }
        }
    }

    /**
     * Get reverse geocoding
     * 위, 경도 정보를 주소 정보로 변환후 이벤트 처리
     * @param latitude
     * @param longitude
     * @param reverseGeocoderListener
     * @receiver
     */
    fun getReverseGeocoding(
        latitude: Double,
        longitude: Double,
        reverseGeocoderListener: (Address) -> Unit, // 가져온 주소로 이벤트 처리
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            // declare here the geocodeListener, as it requires Android API 33
            geocoder.getFromLocation(
                latitude,
                longitude,
                2,
                @RequiresApi(33) object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
//                        Log.d("whatisthis", addresses.toString())
                        if(addresses.size > 0) reverseGeocoderListener(addresses.first())
                    }

                    override fun onError(errorMessage: String?) {
                        super.onError(errorMessage)
                        Log.e("whatisthis", errorMessage.toString())
                    }
                })
        } else {
            val addresses = geocoder.getFromLocation(
                latitude,
                longitude,
                2
            )
            if (addresses != null) {
//                Log.d("whatisthis", "* " + addresses.toString())
                if(addresses.size > 0) reverseGeocoderListener(addresses.first())
            }
        }
    }
}
