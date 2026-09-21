package com.ric.camera

/**
 * Capability-first snapshot for manual sensor exposure controls.
 *
 * CameraX does not expose ISO/shutter ranges through the stable CameraInfo API,
 * so this model deliberately defaults to unsupported until a Camera2-backed
 * capability probe populates real sensor ranges.
 */
data class ManualExposureCapabilities(
    val isoRange: IntRange? = null,
    val shutterTimeRangeNs: LongRange? = null
) {
    val supportsManualIso: Boolean
        get() = isoRange != null

    val supportsManualShutter: Boolean
        get() = shutterTimeRangeNs != null

    companion object {
        val Unsupported = ManualExposureCapabilities()
    }
}
