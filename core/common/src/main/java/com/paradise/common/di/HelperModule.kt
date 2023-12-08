package com.paradise.common.di

//import androidx.fragment.app.Fragment
//import androidx.lifecycle.LifecycleOwner
//import com.paradise.common.helper.CameraHelper
//import com.paradise.common.helper.MusicHelper
//import com.paradise.common.helper.SttHelper
//import com.paradise.common.helper.SttService
//import com.paradise.common.helper.SttTtsController
//import com.paradise.common.helper.TtsHelper
//import com.paradise.common.helper.VolumeHelper
//import com.paradise.common.helper.impl.CameraHelperImpl
//import com.paradise.common.helper.impl.MusicHelperImpl
//import com.paradise.common.helper.impl.SttHelperImpl
//import com.paradise.common.helper.impl.SttTtsControllerImpl
//import com.paradise.common.helper.impl.TtsHelperImpl
//import com.paradise.common.helper.impl.VolumeHelperImpl
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.components.FragmentComponent
//import dagger.hilt.android.scopes.FragmentScoped
//
//@InstallIn(FragmentComponent::class)
//@Module
//object HelperModule {
//    @FragmentScoped
//    @Provides
//    fun provideCameraHelper(fragment: Fragment): CameraHelper = CameraHelperImpl(fragment)
//
//    @FragmentScoped
//    @Provides
//    fun provideMusicHelper(fragment: Fragment): MusicHelper = MusicHelperImpl(fragment)
//
//    @FragmentScoped
//    @Provides
//    fun provideSttHelper(fragment: Fragment): SttHelper = SttHelperImpl(fragment)
//
//    @FragmentScoped
//    @Provides
//    fun provideTtsHelper(fragment: Fragment): TtsHelper = TtsHelperImpl(fragment)
//
//    @FragmentScoped
//    @Provides
//    fun provideSttTtsController(
//        fragment: Fragment,
//    ): SttTtsController = SttTtsControllerImpl(fragment)
//
//    @FragmentScoped
//    @Provides
//    fun provideVolumeHelper(fragment: Fragment): VolumeHelper = VolumeHelperImpl(fragment)
//}