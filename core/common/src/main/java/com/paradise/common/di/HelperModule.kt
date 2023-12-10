package com.paradise.common.di

import androidx.fragment.app.Fragment
import com.paradise.common.helper.CameraHelper
import com.paradise.common.helper.MusicHelper
import com.paradise.common.helper.SttHelper
import com.paradise.common.helper.SttTtsController
import com.paradise.common.helper.TtsHelper
import com.paradise.common.helper.VolumeHelper
import com.paradise.common.helper.impl.CameraHelperImpl
import com.paradise.common.helper.impl.MusicHelperImpl
import com.paradise.common.helper.impl.SttHelperImpl
import com.paradise.common.helper.impl.SttTtsControllerImpl
import com.paradise.common.helper.impl.TtsHelperImpl
import com.paradise.common.helper.impl.VolumeHelperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class, ServiceComponent::class)
object HelperModule {
    @FragmentScoped
    @Provides
    fun provideCameraHelper(fragment: Fragment): CameraHelper = CameraHelperImpl(fragment)

    @FragmentScoped
    @Provides
    fun provideMusicHelper(fragment: Fragment): MusicHelper = MusicHelperImpl(fragment)

    @FragmentScoped
    @Provides
    fun provideVolumeHelper(fragment: Fragment): VolumeHelper = VolumeHelperImpl(fragment)

    @FragmentScoped
    @Provides
    fun provideSttHelper(fragment: Fragment): SttHelper = SttHelperImpl(fragment)

    @FragmentScoped
    @Provides
    fun provideTtsHelper(fragment: Fragment): TtsHelper = TtsHelperImpl(fragment)

    @FragmentScoped
    @Provides
    fun provideSttTtsController(
        sttHelper: SttHelper,
        ttsHelper: TtsHelper,
        fragment: Fragment,
    ): SttTtsController = SttTtsControllerImpl(sttHelper, ttsHelper, fragment)

}