package com.ric.camera

/**
 * Capability-first factory for the bounded burst backend.
 *
 * Hardware that is not eligible for computational burst processing receives
 * the explicit unavailable backend. This still performs no capture work; it
 * only wires verified capability state into backend availability.
 */
object MultiFrameBurstBackendFactory {
    fun from(
        capabilities: ComputationalPhotographyCapabilities,
        maxFrameCount: Int = 3
    ): MultiFrameBurstBackend {
        if (!capabilities.burstProcessingEligible || maxFrameCount < 2) {
            return UnavailableMultiFrameBurstBackend
        }

        return BoundedMultiFrameBurstBackend(
            maxFrameCount = maxFrameCount,
            isAvailable = true
        )
    }
}
