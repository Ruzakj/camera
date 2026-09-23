package com.ric.camera

import androidx.camera.core.ImageProxy

/**
 * Atomically snapshots CameraX metadata and transfers frame ownership.
 *
 * Exposure metadata is supplied by the capture backend when available and remains
 * optional so devices without Camera2 capture-result metadata keep the safe fallback.
 *
 * After adopt() is called, callers must not close [image]. Any rejected metadata or
 * ownership transition closes the CameraX resource immediately; successful adoption
 * delegates exactly-once release to [ComputationalFrameBuffer].
 */
object CameraXComputationalOwnedFrameAdapter {
    fun adopt(
        frameId: Long,
        image: ImageProxy,
        exposure: ComputationalExposureMetadata = ComputationalExposureMetadata.Unavailable,
    ): ComputationalOwnedFrame? {
        val descriptor = CameraXComputationalFrameDescriptorAdapter.snapshot(
            frameId = frameId,
            image = image,
            exposure = exposure,
        ) ?: run {
            image.close()
            return null
        }

        val buffer = CameraXComputationalFrameAdapter.wrap(
            frameId = frameId,
            image = image,
        ) ?: return null

        return ComputationalOwnedFrame.create(
            descriptor = descriptor,
            buffer = buffer,
        ) ?: run {
            buffer.release()
            null
        }
    }
}
