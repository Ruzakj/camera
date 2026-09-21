package com.ric.camera

import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import android.hardware.camera2.CaptureRequest

/**
 * Small capability-safe boundary for AE/AF locking.
 *
 * Lock requests are ignored when no camera is bound. Clearing removes only
 * this controller's Camera2 overrides so normal CameraX auto behavior resumes.
 */
class AeAfLockController(
    private val cameraController: CameraController,
) {
    fun lock(): Boolean {
        val camera = cameraController.camera ?: return false
        val camera2Control = Camera2CameraControl.from(camera.cameraControl)
        val options = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, true)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
            .build()
        camera2Control.setCaptureRequestOptions(options)
        return true
    }

    fun clear(): Boolean {
        val camera = cameraController.camera ?: return false
        Camera2CameraControl.from(camera.cameraControl).clearCaptureRequestOptions()
        return true
    }
}
