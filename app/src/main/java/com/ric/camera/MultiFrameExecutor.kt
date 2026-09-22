package com.ric.camera

/**
 * Safe execution boundary for computational still capture.
 *
 * Multi-frame execution is intentionally gated until a verified burst backend exists.
 * Callers always receive a production-safe single-frame fallback today.
 */
class MultiFrameExecutor {
    data class ExecutionPlan(
        val requested: ComputationalExecutionDecision,
        val effective: ComputationalExecutionDecision,
        val fallbackApplied: Boolean,
    )

    fun prepare(decision: ComputationalExecutionDecision): ExecutionPlan {
        return when (decision) {
            ComputationalExecutionDecision.SingleFrame -> ExecutionPlan(
                requested = decision,
                effective = ComputationalExecutionDecision.SingleFrame,
                fallbackApplied = false,
            )

            is ComputationalExecutionDecision.MultiFrame -> ExecutionPlan(
                requested = decision,
                effective = ComputationalExecutionDecision.SingleFrame,
                fallbackApplied = true,
            )
        }
    }
}
