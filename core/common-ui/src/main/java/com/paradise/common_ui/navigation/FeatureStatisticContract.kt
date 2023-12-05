package com.paradise.common_ui.navigation

import androidx.navigation.NavController

interface FeatureStatisticContract {
    fun show(dataToPass: String, navController: NavController)
}