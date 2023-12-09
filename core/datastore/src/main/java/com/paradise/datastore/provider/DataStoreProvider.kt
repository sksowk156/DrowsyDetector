package com.paradise.datastore.provider

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStoreProvider(private val dataStore: DataStore<Preferences>) {
    suspend fun setBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    fun getBoolean(key: String): Flow<Boolean> {
        val preferencesKey = booleanPreferencesKey(key)
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[preferencesKey] ?: true
        }
    }

    suspend fun setString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    fun getString(key: String): Flow<String> {
        val preferencesKey = stringPreferencesKey(key)
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[preferencesKey] ?: ""
        }
    }

    suspend fun setInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    fun getInt(key: String): Flow<Int> {
        val preferencesKey = intPreferencesKey(key)
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[preferencesKey] ?: 0
        }
    }
}