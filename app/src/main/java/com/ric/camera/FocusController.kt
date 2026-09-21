package com.ric.camera

import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import java.util.concurrent.TimeUnit

/**
 * Focus/metering boundary for the currently bound CameraX camera.
 *
 * Keeps metering capability-safe: when no camera is attached the request is
 * ignored rather than leaking lifecycle state into the UI layer. Persistent
 * requests intentionally skip auto-cancel so AE/AF lock can be applied after
 * the metering point has settled.
 */
class FocusController(
    private val cameraController: CameraController,
) {
    fun focus(point: MeteringPoint): Boolean = meter(point, persistent = false)

    fun meter(point: MeteringPoint, persistent: Boolean): Boolean {
        val camera = cameraController.camera ?: return false
        val builder = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
        )
        if (!persistent) {
            builder.setAutoCancelDuration(3, TimeUnit.SECONDS)
        }

        camera.cameraControl.startFocusAndMetering(builder.build())
        return true
    }

    fun cancel(): Boolean {
        val camera = cameraController.camera ?: return false
        camera.cameraControl.cancelFocusAndMetering()
        return true
    }
}
