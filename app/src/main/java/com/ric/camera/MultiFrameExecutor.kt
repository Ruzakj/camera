package com.ric.camera

/**
 * Safe execution boundary for computational still capture.
 *
 * Multi-frame execution is accepted only when the injected burst backend explicitly
 * reports support for the requested frame count. Otherwise callers receive the
 * production-safe single-frame fallback.
 */
class MultiFrameExecutor(
    private val burstBackend: MultiFrameBurstBackend = UnavailableMultiFrameBurstBackend,
) {
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

            is ComputationalExecutionDecision.MultiFrame -> {
                val backendSupported = burstBackend.supports(decision.frameCount)
                ExecutionPlan(
                    requested = decision,
                    effective = if (backendSupported) decision else ComputationalExecutionDecision.SingleFrame,
                    fallbackApplied = !backendSupported,
                )
            }
        }
    }

    /**
     * Executes only an already-prepared multi-frame plan.
     *
     * Returning false is intentional: the caller retains ownership of the single-frame
     * fallback, preventing a rejected/failed burst from causing duplicate captures.
     */
    fun executeMultiFrame(
        plan: ExecutionPlan,
        captureFrame: () -> Unit,
    ): Boolean {
        val decision = plan.effective as? ComputationalExecutionDecision.MultiFrame ?: return false
        return burstBackend.execute(
            frameCount = decision.frameCount,
            captureFrame = captureFrame,
        )
    }

    companion object {
        fun fromCapabilities(
            capabilities: ComputationalPhotographyCapabilities,
            maxFrameCount: Int = 3,
        ): MultiFrameExecutor = MultiFrameExecutor(
            burstBackend = MultiFrameBurstBackendFactory.from(
                capabilities = capabilities,
                maxFrameCount = maxFrameCount,
            )
        )
    }
}
