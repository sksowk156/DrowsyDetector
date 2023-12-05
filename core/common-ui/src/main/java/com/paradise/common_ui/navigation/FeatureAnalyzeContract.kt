package com.paradise.common_ui.navigation
import androidx.navigation.NavController

interface FeatureAnalyzeContract {
    fun show(dataToPass: String, navController: NavController)
}