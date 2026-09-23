package com.ric.camera

/**
 * Minimal verified bounded burst implementation.
 *
 * Execution is capability-gated and synchronous. Progress is reported so callers can
 * distinguish a clean rejection from a partial burst and avoid unsafe extra fallback frames.
 */
class BoundedMultiFrameBurstBackend(
    private val maxFrameCount: Int,
    override val isAvailable: Boolean
) : MultiFrameBurstBackend {

    override fun supports(frameCount: Int): Boolean =
        isAvailable && maxFrameCount > 1 && frameCount in 2..maxFrameCount

    override fun execute(
        frameCount: Int,
        captureFrame: () -> Unit,
    ): MultiFrameBurstBackend.ExecutionResult {
        if (!supports(frameCount)) return MultiFrameBurstBackend.ExecutionResult.Rejected

        var framesStarted = 0
        return try {
            repeat(frameCount) {
                framesStarted += 1
                captureFrame()
            }
            MultiFrameBurstBackend.ExecutionResult(completed = true, framesStarted = framesStarted)
        } catch (_: Throwable) {
            MultiFrameBurstBackend.ExecutionResult(completed = false, framesStarted = framesStarted)
        }
    }
}
