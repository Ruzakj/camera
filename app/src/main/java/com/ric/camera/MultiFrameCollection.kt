package com.ric.camera

/**
 * Lifecycle-only contract for a future computational frame stack.
 *
 * This deliberately carries frame identity rather than image bytes so ownership and
 * partial-failure semantics can be verified before CameraX/ImageProxy resources are
 * introduced. A collection is complete only when the expected bounded frame count
 * has been observed exactly once.
 */
class MultiFrameCollection private constructor(
    val expectedFrameCount: Int,
) {
    enum class State {
        Collecting,
        Complete,
        Failed,
    }

    private val frameIds = LinkedHashSet<Long>(expectedFrameCount)

    var state: State = State.Collecting
        private set

    val collectedFrameCount: Int
        get() = frameIds.size

    fun recordFrame(frameId: Long): Boolean {
        if (state != State.Collecting) return false
        if (!frameIds.add(frameId)) return false

        if (frameIds.size == expectedFrameCount) {
            state = State.Complete
        }
        return true
    }

    fun fail() {
        if (state == State.Collecting) state = State.Failed
    }

    companion object {
        fun create(expectedFrameCount: Int): MultiFrameCollection? =
            expectedFrameCount.takeIf { it > 1 }?.let(::MultiFrameCollection)
    }
}
