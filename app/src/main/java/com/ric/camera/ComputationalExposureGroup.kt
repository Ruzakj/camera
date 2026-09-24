package com.ric.camera

/**
 * Capability-safe exposure classification for future HDR/Night frame selection.
 *
 * Frames without complete exposure metadata remain usable, but are deliberately
 * classified as [Unknown] instead of being guessed into an exposure bracket.
 */
object ComputationalExposureGroup {
    enum class Bucket {
        Unknown,
        Short,
        Reference,
        Long,
    }

    fun classify(
        descriptor: ComputationalFrameDescriptor,
        referenceExposureTimeNanos: Long,
    ): Bucket {
        val exposure = descriptor.exposureTimeNanos ?: return Bucket.Unknown
        if (referenceExposureTimeNanos <= 0L) return Bucket.Unknown

        return when {
            exposure <= referenceExposureTimeNanos / 2L -> Bucket.Short
            exposure >= safeDouble(referenceExposureTimeNanos) -> Bucket.Long
            else -> Bucket.Reference
        }
    }

    private fun safeDouble(value: Long): Long =
        if (value > Long.MAX_VALUE / 2L) Long.MAX_VALUE else value * 2L
}
