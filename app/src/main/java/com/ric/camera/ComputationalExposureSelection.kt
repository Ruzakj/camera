package com.ric.camera

/**
 * Capability-safe exposure selection for future HDR/Night processing.
 *
 * Known brackets are exposed only when exposure metadata can be classified.
 * Unknown frames are preserved separately so callers can keep their existing
 * fallback path instead of guessing a bracket.
 */
object ComputationalExposureSelection {
    data class Result(
        val short: List<ComputationalFrameDescriptor>,
        val reference: List<ComputationalFrameDescriptor>,
        val long: List<ComputationalFrameDescriptor>,
        val unknown: List<ComputationalFrameDescriptor>,
    ) {
        val hasCompleteBracket: Boolean
            get() = short.isNotEmpty() && reference.isNotEmpty() && long.isNotEmpty()
    }

    fun select(
        frames: List<ComputationalFrameDescriptor>,
        referenceExposureTimeNanos: Long,
    ): Result {
        val short = mutableListOf<ComputationalFrameDescriptor>()
        val reference = mutableListOf<ComputationalFrameDescriptor>()
        val long = mutableListOf<ComputationalFrameDescriptor>()
        val unknown = mutableListOf<ComputationalFrameDescriptor>()

        frames.forEach { frame ->
            when (ComputationalExposureGroup.classify(frame, referenceExposureTimeNanos)) {
                ComputationalExposureGroup.Bucket.Short -> short += frame
                ComputationalExposureGroup.Bucket.Reference -> reference += frame
                ComputationalExposureGroup.Bucket.Long -> long += frame
                ComputationalExposureGroup.Bucket.Unknown -> unknown += frame
            }
        }

        return Result(
            short = short,
            reference = reference,
            long = long,
            unknown = unknown,
        )
    }
}
