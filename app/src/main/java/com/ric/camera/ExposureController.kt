package com.ric.camera

import androidx.camera.core.Camera
import androidx.camera.core.ExposureState

/**
 * Capability-safe boundary for exposure compensation.
 *
 * Manual exposure UI is intentionally not wired in this checkpoint. Keeping
 * the CameraX call here lets later controls share one guarded implementation.
 */
class ExposureController(
    private val cameraController: CameraController
) {
    val exposureState: ExposureState?
        get() = cameraController.camera?.cameraInfo?.exposureState

    fun setExposureCompensation(index: Int): Boolean {
        val camera: Camera = cameraController.camera ?: return false
        val state = camera.cameraInfo.exposureState
        if (!state.isExposureCompensationSupported) return false

        val safeIndex = index.coerceIn(
            state.exposureCompensationRange.lower,
            state.exposureCompensationRange.upper
        )
        camera.cameraControl.setExposureCompensationIndex(safeIndex)
        return true
    }
}
