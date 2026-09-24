package com.ric.camera

/**
 * Capability-safe gate between exposure classification and future HDR/Night processing.
 *
 * A computational bracket is eligible only when all three known exposure groups are
 * present and there are no unclassified frames. Anything incomplete or ambiguous is
 * explicitly routed to the existing fallback path rather than guessing frame roles.
 */
object ComputationalExposureBracketPolicy {
    sealed interface Decision {
        data class UseBracket(
            val short: List<ComputationalFrameDescriptor>,
            val reference: List<ComputationalFrameDescriptor>,
            val long: List<ComputationalFrameDescriptor>,
        ) : Decision

        data object UseFallback : Decision
    }

    fun decide(selection: ComputationalExposureSelection.Result): Decision {
        if (!selection.hasCompleteBracket || selection.unknown.isNotEmpty()) {
            return Decision.UseFallback
        }

        return Decision.UseBracket(
            short = selection.short,
            reference = selection.reference,
            long = selection.long,
        )
    }
}
