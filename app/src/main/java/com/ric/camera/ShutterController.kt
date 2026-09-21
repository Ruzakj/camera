package com.ric.camera

/**
 * Capability-safe boundary for manual shutter-time requests.
 *
 * This checkpoint only validates and stores a requested exposure time. Camera2
 * request wiring is intentionally deferred so unsupported hardware continues
 * using the existing automatic-exposure path without behavior changes.
 */
class ShutterController(
    private val cameraController: CameraController
) {
    var requestedShutterTimeNs: Long? = null
        private set

    val supportedRangeNs: LongRange?
        get() = cameraController.capabilities?.manualExposure?.shutterTimeRangeNs

    fun requestShutterTimeNs(exposureTimeNs: Long): Boolean {
        val range = supportedRangeNs ?: return false
        if (cameraController.camera == null) return false

        requestedShutterTimeNs = exposureTimeNs.coerceIn(range.first, range.last)
        return true
    }

    fun clear() {
        requestedShutterTimeNs = null
    }
}
