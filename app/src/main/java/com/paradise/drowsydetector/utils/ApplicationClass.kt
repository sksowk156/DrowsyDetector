package com.paradise.drowsydetector.utils

import android.app.Application

class ApplicationClass : Application() {
    companion object {
        private lateinit var appInstance: ApplicationClass
        fun getApplicationContext(): ApplicationClass = appInstance
    }
    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }
}