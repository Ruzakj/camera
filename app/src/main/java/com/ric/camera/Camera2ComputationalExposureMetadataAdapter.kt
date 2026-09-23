package com.ric.camera

import android.hardware.camera2.CaptureResult
import androidx.camera.camera2.interop.Camera2CameraCaptureResult
import androidx.camera.core.ImageProxy

/**
 * Snapshots optional Camera2 exposure metadata without retaining the ImageProxy.
 *
 * Non-Camera2 or incomplete capture results degrade to Unavailable so computational
 * processing remains capability-safe on devices/backends that omit sensor metadata.
 */
object Camera2ComputationalExposureMetadataAdapter {
    fun snapshot(image: ImageProxy): ComputationalExposureMetadata {
        val camera2Result = image.imageInfo.cameraCaptureResult as? Camera2CameraCaptureResult
            ?: return ComputationalExposureMetadata.Unavailable
        val captureResult = camera2Result.captureResult

        return ComputationalExposureMetadata.create(
            exposureTimeNanos = captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME),
            sensitivityIso = captureResult.get(CaptureResult.SENSOR_SENSITIVITY),
        )
    }
}
