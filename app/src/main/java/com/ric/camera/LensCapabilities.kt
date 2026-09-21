package com.ric.camera

import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider

/**
 * Snapshot of lens facings that CameraX can actually bind on this device.
 *
 * Keeps lens switching capability-first so UI/controller code can avoid
 * requesting a camera that is not exposed by the current provider.
 */
data class LensCapabilities(
    val hasBack: Boolean,
    val hasFront: Boolean,
) {
    fun supports(lensFacing: Int): Boolean = when (lensFacing) {
        CameraSelector.LENS_FACING_BACK -> hasBack
        CameraSelector.LENS_FACING_FRONT -> hasFront
        else -> false
    }

    fun alternateFor(currentLensFacing: Int): Int? = when (currentLensFacing) {
        CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT.takeIf(::supports)
        CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK.takeIf(::supports)
        else -> null
    }

    companion object {
        fun from(provider: ProcessCameraProvider): LensCapabilities = LensCapabilities(
            hasBack = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA),
            hasFront = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA),
        )
    }
}
