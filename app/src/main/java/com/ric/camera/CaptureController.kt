package com.ric.camera

import androidx.camera.core.ImageCapture

/**
 * Lifecycle-safe boundary for still-image capture state.
 *
 * This checkpoint intentionally does not move the existing save pipeline yet;
 * it only centralizes ownership of the currently bound ImageCapture use case so
 * later capture refactors can fail safely when the camera is not ready.
 */
class CaptureController {
    private var imageCapture: ImageCapture? = null

    val isReady: Boolean
        get() = imageCapture != null

    fun attach(imageCapture: ImageCapture) {
        this.imageCapture = imageCapture
    }

    fun detach() {
        imageCapture = null
    }

    fun <T> withImageCapture(block: (ImageCapture) -> T): T? {
        val capture = imageCapture ?: return null
        return block(capture)
    }
}
