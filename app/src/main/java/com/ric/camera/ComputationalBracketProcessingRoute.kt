package com.ric.camera

/**
 * Capability-safe routing boundary for future HDR/Night pixel processing.
 *
 * This converts the verified exposure bracket policy into an explicit processing route.
 * Ambiguous or incomplete selections always stay on the existing fallback path.
 */
object ComputationalBracketProcessingRoute {
    sealed interface Route {
        data class Bracket(
            val short: List<ComputationalFrameDescriptor>,
            val reference: List<ComputationalFrameDescriptor>,
            val long: List<ComputationalFrameDescriptor>,
        ) : Route

        data object Fallback : Route
    }

    fun route(selection: ComputationalExposureSelection.Result): Route {
        return when (val decision = ComputationalExposureBracketPolicy.decide(selection)) {
            is ComputationalExposureBracketPolicy.Decision.UseBracket -> Route.Bracket(
                short = decision.short,
                reference = decision.reference,
                long = decision.long,
            )
            ComputationalExposureBracketPolicy.Decision.UseFallback -> Route.Fallback
        }
    }
}
