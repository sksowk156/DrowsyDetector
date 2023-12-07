package com.paradise.statistic.navi

import com.paradise.common_ui.navicontract.FeatureStatisticContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object FeatureStatisticModule {
    @Provides
    fun providesFeatureStatisticContract(): FeatureStatisticContract =
        FeatureStatisticContractImpl()
}