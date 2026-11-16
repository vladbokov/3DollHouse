package com.dollhouse.lab.filament

object ToyRoom {
    const val WIDTH = 3f
    const val DEPTH = 4f

    private const val FLOOR_THICKNESS = 0.1f

    // The floor extends downward, keeping its top surface at Y = 0
    val floor =
        RoomBox(
            name = "floor",
            minX = 0f,
            minY = -FLOOR_THICKNESS,
            minZ = 0f,
            sizeX = WIDTH,
            sizeY = FLOOR_THICKNESS,
            sizeZ = DEPTH,
            color = 0xE9C98A,
        )
}
