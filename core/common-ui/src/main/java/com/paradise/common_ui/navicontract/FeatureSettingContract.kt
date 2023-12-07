package com.paradise.common_ui.navicontract

import androidx.navigation.NavController

interface FeatureSettingContract {
    fun show(dataToPass: String, navController: NavController)
}