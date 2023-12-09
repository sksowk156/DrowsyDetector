package com.paradise.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.paradise.common.network.PREFERENCES_NAME
import com.paradise.datastore.provider.DataStoreProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatastoreModule {
    private val Context.dataStore by preferencesDataStore(PREFERENCES_NAME)

    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    fun provideDataStoreProvider(datastore: DataStore<Preferences>) = DataStoreProvider(datastore)
}

