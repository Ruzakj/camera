package com.ric.camera

import androidx.camera.core.ImageProxy

/**
 * Snapshots immutable metadata from a CameraX frame without retaining ImageProxy.
 *
 * Exposure metadata remains optional until a capture-result backed source is wired.
 * This boundary deliberately copies only metadata already exposed by ImageProxy so
 * callers can inspect frame identity/timing after the CameraX resource is released.
 */
object CameraXComputationalFrameDescriptorAdapter {
    fun snapshot(
        frameId: Long,
        image: ImageProxy,
    ): ComputationalFrameDescriptor? = ComputationalFrameDescriptor.create(
        frameId = frameId,
        timestampNanos = image.imageInfo.timestamp,
    )
}
