package com.ric.camera

/**
 * Minimal verified bounded burst implementation.
 *
 * Execution is capability-gated and synchronous: the supplied frame callback is
 * invoked exactly [frameCount] times only when the complete request is supported.
 * Unsupported requests return false without touching the proven single-frame path.
 */
class BoundedMultiFrameBurstBackend(
    private val maxFrameCount: Int,
    override val isAvailable: Boolean
) : MultiFrameBurstBackend {

    override fun supports(frameCount: Int): Boolean =
        isAvailable && maxFrameCount > 1 && frameCount in 2..maxFrameCount

    override fun execute(frameCount: Int, captureFrame: () -> Unit): Boolean {
        if (!supports(frameCount)) return false

        repeat(frameCount) {
            captureFrame()
        }
        return true
    }
}
