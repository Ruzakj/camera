package com.ric.camera

import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture

/**
 * Small capability-first bridge between a bound camera and CaptureController.
 *
 * Keeping capability probing here lets the UI call site stay unaware of computational
 * planning details. CaptureExecutionAdapter still guarantees the production-safe
 * single-frame path until a real multi-frame backend is explicitly enabled.
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
}
