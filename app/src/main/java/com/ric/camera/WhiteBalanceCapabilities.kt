package com.ric.camera

/**
 * Capability-first snapshot for white-balance controls.
 *
 * Camera2 devices may expose different AWB modes. Keep the supported set
 * explicit so later UI/request wiring never assumes a mode exists.
 */
data class WhiteBalanceCapabilities(
    val supportedAwbModes: Set<Int> = emptySet()
) {
    val isSupported: Boolean
        get() = supportedAwbModes.isNotEmpty()

    fun supports(mode: Int): Boolean = mode in supportedAwbModes

    companion object {
        val Unsupported = WhiteBalanceCapabilities()
    }
}
