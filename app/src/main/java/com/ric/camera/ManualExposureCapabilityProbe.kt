package com.ric.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo

/**
 * Reads manual-exposure limits from the Camera2 characteristics backing CameraX.
 * Missing or invalid characteristics deliberately degrade to unsupported.
 */
object ManualExposureCapabilityProbe {
    fun from(cameraInfo: CameraInfo): ManualExposureCapabilities {
        return runCatching {
            val camera2Info = Camera2CameraInfo.from(cameraInfo)
            val iso = camera2Info.getCameraCharacteristic(
                CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE
            )
            val shutter = camera2Info.getCameraCharacteristic(
                CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE
            )

            ManualExposureCapabilities(
                isoRange = iso?.takeIf { it.lower <= it.upper }?.let { it.lower..it.upper },
                shutterTimeRangeNs = shutter?.takeIf { it.lower <= it.upper }
                    ?.let { it.lower..it.upper }
            )
        }.getOrDefault(ManualExposureCapabilities.Unsupported)
    }
}
