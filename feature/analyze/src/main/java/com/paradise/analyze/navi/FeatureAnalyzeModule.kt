package com.paradise.analyze.navi

import com.paradise.common_ui.navicontract.FeatureAnalyzeContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object FeatureAnalyzeModule {
    @Provides
    fun providesFeatureAnalyzeContract(): FeatureAnalyzeContract = FeatureAnalyzeContractImpl()
}