package com.paradise.common_ui.navicontract
import androidx.navigation.NavController

interface FeatureAnalyzeContract {
    fun show(dataToPass: String, navController: NavController)
}