package com.ric.camera

import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import java.util.concurrent.TimeUnit

/**
 * Focus boundary for the currently bound CameraX camera.
 *
 * Keeps tap-to-focus capability-safe: when no camera is attached the request
 * is ignored rather than leaking lifecycle state into the UI layer.
 */
class FocusController(
    private val cameraController: CameraController,
) {
    fun focus(point: MeteringPoint): Boolean {
        val camera = cameraController.camera ?: return false
        val action = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
        )
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()

        camera.cameraControl.startFocusAndMetering(action)
        return true
    }

    fun cancel(): Boolean {
        val camera = cameraController.camera ?: return false
        camera.cameraControl.cancelFocusAndMetering()
        return true
    }
}
