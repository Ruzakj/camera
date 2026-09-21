package com.ric.camera

import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.impl.Camera2ImplConfig
import androidx.camera.camera2.interop.Camera2CameraControl

/**
 * Capability-safe boundary for white-balance requests.
 *
 * AWB modes are applied only when advertised by the active camera. Clearing
 * removes the Camera2 override so CameraX can resume its normal AWB path.
 */
class WhiteBalanceController(
    private val cameraController: CameraController
) {
    var requestedAwbMode: Int? = null
        private set

    val supportedModes: Set<Int>
        get() = cameraController.capabilities?.whiteBalance?.supportedAwbModes.orEmpty()

    fun requestMode(mode: Int): Boolean {
        val capabilities = cameraController.capabilities?.whiteBalance ?: return false
        if (!capabilities.supports(mode)) return false
        val camera = cameraController.camera ?: return false

        return runCatching {
            Camera2CameraControl.from(camera.cameraControl).setCaptureRequestOptions(
                Camera2ImplConfig.Builder()
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, mode)
                    .build()
            )
            requestedAwbMode = mode
        }.isSuccess
    }

    fun clear() {
        cameraController.camera?.let { camera ->
            runCatching {
                Camera2CameraControl.from(camera.cameraControl).clearCaptureRequestOptions()
            }
        }
        requestedAwbMode = null
    }
}
