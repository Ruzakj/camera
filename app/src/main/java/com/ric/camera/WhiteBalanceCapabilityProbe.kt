package com.ric.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo

/** Reads the AWB modes actually advertised by the bound camera. */
object WhiteBalanceCapabilityProbe {
    fun from(cameraInfo: CameraInfo): WhiteBalanceCapabilities = runCatching {
        val modes = Camera2CameraInfo.from(cameraInfo)
            .getCameraCharacteristic(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
            ?.toSet()
            .orEmpty()

        if (modes.isEmpty()) {
            WhiteBalanceCapabilities.Unsupported
        } else {
            WhiteBalanceCapabilities(supportedAwbModes = modes)
        }
    }.getOrDefault(WhiteBalanceCapabilities.Unsupported)
}
