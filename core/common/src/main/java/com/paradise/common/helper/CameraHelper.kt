package com.paradise.common.helper

import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.common.network.LEFT_RIGHT_ANGLE_THREDHOLD
import com.paradise.common.network.UP_DOWN_ANGLE_THREDHOLD

interface CameraHelper {
    fun initCameraHelper()
    fun stopCameraHelper()
    fun releaseCameraHelper()
    fun startAnalyze(previewView: PreviewView, analyzedData: (Face, List<FaceMeshPoint>) -> Unit)
    fun startCamera2(previewView: PreviewView, analyzedData: (Face, List<FaceMeshPoint>) -> Unit)
    fun calRatio(upDownAngle: Float, leftRightAngle: Float, landmark: List<FaceMeshPoint>): Double
    fun calDist(point1: PointF3D, point2: PointF3D): Double
    fun checkHeadAngleInNoStandard(upDownAngle: Float, leftRightAngle: Float): Boolean
    fun isInLeftRight(leftRightAngle: Float): Boolean
    fun checkHeadAngleInStandard(leftRightAngle: Float, upDownAngle: Float): Boolean

}