package com.ric.camera

object ComputationalBracketFrameCollectionBoundary {
    sealed interface Result {
        data object Fallback : Result
        data class Ready(
            val frameCount: Int,
            val collection: ComputationalFrameCollection,
        ) : Result
    }

    fun prepare(
        plan: ComputationalBracketExecutionPlan.Plan,
        backend: MultiFrameBurstBackend,
    ): Result {
        val burst = plan as? ComputationalBracketExecutionPlan.Plan.Burst
            ?: return Result.Fallback
        if (!backend.supports(burst.frameCount)) return Result.Fallback
        val collection = ComputationalFrameCollection.create(burst.frameCount)
            ?: return Result.Fallback
        return Result.Ready(burst.frameCount, collection)
    }
}
