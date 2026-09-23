package com.ric.camera

/**
 * Owns opaque computational frame buffers for exactly one multi-frame collection.
 *
 * Buffers remain isolated from CameraX/ImageProxy. Terminal lifecycle transitions
 * release every accepted buffer deterministically; callers cannot add buffers after
 * completion or failure.
 */
class ComputationalFrameCollection private constructor(
    private val collection: MultiFrameCollection,
) {
    private val buffers = LinkedHashMap<Long, ComputationalFrameBuffer>(collection.expectedFrameCount)

    val state: MultiFrameCollection.State
        get() = collection.state

    val ownedBufferCount: Int
        get() = buffers.values.count { !it.isReleased }

    fun add(buffer: ComputationalFrameBuffer): Boolean {
        if (collection.state != MultiFrameCollection.State.Collecting) return false
        if (buffer.isReleased || buffers.containsKey(buffer.frameId)) return false
        if (!collection.recordFrame(buffer.frameId)) return false

        buffers[buffer.frameId] = buffer
        if (collection.state == MultiFrameCollection.State.Complete) {
            releaseAll()
        }
        return true
    }

    fun fail() {
        collection.fail()
        releaseAll()
    }

    private fun releaseAll() {
        buffers.values.forEach { it.release() }
    }

    companion object {
        fun create(expectedFrameCount: Int): ComputationalFrameCollection? =
            MultiFrameCollection.create(expectedFrameCount)?.let(::ComputationalFrameCollection)
    }
}
