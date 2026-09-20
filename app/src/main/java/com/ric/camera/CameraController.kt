package com.ric.camera

import androidx.camera.core.Camera

/**
 * Small architecture boundary around the currently bound CameraX camera.
 *
 * This checkpoint intentionally does not own lifecycle binding yet. It gives
 * subsequent focus/exposure/capture controllers one capability-first source
 * instead of reading CameraInfo independently throughout the activity.
 */
class CameraController {
    private var boundCamera: Camera? = null

    val camera: Camera?
        get() = boundCamera

    val capabilities: CameraCapabilities
        get() = CameraCapabilities.from(boundCamera)

    fun attach(camera: Camera) {
        boundCamera = camera
    }

    fun detach() {
        boundCamera = null
    }
}
