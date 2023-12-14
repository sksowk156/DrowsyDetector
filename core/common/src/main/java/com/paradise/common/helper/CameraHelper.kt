package com.paradise.common.helper

import androidx.camera.view.PreviewView
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.facemesh.FaceMeshPoint

interface CameraHelper {
    fun initCameraHelper()
    fun stopCameraHelper()
    fun releaseCameraHelper()
    fun startAnalyze(previewView: PreviewView, analyzedData: (Face, List<FaceMeshPoint>) -> Unit)
    fun startCamera2(previewView: PreviewView, analyzedData: (Face, List<FaceMeshPoint>) -> Unit)
    fun calRatio(upDownAngle: Float, leftRightAngle: Float, landmark: List<FaceMeshPoint>): Double
    fun checkHeadAngleInNoStandard(upDownAngle: Float, leftRightAngle: Float): Boolean
    fun isInLeftRight(leftRightAngle: Float) : Boolean
    fun checkHeadAngleInStandard(leftRightAngle: Float, upDownAngle: Float) : Boolean
}