package com.ric.camera

import androidx.camera.core.ImageCapture

/**
 * Lifecycle-safe boundary for still-image capture state.
 *
 * The proven ImageCapture/save pipeline remains untouched. Computational capture
 * is exposed as planning/execution metadata only so later execution work can be
 * introduced without bypassing readiness checks or the single-frame fallback.
 */
class CaptureController(
    private val multiFrameCoordinator: MultiFrameCaptureCoordinator = MultiFrameCaptureCoordinator()
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

    fun <T> withImageCapture(block: (ImageCapture) -> T): T? {
        val capture = imageCapture ?: return null
        return block(capture)
    }
}
