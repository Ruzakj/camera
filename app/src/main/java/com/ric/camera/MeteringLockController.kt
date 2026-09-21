package com.ric.camera

import androidx.camera.core.MeteringPoint

/**
 * Coordinates the manual metering -> lock flow without leaking CameraX
 * controls into the UI layer.
 *
 * Metering is kept persistent so callers can wait for the preview to settle
 * before explicitly locking. Clearing releases both Camera2 lock overrides
 * and the persistent CameraX metering request, restoring automatic behavior.
 */
class MeteringLockController(
    private val focusController: FocusController,
    private val aeAfLockController: AeAfLockController,
) {
    fun meter(point: MeteringPoint): Boolean =
        focusController.meter(point, persistent = true)

    fun lock(): Boolean = aeAfLockController.lock()

    fun clear(): Boolean {
        val lockCleared = aeAfLockController.clear()
        val meteringCleared = focusController.cancel()
        return lockCleared || meteringCleared
    }
}
