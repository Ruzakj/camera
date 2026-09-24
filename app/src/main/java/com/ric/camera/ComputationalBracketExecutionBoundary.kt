package com.ric.camera

/**
 * Minimal execution boundary for a verified computational bracket plan.
 *
 * A planned burst is delegated to the capability-gated backend. Single-frame fallback is
 * permitted only when no burst frame has started, preventing an extra fallback exposure after
 * a partially executed burst. A fallback plan never touches the burst backend.
 */
object ComputationalBracketExecutionBoundary {
    sealed interface Result {
        data object Completed : Result
        data object FallbackAllowed : Result
        data class PartialFailure(val framesStarted: Int) : Result
    }

    fun execute(
        plan: ComputationalBracketExecutionPlan.Plan,
        backend: MultiFrameBurstBackend,
        captureFrame: () -> Unit,
    ): Result {
        val burst = plan as? ComputationalBracketExecutionPlan.Plan.Burst
            ?: return Result.FallbackAllowed

        val execution = backend.execute(burst.frameCount, captureFrame)
        return when {
            execution.completed -> Result.Completed
            execution.canSingleFrameFallback -> Result.FallbackAllowed
            else -> Result.PartialFailure(execution.framesStarted)
        }
    }
}
