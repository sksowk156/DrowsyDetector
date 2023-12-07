package com.paradise.common_ui.navicontract

import androidx.navigation.NavController

interface FeatureHomeContract {
    fun show(dataToPass: String, navController: NavController)
}