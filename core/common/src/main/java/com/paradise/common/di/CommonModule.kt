package com.paradise.common.di
//
//import android.content.Context
//import android.location.Geocoder
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.paradise.common.helper.LocationHelper
//import com.paradise.common.helper.ToastHelper
//import com.paradise.common.helper.impl.LocationHelperImpl
//import com.paradise.common.helper.impl.ToastHelperImpl
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import java.util.Locale
//import javax.inject.Singleton
//
//@InstallIn(SingletonComponent::class)
//@Module
//object CommonModule {
//    @Provides
//    @Singleton
//    fun provideToastHelper(@ApplicationContext context: Context): ToastHelper =
//        ToastHelperImpl(context)
//
//    @Provides
//    @Singleton
//    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
//        LocationServices.getFusedLocationProviderClient(context)
//
//    @Provides
//    @Singleton
//    fun provideGeocoder(@ApplicationContext context: Context) = Geocoder(context, Locale.KOREA)
//
//    @Provides
//    @Singleton
//    fun provideLocationHelper(
//        fusedLocationProviderClient: FusedLocationProviderClient,
//        geocoder: Geocoder,
//    ): LocationHelper = LocationHelperImpl(fusedLocationProviderClient, geocoder)
//}