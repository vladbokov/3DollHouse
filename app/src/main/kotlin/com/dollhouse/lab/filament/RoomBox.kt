package com.dollhouse.lab.filament

// A box in room coordinates, measured in meters
data class RoomBox(
    val name: String,
    val minX: Float,
    val minY: Float,
    val minZ: Float,
    val sizeX: Float,
    val sizeY: Float,
    val sizeZ: Float,
    val color: Int, // Packed sRGB color, alpha is ignored by the opaque material
) {
    // The unit cube is centered, so placing it requires the box center
    val centerX: Float
        get() = minX + sizeX / 2

    val centerY: Float
        get() = minY + sizeY / 2

    val centerZ: Float
        get() = minZ + sizeZ / 2
}
