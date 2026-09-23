package com.ric.camera

import android.hardware.camera2.CaptureResult

/**
 * Snapshots optional Camera2 exposure metadata from a capture result without retaining
 * camera resources.
 *
 * Callers that do not have a Camera2 CaptureResult should use
 * [ComputationalExposureMetadata.Unavailable], keeping computational processing
 * capability-safe on devices/backends that omit sensor metadata.
 */
object Camera2ComputationalExposureMetadataAdapter {
    fun snapshot(captureResult: CaptureResult?): ComputationalExposureMetadata {
        if (captureResult == null) return ComputationalExposureMetadata.Unavailable

        return ComputationalExposureMetadata.create(
            exposureTimeNanos = captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME),
            sensitivityIso = captureResult.get(CaptureResult.SENSOR_SENSITIVITY),
        )
    }
}
