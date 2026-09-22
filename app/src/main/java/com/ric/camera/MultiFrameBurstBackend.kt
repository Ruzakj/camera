package com.ric.camera

/**
 * Capability-safe contract for a future verified multi-frame burst backend.
 *
 * This boundary does not alter the production ImageCapture path. Backends must
 * explicitly report availability before a multi-frame request can be accepted.
 */
interface MultiFrameBurstBackend {
    val isAvailable: Boolean

    fun supports(frameCount: Int): Boolean = isAvailable && frameCount > 1
}

/** Production-safe default until a real burst backend is verified. */
object UnavailableMultiFrameBurstBackend : MultiFrameBurstBackend {
    override val isAvailable: Boolean = false
}
