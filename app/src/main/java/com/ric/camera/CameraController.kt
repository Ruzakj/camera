package com.ric.camera

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector

/**
 * Small architecture boundary around the currently bound CameraX camera.
 *
 * This checkpoint intentionally does not own lifecycle binding yet. It gives
 * subsequent focus/exposure/capture controllers one capability-first source
 * instead of reading CameraInfo independently throughout the activity.
 */
class CameraController {
    private var boundCamera: Camera? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    val camera: Camera?
        get() = boundCamera

    val capabilities: CameraCapabilities?
        get() = boundCamera?.let { CameraCapabilities.from(it, lensFacing) }

    fun attach(camera: Camera, lensFacing: Int = CameraSelector.LENS_FACING_BACK) {
        boundCamera = camera
        this.lensFacing = lensFacing
    }

    fun detach() {
        boundCamera = null
    }
}
