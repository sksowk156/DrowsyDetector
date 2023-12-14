//package com.paradise.drowsydetector.domain.repository
//
//import kotlinx.coroutines.flow.Flow
//
//interface SettingRepository {
//
//    suspend fun setBoolean(key: String, value: Boolean)
//
//    fun getBoolean(key: String): Flow<Boolean>
//
//    suspend fun setString(key: String, value: String)
//
//    fun getString(key: String): Flow<String>
//
//    suspend fun setInt(key: String, value: Int)
//
//    fun getInt(key: String): Flow<Int>
//}