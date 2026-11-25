package com.dollhouse.lab.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.dollhouse.lab.R
import com.dollhouse.lab.model.FurnitureType

// Resolved sRGB colors from Android resources, not resource IDs
data class ToyRoomPalette(
    val wood: Int,
    val warmWhite: Int,
    val skyBlue: Int,
    val coral: Int,
    val mustard: Int,
) {
    fun furnitureColor(type: FurnitureType): Int =
        when (type) {
            FurnitureType.Rug -> mustard
            FurnitureType.Bed -> skyBlue
            FurnitureType.Wardrobe -> coral
        }

    companion object {
        fun fromResources(context: Context): ToyRoomPalette =
            ToyRoomPalette(
                wood = ContextCompat.getColor(context, R.color.room_wood),
                warmWhite = ContextCompat.getColor(context, R.color.room_warm_white),
                skyBlue = ContextCompat.getColor(context, R.color.room_sky_blue),
                coral = ContextCompat.getColor(context, R.color.room_coral),
                mustard = ContextCompat.getColor(context, R.color.room_mustard),
            )
    }
}

// Resolved once per composition, so screens pass colors down instead of Context
@Composable
fun rememberToyRoomPalette(): ToyRoomPalette {
    val context = LocalContext.current
    return remember(context) { ToyRoomPalette.fromResources(context) }
}
