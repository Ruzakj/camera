package com.ric.camera

/**
 * Capability-safe boundary for white-balance requests.
 *
 * This checkpoint only validates requested AWB modes against the active
 * camera's probed capabilities. Camera2 request wiring is intentionally kept
 * separate so unsupported hardware continues using its existing AWB path.
 */
class WhiteBalanceController(
    private val cameraController: CameraController
) {
    var requestedAwbMode: Int? = null
        private set

    val supportedModes: Set<Int>
        get() = cameraController.capabilities?.whiteBalance?.supportedAwbModes.orEmpty()

    fun requestMode(mode: Int): Boolean {
        val capabilities = cameraController.capabilities?.whiteBalance ?: return false
        if (!capabilities.supports(mode)) return false
        if (cameraController.camera == null) return false

        requestedAwbMode = mode
        return true
    }

    fun clear() {
        requestedAwbMode = null
    }
}
