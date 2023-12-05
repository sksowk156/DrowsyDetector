package com.paradise.analyze

import com.paradise.common_ui.navigation.FeatureAnalyzeContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(FragmentComponent::class)
object FeatureAnalyzeModule {
    @Provides
    fun providesFeatureAnalyzeContract(): FeatureAnalyzeContract = FeatureAnalyzeContractImpl()
}