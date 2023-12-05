package com.paradise.statistic

import com.paradise.common_ui.navigation.FeatureStatisticContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(FragmentComponent::class)
object FeatureStatisticModule {
    @Provides
    fun providesFeatureStatisticContract(): FeatureStatisticContract =
        FeatureStatisticContractImpl()
}