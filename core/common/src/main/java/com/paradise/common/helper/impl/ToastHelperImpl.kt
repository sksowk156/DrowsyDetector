//package com.paradise.common.helper.impl
//
//import android.content.Context
//import android.widget.Toast
//import com.paradise.common.helper.ToastHelper
//import dagger.hilt.android.qualifiers.ApplicationContext
//import javax.inject.Inject
//
//class ToastHelperImpl @Inject constructor(
//    @ApplicationContext private val context: Context
//) : ToastHelper {
//    override fun showToast(message: String) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//    }
//}
