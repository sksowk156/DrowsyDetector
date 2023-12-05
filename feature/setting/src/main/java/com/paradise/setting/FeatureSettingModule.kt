package com.paradise.setting

import com.paradise.common_ui.navigation.FeatureSettingContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(FragmentComponent::class)
object FeatureSettingModule {
    @Provides
    fun providesFeatureSettingContract(): FeatureSettingContract = FeatureSettingContractImpl()
}