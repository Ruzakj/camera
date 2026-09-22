package com.ric.camera

/**
 * Capability-safe coordinator for computational capture planning.
 *
 * This checkpoint deliberately plans captures only; it does not replace the proven
 * ImageCapture save path. Unsupported hardware remains a one-frame capture.
 */
data class ComputationalCapturePlan(
    val mode: ComputationalCaptureMode,
    val frameCount: Int
)

class MultiFrameCaptureCoordinator {
    fun plan(capabilities: ComputationalPhotographyCapabilities): ComputationalCapturePlan {
        val mode = MultiFrameCapturePolicy.modeFor(capabilities)
        return ComputationalCapturePlan(
            mode = mode,
            frameCount = MultiFrameCapturePolicy.frameCountFor(capabilities)
        )
    }
}
