package com.ric.camera

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

/** Lightweight MediaStore access for the camera gallery preview. */
class GalleryController(private val contentResolver: ContentResolver) {
    fun latestImageUri(): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val args = arrayOf("Pictures/Ric Camera%")
        val sort = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        return runCatching {
            contentResolver.query(collection, projection, selection, args, sort)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                ContentUris.withAppendedId(collection, id)
            }
        }.getOrNull()
    }

    fun isReadable(uri: Uri?): Boolean {
        if (uri == null) return false
        return runCatching {
            contentResolver.openFileDescriptor(uri, "r")?.use { it.fileDescriptor.valid() } ?: false
        }.getOrDefault(false)
    }
}
