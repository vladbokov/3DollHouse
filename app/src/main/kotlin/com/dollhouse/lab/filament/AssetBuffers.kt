package com.dollhouse.lab.filament

import android.content.res.AssetManager
import java.nio.ByteBuffer

/**
 * Reads an asset into a direct buffer. Filament passes buffers to native code, which can only read
 * direct ones.
 */
fun AssetManager.readDirectBuffer(path: String): ByteBuffer {
    val bytes = open(path).use { it.readBytes() }
    return ByteBuffer.allocateDirect(bytes.size).apply {
        put(bytes)
        // Moves the position back to the start, so the data can be read.
        flip()
    }
}
