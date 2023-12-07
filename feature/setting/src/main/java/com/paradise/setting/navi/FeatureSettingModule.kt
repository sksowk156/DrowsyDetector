package com.paradise.setting.navi

import com.paradise.common_ui.navicontract.FeatureSettingContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object FeatureSettingModule {
    @Provides
    fun providesFeatureSettingContract(): FeatureSettingContract = FeatureSettingContractImpl()
}