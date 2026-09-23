package com.ric.camera

/**
 * Adapts a closeable frame resource into the computational ownership contract.
 *
 * The resource stays opaque so CameraX/ImageProxy is not coupled to the computational
 * layer yet. Closing is delegated to [ComputationalFrameBuffer], which guarantees the
 * release action is invoked at most once.
 */
object ComputationalFrameResourceAdapter {
    fun <T : AutoCloseable> wrap(
        frameId: Long,
        timestampNanos: Long,
        resource: T,
    ): ComputationalFrameBuffer? =
        ComputationalFrameBuffer.create(
            frameId = frameId,
            timestampNanos = timestampNanos,
            releaseAction = resource::close,
        )
}
