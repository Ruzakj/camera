package com.ric.camera

import androidx.camera.core.ImageProxy

/**
 * Snapshots immutable metadata from a CameraX frame without retaining ImageProxy.
 *
 * Exposure metadata is supplied by the capture backend when available. Keeping it
 * optional preserves a capability-safe fallback for devices or paths that cannot
 * expose Camera2 capture-result metadata.
 */
object CameraXComputationalFrameDescriptorAdapter {
    fun snapshot(
        frameId: Long,
        image: ImageProxy,
        exposure: ComputationalExposureMetadata = ComputationalExposureMetadata.Unavailable,
    ): ComputationalFrameDescriptor? = ComputationalFrameDescriptor.create(
        frameId = frameId,
        timestampNanos = image.imageInfo.timestamp,
        exposureTimeNanos = exposure.exposureTimeNanos,
        sensitivityIso = exposure.sensitivityIso,
    )
}
