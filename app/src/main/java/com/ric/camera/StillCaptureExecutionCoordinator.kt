package com.ric.camera

import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture

/**
 * Small capability-first bridge between a bound camera and CaptureController.
 *
 * Keeping capability probing here lets the UI call site stay unaware of computational
 * planning details. The existing execute path remains production-safe single-frame;
 * guarded multi-frame execution is exposed separately until it is verified end-to-end.
 */
class StillCaptureExecutionCoordinator(
    private val captureController: CaptureController,
) {
    fun <T> execute(
        cameraInfo: CameraInfo,
        maxFrameCount: Int = 3,
        singleFrameCapture: (ImageCapture) -> T,
    ): CaptureExecutionAdapter.Result<T>? {
        val capabilities = ComputationalPhotographyCapabilityProbe.from(cameraInfo)
        return captureController.execute(
            capabilities = capabilities,
            maxFrameCount = maxFrameCount,
            singleFrameCapture = singleFrameCapture,
        )
    }

    /**
     * Executes a capability-approved bounded burst, or exactly one fallback frame when
     * the prepared plan cannot run as multi-frame. This is intentionally separate from
     * [execute] so the proven production save/gallery path cannot change implicitly.
     *
     * @return true only when the complete multi-frame request was accepted and executed.
     */
    fun executeMultiFrameOrFallback(
        cameraInfo: CameraInfo,
        maxFrameCount: Int = 3,
        captureFrame: (ImageCapture) -> Unit,
    ): Boolean {
        val capabilities = ComputationalPhotographyCapabilityProbe.from(cameraInfo)
        val plan = captureController.executionPlan(
            capabilities = capabilities,
            maxFrameCount = maxFrameCount,
        ) ?: return false
        val capture = captureController.withImageCapture { it } ?: return false
        val executor = MultiFrameExecutor.fromCapabilities(
            capabilities = capabilities,
            maxFrameCount = maxFrameCount,
        )

        val burstExecuted = executor.executeMultiFrame(plan) {
            captureFrame(capture)
        }
        if (!burstExecuted) {
            captureFrame(capture)
        }
        return burstExecuted
    }
}
