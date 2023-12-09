package com.paradise.setting.navi

import com.paradise.common_ui.navicontract.FeatureSettingContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureSettingModule {
    @Singleton
    @Provides
    fun providesFeatureSettingContract(): FeatureSettingContract = FeatureSettingContractImpl()
}