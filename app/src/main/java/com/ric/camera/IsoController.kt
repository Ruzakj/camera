package com.ric.camera

import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.Camera2CameraControl

/**
 * Capability-safe boundary for manual ISO requests.
 *
 * Manual ISO is applied only when the active camera advertises a valid range.
 * Clearing the request removes the Camera2 overrides so CameraX can return to
 * its normal automatic-exposure path.
 */
class IsoController(
    private val cameraController: CameraController
) {
    var requestedIso: Int? = null
        private set

    val supportedRange: IntRange?
        get() = cameraController.capabilities?.manualExposure?.isoRange

    fun requestIso(iso: Int): Boolean {
        val range = supportedRange ?: return false
        val camera = cameraController.camera ?: return false
        val safeIso = iso.coerceIn(range.first, range.last)

        return runCatching {
            val camera2Control = Camera2CameraControl.from(camera.cameraControl)
            camera2Control.setCaptureRequestOptions(
                androidx.camera.camera2.impl.Camera2ImplConfig.Builder()
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                    .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, safeIso)
                    .build()
            )
            requestedIso = safeIso
        }.isSuccess
    }

    fun clear() {
        cameraController.camera?.let { camera ->
            runCatching {
                Camera2CameraControl.from(camera.cameraControl).clearCaptureRequestOptions()
            }
        }
        requestedIso = null
    }
}
