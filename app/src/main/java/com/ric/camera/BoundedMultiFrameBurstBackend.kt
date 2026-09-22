package com.ric.camera

/**
 * Minimal verified burst capability implementation.
 *
 * This backend only advertises bounded multi-frame support. It deliberately
 * performs no capture work, so the production ImageCapture path remains
 * unchanged until a real burst execution path is verified separately.
 */
class BoundedMultiFrameBurstBackend(
    private val maxFrameCount: Int,
    override val isAvailable: Boolean
) : MultiFrameBurstBackend {

    override fun supports(frameCount: Int): Boolean =
        isAvailable && maxFrameCount > 1 && frameCount in 2..maxFrameCount
}
