package com.paradise.home.navi

import com.paradise.common_ui.navicontract.FeatureHomeContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureHomeModule {
    @Singleton
    @Provides
    fun providesFeatureHomeContract(): FeatureHomeContract = FeatureHomeContractImpl()
}