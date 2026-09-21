package com.ric.camera

import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.Camera2CameraControl

/**
 * Capability-safe boundary for manual shutter-time requests.
 *
 * Manual shutter time is applied only when the active camera advertises a
 * valid exposure-time range. Clearing the request removes Camera2 overrides so
 * CameraX can return to its normal automatic-exposure path.
 */
class ShutterController(
    private val cameraController: CameraController
) {
    var requestedShutterTimeNs: Long? = null
        private set

    val supportedRangeNs: LongRange?
        get() = cameraController.capabilities?.manualExposure?.shutterTimeRangeNs

    fun requestShutterTimeNs(exposureTimeNs: Long): Boolean {
        val range = supportedRangeNs ?: return false
        val camera = cameraController.camera ?: return false
        val safeExposureTimeNs = exposureTimeNs.coerceIn(range.first, range.last)

        return runCatching {
            val camera2Control = Camera2CameraControl.from(camera.cameraControl)
            camera2Control.setCaptureRequestOptions(
                androidx.camera.camera2.impl.Camera2ImplConfig.Builder()
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                    .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, safeExposureTimeNs)
                    .build()
            )
            requestedShutterTimeNs = safeExposureTimeNs
        }.isSuccess
    }

    fun clear() {
        cameraController.camera?.let { camera ->
            runCatching {
                Camera2CameraControl.from(camera.cameraControl).clearCaptureRequestOptions()
            }
        }
        requestedShutterTimeNs = null
    }
}
