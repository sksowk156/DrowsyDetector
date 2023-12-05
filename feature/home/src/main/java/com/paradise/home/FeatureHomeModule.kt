package com.paradise.home

import com.paradise.common_ui.navigation.FeatureHomeContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(FragmentComponent::class)
object FeatureHomeModule {
    @Provides
    fun providesFeatureHomeContract(): FeatureHomeContract = FeatureHomeContractImpl()
}