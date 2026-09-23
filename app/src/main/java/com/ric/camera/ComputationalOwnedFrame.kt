package com.ric.camera

/**
 * Couples immutable frame metadata with its owned resource without duplicating identity.
 *
 * Processing stages can inspect [descriptor] while ownership and release remain governed
 * exclusively by [buffer]. Construction rejects mismatched metadata so descriptor data
 * can never silently refer to a different owned frame.
 */
data class ComputationalOwnedFrame private constructor(
    val descriptor: ComputationalFrameDescriptor,
    val buffer: ComputationalFrameBuffer,
) {
    companion object {
        fun create(
            descriptor: ComputationalFrameDescriptor,
            buffer: ComputationalFrameBuffer,
        ): ComputationalOwnedFrame? {
            if (buffer.isReleased) return null
            if (descriptor.frameId != buffer.frameId) return null
            if (descriptor.timestampNanos != buffer.timestampNanos) return null
            return ComputationalOwnedFrame(descriptor, buffer)
        }

        fun from(buffer: ComputationalFrameBuffer): ComputationalOwnedFrame? {
            val descriptor = ComputationalFrameDescriptor.create(
                frameId = buffer.frameId,
                timestampNanos = buffer.timestampNanos,
            ) ?: return null
            return create(descriptor, buffer)
        }
    }
}
