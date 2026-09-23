package com.ric.camera

/**
 * Immutable metadata snapshot for a computational frame.
 *
 * Keeping descriptor data independent from ImageProxy lets future HDR/Night stages
 * inspect frame identity and capture timing without extending CameraX resource lifetime.
 */
data class ComputationalFrameDescriptor(
    val frameId: Long,
    val timestampNanos: Long,
) {
    companion object {
        fun create(
            frameId: Long,
            timestampNanos: Long,
        ): ComputationalFrameDescriptor? {
            if (frameId < 0L || timestampNanos < 0L) return null
            return ComputationalFrameDescriptor(
                frameId = frameId,
                timestampNanos = timestampNanos,
            )
        }
    }
}
