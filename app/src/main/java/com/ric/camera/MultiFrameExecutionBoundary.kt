package com.ric.camera

/**
 * Execution decision boundary for computational still capture.
 *
 * It intentionally does not execute ImageCapture yet. The existing proven save path
 * remains authoritative while callers can distinguish a safe single-frame capture
 * from a future multi-frame execution request.
 */
sealed interface ComputationalExecutionDecision {
    data object SingleFrame : ComputationalExecutionDecision

    data class MultiFrame(val frameCount: Int) : ComputationalExecutionDecision
}

object MultiFrameExecutionBoundary {
    fun decisionFor(plan: ComputationalCapturePlan): ComputationalExecutionDecision {
        return when {
            plan.mode != ComputationalCaptureMode.MULTI_FRAME ->
                ComputationalExecutionDecision.SingleFrame

            plan.frameCount <= 1 ->
                ComputationalExecutionDecision.SingleFrame

            else -> ComputationalExecutionDecision.MultiFrame(plan.frameCount)
        }
    }
}
