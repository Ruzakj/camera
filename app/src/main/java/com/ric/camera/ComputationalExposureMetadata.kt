package com.ric.camera

/**
 * Immutable, capability-safe exposure snapshot for computational frames.
 *
 * Camera backends may omit either value when the active device or capture path does
 * not expose it. Invalid non-positive values are normalized to null so downstream
 * HDR/Night logic can fall back instead of assuming unsupported metadata exists.
 */
data class ComputationalExposureMetadata private constructor(
    val exposureTimeNanos: Long?,
    val sensitivityIso: Int?,
) {
    companion object {
        fun create(
            exposureTimeNanos: Long?,
            sensitivityIso: Int?,
        ): ComputationalExposureMetadata = ComputationalExposureMetadata(
            exposureTimeNanos = exposureTimeNanos?.takeIf { it > 0L },
            sensitivityIso = sensitivityIso?.takeIf { it > 0 },
        )

        val Unavailable: ComputationalExposureMetadata = ComputationalExposureMetadata(
            exposureTimeNanos = null,
            sensitivityIso = null,
        )
    }
}
