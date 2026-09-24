package com.ric.camera

/**
 * Capability-safe bridge from a verified bracket processing route to burst execution.
 *
 * This checkpoint deliberately plans only: it never starts capture. A bracket is executable
 * only when the backend explicitly supports the exact number of classified frames. Otherwise
 * callers stay on the proven fallback path.
 */
object ComputationalBracketExecutionPlan {
    sealed interface Plan {
        data class Burst(val frameCount: Int) : Plan
        data object Fallback : Plan
    }

    fun plan(
        route: ComputationalBracketProcessingRoute.Route,
        backend: MultiFrameBurstBackend,
    ): Plan {
        val bracket = route as? ComputationalBracketProcessingRoute.Route.Bracket
            ?: return Plan.Fallback

        val frameCount = bracket.short.size + bracket.reference.size + bracket.long.size
        return if (backend.supports(frameCount)) {
            Plan.Burst(frameCount)
        } else {
            Plan.Fallback
        }
    }
}
