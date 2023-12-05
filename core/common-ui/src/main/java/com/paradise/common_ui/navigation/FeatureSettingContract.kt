package com.paradise.common_ui.navigation

import androidx.navigation.NavController

interface FeatureSettingContract {
    fun show(dataToPass: String, navController: NavController)
}