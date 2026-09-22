package com.ric.camera

/**
 * Capability-safe policy boundary for future multi-frame computational capture.
 *
 * This does not alter the existing capture pipeline. Unsupported hardware always
 * falls back to the proven single-frame path until a verified burst engine is wired.
 */
enum class ComputationalCaptureMode {
    SINGLE_FRAME,
    MULTI_FRAME
}

object MultiFrameCapturePolicy {
    fun modeFor(capabilities: ComputationalPhotographyCapabilities): ComputationalCaptureMode =
        if (capabilities.burstProcessingEligible) {
            ComputationalCaptureMode.MULTI_FRAME
        } else {
            ComputationalCaptureMode.SINGLE_FRAME
        }

    fun frameCountFor(capabilities: ComputationalPhotographyCapabilities): Int =
        when (modeFor(capabilities)) {
            ComputationalCaptureMode.MULTI_FRAME -> 3
            ComputationalCaptureMode.SINGLE_FRAME -> 1
        }
}
