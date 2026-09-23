package com.ric.camera

/**
 * Capability-safe contract for a verified multi-frame burst backend.
 *
 * Planning and execution remain separate: a backend must explicitly report
 * support before execution is attempted, and unsupported requests are rejected
 * without touching the proven single-frame ImageCapture path.
 */
interface MultiFrameBurstBackend {
    val isAvailable: Boolean

    fun supports(frameCount: Int): Boolean = isAvailable && frameCount > 1

    /** Result distinguishes a clean rejection from a partially executed burst. */
    data class ExecutionResult(
        val completed: Boolean,
        val framesStarted: Int,
    ) {
        val canSingleFrameFallback: Boolean get() = !completed && framesStarted == 0

        companion object {
            val Rejected = ExecutionResult(completed = false, framesStarted = 0)
        }
    }

    /**
     * Executes one bounded burst request when the backend supports it.
     *
     * The default implementation is deliberately non-executing so existing
     * capability-only backends cannot accidentally change production capture.
     */
    fun execute(frameCount: Int, captureFrame: () -> Unit): ExecutionResult = ExecutionResult.Rejected
}

/** Production-safe default until a real burst backend is verified. */
object UnavailableMultiFrameBurstBackend : MultiFrameBurstBackend {
    override val isAvailable: Boolean = false
}
