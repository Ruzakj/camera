package com.ric.camera

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector

/**
 * Small architecture boundary around the currently bound CameraX camera.
 *
 * Owns a capability-first lifecycle snapshot while preserving the existing
 * binding, focus, exposure, and capture behavior.
 */
class CameraController {
    private var boundCamera: Camera? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    var state: CameraState = CameraState.idle()
        private set

    val camera: Camera?
        get() = boundCamera

    val capabilities: CameraCapabilities?
        get() = state.capabilities

    fun beginBinding(lensFacing: Int) {
        boundCamera = null
        this.lensFacing = lensFacing
        state = CameraState.binding(lensFacing)
    }

    fun attach(camera: Camera, lensFacing: Int = CameraSelector.LENS_FACING_BACK) {
        boundCamera = camera
        this.lensFacing = lensFacing
        state = CameraState.ready(CameraCapabilities.from(camera, lensFacing))
    }

    fun failBinding(error: Throwable? = null) {
        boundCamera = null
        state = CameraState.error(lensFacing, error?.message)
    }

    fun detach() {
        boundCamera = null
        state = CameraState.idle()
    }
}
