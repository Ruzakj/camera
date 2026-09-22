package com.ric.camera

import androidx.camera.core.ImageCapture

/**
 * Production-safe bridge between computational planning and the proven still-capture path.
 *
 * Until a real multi-frame backend is verified end-to-end, every execution plan resolves
 * to exactly one ImageCapture invocation. This keeps planning metadata observable without
 * changing photo saving behavior or bypassing CaptureController readiness checks.
 */
class CaptureExecutionAdapter {
    data class Result<T>(
        val value: T,
        val effectiveDecision: ComputationalExecutionDecision,
        val multiFrameDeferred: Boolean,
    )

    fun <T> execute(
        imageCapture: ImageCapture,
        plan: MultiFrameExecutor.ExecutionPlan,
        singleFrameCapture: (ImageCapture) -> T,
    ): Result<T> {
        val multiFrameDeferred = plan.effective is ComputationalExecutionDecision.MultiFrame
        return Result(
            value = singleFrameCapture(imageCapture),
            effectiveDecision = ComputationalExecutionDecision.SingleFrame,
            multiFrameDeferred = multiFrameDeferred,
        )
    }
}
