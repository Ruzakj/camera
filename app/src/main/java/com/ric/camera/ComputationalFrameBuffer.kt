package com.ric.camera

/**
 * Ownership contract for a future computational frame buffer.
 *
 * The payload stays opaque at this boundary so CameraX/ImageProxy resources are not
 * introduced until their close/release lifecycle is verified. A buffer can be released
 * exactly once; processing code must never retain or use it after release.
 */
class ComputationalFrameBuffer private constructor(
    val frameId: Long,
    val timestampNanos: Long,
    private val releaseAction: () -> Unit,
) {
    enum class State {
        Owned,
        Released,
    }

    var state: State = State.Owned
        private set

    val isReleased: Boolean
        get() = state == State.Released

    fun release(): Boolean {
        if (state != State.Owned) return false
        state = State.Released
        releaseAction()
        return true
    }

    companion object {
        fun create(
            frameId: Long,
            timestampNanos: Long,
            releaseAction: () -> Unit,
        ): ComputationalFrameBuffer? {
            if (frameId < 0L || timestampNanos < 0L) return null
            return ComputationalFrameBuffer(frameId, timestampNanos, releaseAction)
        }
    }
}
