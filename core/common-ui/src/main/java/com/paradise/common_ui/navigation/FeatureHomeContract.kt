package com.paradise.common_ui.navigation

import androidx.navigation.NavController

interface FeatureHomeContract {
    fun show(dataToPass: String, navController: NavController)
}