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
        val initialCollectedFrameCount = collection.collectedFrameCount
        var nextFrameId = initialCollectedFrameCount.toLong()
        val result = backend.execute(collection.expectedFrameCount) {
            captureFrame()
            check(collection.recordFrame(nextFrameId++)) {
                "Burst progress violated collection bounds or lifecycle state"
            }
        }

        val framesCollected = collection.collectedFrameCount - initialCollectedFrameCount
        val progressMatchesExecution = framesCollected == result.framesStarted
        val completionMatchesCollection = result.completed ==
            (collection.state == MultiFrameCollection.State.Complete)

        if (!progressMatchesExecution || !completionMatchesCollection) {
            collection.fail()
        } else if (!result.completed && result.framesStarted > 0) {
            collection.fail()
        }
        return result
    }
}
