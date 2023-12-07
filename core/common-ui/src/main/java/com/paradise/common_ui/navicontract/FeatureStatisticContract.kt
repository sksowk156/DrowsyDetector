package com.paradise.common_ui.navicontract

import androidx.navigation.NavController

interface FeatureStatisticContract {
    fun show(dataToPass: String, navController: NavController)
}