package com.ric.camera

import androidx.camera.core.ImageProxy

/**
 * Transfers a CameraX frame into a computational collection without exposing pixel planes.
 *
 * Ownership transfers as soon as [accept] is called. If wrapping or collection admission
 * fails, the frame is released before returning false. Accepted frames remain owned by the
 * collection and are released by its terminal lifecycle.
 */
object CameraXComputationalFrameCollectionBridge {
    fun accept(
        collection: ComputationalFrameCollection,
        frameId: Long,
        image: ImageProxy,
    ): Boolean {
        val buffer = CameraXComputationalFrameAdapter.wrap(frameId, image) ?: return false
        if (collection.add(buffer)) return true

        buffer.release()
        return false
    }
}
