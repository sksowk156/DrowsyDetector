package com.paradise.common.di

import android.app.Service
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import com.paradise.common.helper.MusicServiceHelper
import com.paradise.common.helper.impl.MusicServiceHelperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
object ServiceModule {
    // 새로 추가한 provideLifecycleService 메서드
    @ServiceScoped
    @Provides
    fun provideLifecycleService(service: Service): LifecycleService = service as LifecycleService

    @ServiceScoped
    @Provides
    fun provideServiceContext(lifecycleService: LifecycleService): Context = lifecycleService

    @ServiceScoped
    @Provides
    fun provideServiceLifecycle(lifecycleService: LifecycleService): LifecycleOwner =
        lifecycleService

    @ServiceScoped
    @Provides
    fun provideMusicHelperService(
        context: Context, lifecycleService: LifecycleService,
    ): MusicServiceHelper = MusicServiceHelperImpl(context, lifecycleService)
}