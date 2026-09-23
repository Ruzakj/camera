package com.ric.camera

/**
 * Bridges burst execution progress into the lifecycle-only collection contract.
 *
 * No image resources are owned here. Frame identity is generated only after a frame
 * callback returns successfully, so a throwing capture callback cannot be recorded as
 * collected. Partial execution is marked failed and a clean rejection remains empty.
 */
class MultiFrameCollectionProgress(
    private val collection: MultiFrameCollection,
) {
    fun execute(
        backend: MultiFrameBurstBackend,
        captureFrame: () -> Unit,
    ): MultiFrameBurstBackend.ExecutionResult {
        var nextFrameId = 0L
        val result = backend.execute(collection.expectedFrameCount) {
            captureFrame()
            collection.recordFrame(nextFrameId++)
        }

        if (!result.completed && result.framesStarted > 0) {
            collection.fail()
        }
        return result
    }
}
