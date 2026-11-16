package com.dollhouse.lab.filament

import com.google.android.filament.View

/**
 * Connects scene content to the rendering host.
 *
 * The host draws [view], forwards surface size changes to [resize], and calls [destroy] before
 * destroying the Engine.
 *
 * This contract keeps scene setup separate from Android surface handling and the frame loop.
 */
interface FilamentContent {
    val view: View

    fun resize(width: Int, height: Int)

    fun destroy()
}
