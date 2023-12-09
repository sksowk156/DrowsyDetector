package com.paradise.data.repositoryImpl

import com.paradise.data.repository.SettingRepository
import com.paradise.datastore.provider.DataStoreProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingRepositoryImpl @Inject constructor(private val dataStoreProvider: DataStoreProvider) :
    SettingRepository {
    override suspend fun setBoolean(key: String, value: Boolean) {
        dataStoreProvider.setBoolean(key, value)
    }

    override fun getBoolean(key: String): Flow<Boolean> = dataStoreProvider.getBoolean(key)
    override suspend fun setString(key: String, value: String) {
        dataStoreProvider.setString(key, value)
    }

    override fun getString(key: String): Flow<String> = dataStoreProvider.getString(key)
    override suspend fun setInt(key: String, value: Int) {
        dataStoreProvider.setInt(key, value)
    }

    override fun getInt(key: String): Flow<Int> = dataStoreProvider.getInt(key)


}