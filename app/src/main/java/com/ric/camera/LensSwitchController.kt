package com.ric.camera

class LensSwitchController(private val capabilities: LensCapabilities) {
    fun nextLens(currentLensFacing: Int): Int = capabilities.alternateFor(currentLensFacing) ?: currentLensFacing
    fun canSwitch(currentLensFacing: Int): Boolean = capabilities.alternateFor(currentLensFacing) != null
}
