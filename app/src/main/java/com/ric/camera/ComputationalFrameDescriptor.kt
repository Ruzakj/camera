package com.ric.camera

/**
 * Immutable metadata snapshot for a computational frame.
 *
 * Keeping descriptor data independent from ImageProxy lets future HDR/Night stages
 * inspect frame identity, capture timing, and optional exposure metadata without
 * extending CameraX resource lifetime.
 */
data class ComputationalFrameDescriptor(
    val frameId: Long,
    val timestampNanos: Long,
    val exposureTimeNanos: Long? = null,
    val sensitivityIso: Int? = null,
) {
    companion object {
        fun create(
            frameId: Long,
            timestampNanos: Long,
            exposureTimeNanos: Long? = null,
            sensitivityIso: Int? = null,
        ): ComputationalFrameDescriptor? {
            if (frameId < 0L || timestampNanos < 0L) return null
            if (exposureTimeNanos != null && exposureTimeNanos <= 0L) return null
            if (sensitivityIso != null && sensitivityIso <= 0) return null

            return ComputationalFrameDescriptor(
                frameId = frameId,
                timestampNanos = timestampNanos,
                exposureTimeNanos = exposureTimeNanos,
                sensitivityIso = sensitivityIso,
            )
        }
    }
}
