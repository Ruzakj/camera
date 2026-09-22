package com.ric.camera

import androidx.camera.core.ImageCapture

/**
 * Lifecycle-safe boundary for still-image capture state.
 *
 * The proven ImageCapture/save pipeline remains untouched. Computational capture
 * is exposed through capability-safe planning and a production-safe execution adapter
 * without bypassing readiness checks or the single-frame fallback.
 */
class CaptureController(
    private val multiFrameCoordinator: MultiFrameCaptureCoordinator = MultiFrameCaptureCoordinator(),
    private val executionAdapter: CaptureExecutionAdapter = CaptureExecutionAdapter(),
) {
    private var imageCapture: ImageCapture? = null

    val isReady: Boolean
        get() = imageCapture != null

    fun attach(imageCapture: ImageCapture) {
        this.imageCapture = imageCapture
    }

    fun detach() {
        imageCapture = null
    }

    fun capturePlan(
        capabilities: ComputationalPhotographyCapabilities
    ): ComputationalCapturePlan? {
        if (!isReady) return null
        return multiFrameCoordinator.plan(capabilities)
    }

    fun executionDecision(
        capabilities: ComputationalPhotographyCapabilities
    ): ComputationalExecutionDecision? {
        val plan = capturePlan(capabilities) ?: return null
        return MultiFrameExecutionBoundary.decisionFor(plan)
    }

    fun executionPlan(
        capabilities: ComputationalPhotographyCapabilities,
        maxFrameCount: Int = 3,
    ): MultiFrameExecutor.ExecutionPlan? {
        val decision = executionDecision(capabilities) ?: return null
        return MultiFrameExecutor.fromCapabilities(
            capabilities = capabilities,
            maxFrameCount = maxFrameCount,
        ).prepare(decision)
    }

    fun <T> execute(
        capabilities: ComputationalPhotographyCapabilities,
        maxFrameCount: Int = 3,
        singleFrameCapture: (ImageCapture) -> T,
    ): CaptureExecutionAdapter.Result<T>? {
        val capture = imageCapture ?: return null
        val plan = executionPlan(capabilities, maxFrameCount) ?: return null
        return executionAdapter.execute(capture, plan, singleFrameCapture)
    }

    fun <T> withImageCapture(block: (ImageCapture) -> T): T? {
        val capture = imageCapture ?: return null
        return block(capture)
    }
}
