package com.ric.camera

import androidx.camera.core.ImageProxy

/**
 * CameraX boundary for computational frame ownership.
 *
 * Once wrap() is called, ownership of [image] transfers to the computational frame
 * contract. Valid frames are closed by [ComputationalFrameBuffer.release]; rejected
 * metadata is closed immediately by [ComputationalFrameResourceAdapter].
 *
 * This adapter intentionally exposes no pixel planes yet, keeping HDR/Night processing
 * disabled until frame lifetime semantics are verified independently.
 */
object CameraXComputationalFrameAdapter {
    fun wrap(
        frameId: Long,
        image: ImageProxy,
    ): ComputationalFrameBuffer? = ComputationalFrameResourceAdapter.wrap(
        frameId = frameId,
        timestampNanos = image.imageInfo.timestamp,
        resource = ImageProxyCloseable(image),
    )

    private class ImageProxyCloseable(
        private val image: ImageProxy,
    ) : AutoCloseable {
        override fun close() = image.close()
    }
}
