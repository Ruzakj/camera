package com.ric.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo

/**
 * Capability-first boundary for computational photography features.
 *
 * This checkpoint intentionally exposes hardware primitives only; processing modules can
 * opt in later without assuming RAW or manual sensor support on every device/lens.
 */
data class ComputationalPhotographyCapabilities(
    val rawCaptureSupported: Boolean,
    val manualSensorSupported: Boolean,
    val burstProcessingEligible: Boolean
) {
    companion object {
        val Unsupported = ComputationalPhotographyCapabilities(
            rawCaptureSupported = false,
            manualSensorSupported = false,
            burstProcessingEligible = false
        )
    }
}

object ComputationalPhotographyCapabilityProbe {
    fun from(cameraInfo: CameraInfo): ComputationalPhotographyCapabilities = runCatching {
        val characteristics = Camera2CameraInfo.from(cameraInfo)
        val capabilities = characteristics.getCameraCharacteristic(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
        ) ?: intArrayOf()

        val raw = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
        val manualSensor = capabilities.contains(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR
        )

        ComputationalPhotographyCapabilities(
            rawCaptureSupported = raw,
            manualSensorSupported = manualSensor,
            burstProcessingEligible = manualSensor
        )
    }.getOrDefault(ComputationalPhotographyCapabilities.Unsupported)
}
