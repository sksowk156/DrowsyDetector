package com.paradise.home.navi

import com.paradise.common_ui.navicontract.FeatureHomeContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object FeatureHomeModule {
    @Provides
    fun providesFeatureHomeContract(): FeatureHomeContract = FeatureHomeContractImpl()
}