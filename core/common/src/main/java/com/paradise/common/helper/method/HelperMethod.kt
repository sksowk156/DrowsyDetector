package com.paradise.common.helper.method

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Check permission
 * TedPermission 권한 확인 및 요청 메서드
 * @param permissionList
 * @param grantedEventListener
 * @receiver
 */
fun checkPermissions(permissionList: Array<String>, grantedEventListener: () -> Unit) {
    TedPermission.create()
        .setPermissionListener(object : PermissionListener {
            //권한이 허용됐을 때
            override fun onPermissionGranted() {
                grantedEventListener()
            }

            //권한이 거부됐을 때
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
//                showToast("권한 허용 X")
            }
        })
        .setDeniedMessage("권한을 허용해주세요. [설정] > [앱 및 알림] > [고급] > [앱 권한]")
        .setPermissions(*permissionList)// 얻으려는 권한(여러개 가능)
        .check()
}
