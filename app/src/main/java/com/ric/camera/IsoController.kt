package com.ric.camera

/**
 * Capability-safe boundary for manual ISO requests.
 *
 * This checkpoint deliberately validates and stores the requested ISO without
 * changing Camera2 request options yet. A later verified checkpoint can wire
 * the value into the capture request while preserving auto-exposure fallback.
 */
class IsoController(
    private val cameraController: CameraController
) {
    var requestedIso: Int? = null
        private set

    val supportedRange: IntRange?
        get() = cameraController.capabilities?.manualExposure?.isoRange

    fun requestIso(iso: Int): Boolean {
        val range = supportedRange ?: return false
        requestedIso = iso.coerceIn(range.first, range.last)
        return true
    }

    fun clear() {
        requestedIso = null
    }
}
