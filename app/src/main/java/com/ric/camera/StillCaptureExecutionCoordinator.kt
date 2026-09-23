package com.ric.camera

import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture

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

        val result = runCatching {
            executor.executeMultiFrame(plan) {
                captureFrame(capture)
            }
        }.getOrDefault(MultiFrameBurstBackend.ExecutionResult.Rejected)

        if (result.canSingleFrameFallback) {
            captureFrame(capture)
        }
        return result.completed
    }
}
