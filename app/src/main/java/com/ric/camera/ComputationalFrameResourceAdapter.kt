package com.ric.camera

/**
 * Adapts a closeable frame resource into the computational ownership contract.
 *
 * The resource stays opaque so CameraX/ImageProxy is not coupled to the computational
 * layer yet. Once wrap() is called this adapter owns the resource: accepted resources
 * are released by [ComputationalFrameBuffer], while rejected metadata closes the
 * resource immediately so an invalid frame cannot leak a camera buffer.
 */
object ComputationalFrameResourceAdapter {
    fun <T : AutoCloseable> wrap(
        frameId: Long,
        timestampNanos: Long,
        resource: T,
    ): ComputationalFrameBuffer? {
        val buffer = ComputationalFrameBuffer.create(
            frameId = frameId,
            timestampNanos = timestampNanos,
            releaseAction = resource::close,
        )
        if (buffer == null) resource.close()
        return buffer
    }
}
