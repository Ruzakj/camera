package com.ric.camera

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExposureState

/**
 * Immutable snapshot of capabilities exposed by the currently bound CameraX camera.
 * Keep UI and future controllers capability-first instead of assuming hardware support.
 */
data class CameraCapabilities(
    val lensFacing: Int,
    val hasFlashUnit: Boolean,
    val minZoomRatio: Float,
    val maxZoomRatio: Float,
    val exposureCompensationSupported: Boolean,
    val exposureCompensationMinIndex: Int,
    val exposureCompensationMaxIndex: Int,
    val exposureCompensationStep: Float,
    val manualExposure: ManualExposureCapabilities = ManualExposureCapabilities.Unsupported,
    val whiteBalance: WhiteBalanceCapabilities = WhiteBalanceCapabilities.Unsupported
) {
    companion object {
        fun from(camera: Camera, lensFacing: Int): CameraCapabilities {
            val zoomState = camera.cameraInfo.zoomState.value
            val exposure = camera.cameraInfo.exposureState
            val range = exposure.exposureCompensationRange
            return CameraCapabilities(
                lensFacing = lensFacing,
                hasFlashUnit = camera.cameraInfo.hasFlashUnit(),
                minZoomRatio = zoomState?.minZoomRatio ?: 1f,
                maxZoomRatio = zoomState?.maxZoomRatio ?: 1f,
                exposureCompensationSupported = exposure.isSupported(),
                exposureCompensationMinIndex = range.lower,
                exposureCompensationMaxIndex = range.upper,
                exposureCompensationStep = exposure.exposureCompensationStep.toFloat(),
                manualExposure = ManualExposureCapabilityProbe.from(camera.cameraInfo),
                whiteBalance = WhiteBalanceCapabilityProbe.from(camera.cameraInfo)
            )
        }
    }
}

private fun ExposureState.isSupported(): Boolean {
    val range = exposureCompensationRange
    return !(range.lower == 0 && range.upper == 0)
}

fun CameraCapabilities.isBackCamera(): Boolean = lensFacing == CameraSelector.LENS_FACING_BACK
