package com.ric.camera

/**
 * Small immutable snapshot of camera lifecycle state.
 *
 * Keeping state independent from Activity/UI code gives later controller and
 * engine refactors one capability-safe source of truth without changing the
 * currently verified capture path.
 */
data class CameraState(
    val phase: Phase = Phase.IDLE,
    val lensFacing: Int? = null,
    val capabilities: CameraCapabilities? = null,
    val errorMessage: String? = null,
) {
    val isReady: Boolean
        get() = phase == Phase.READY && capabilities != null

    enum class Phase {
        IDLE,
        BINDING,
        READY,
        ERROR,
    }

    companion object {
        fun idle(): CameraState = CameraState()

        fun binding(lensFacing: Int): CameraState = CameraState(
            phase = Phase.BINDING,
            lensFacing = lensFacing,
        )

        fun ready(capabilities: CameraCapabilities): CameraState = CameraState(
            phase = Phase.READY,
            lensFacing = capabilities.lensFacing,
            capabilities = capabilities,
        )

        fun error(lensFacing: Int?, message: String?): CameraState = CameraState(
            phase = Phase.ERROR,
            lensFacing = lensFacing,
            errorMessage = message,
        )
    }
}
