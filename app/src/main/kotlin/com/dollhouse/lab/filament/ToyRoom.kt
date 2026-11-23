package com.dollhouse.lab.filament

class ToyRoom(palette: ToyRoomPalette) {
  // The floor extends under the walls, keeping its top surface at Y = 0
  val floor =
      RoomBox(
          name = "floor",
          minX = -WALL_THICKNESS,
          minY = -FLOOR_THICKNESS,
          minZ = -WALL_THICKNESS,
          sizeX = WIDTH + 2 * WALL_THICKNESS,
          sizeY = FLOOR_THICKNESS,
          sizeZ = DEPTH + 2 * WALL_THICKNESS,
          color = palette.wood,
      )

  // The wall stays outside the room, with its inner face at X = 0
  val wallLeft =
      RoomBox(
          name = "wall -X",
          minX = -WALL_THICKNESS,
          minY = 0f,
          minZ = 0f,
          sizeX = WALL_THICKNESS,
          sizeY = HEIGHT,
          sizeZ = DEPTH,
          color = palette.warmWhite,
      )

  // The inner face stays at X = WIDTH, with thickness extending outward
  val wallRight =
      RoomBox(
          name = "wall +X",
          minX = WIDTH,
          minY = 0f,
          minZ = 0f,
          sizeX = WALL_THICKNESS,
          sizeY = HEIGHT,
          sizeZ = DEPTH,
          color = palette.warmWhite,
      )

  // Z walls span the outer width to close the corners without overlapping the side walls
  val wallBack =
      RoomBox(
          name = "wall -Z",
          minX = -WALL_THICKNESS,
          minY = 0f,
          minZ = -WALL_THICKNESS,
          sizeX = WIDTH + 2 * WALL_THICKNESS,
          sizeY = HEIGHT,
          sizeZ = WALL_THICKNESS,
          color = palette.warmWhite,
      )

  // The inner face stays at Z = DEPTH
  val wallFront =
      RoomBox(
          name = "wall +Z",
          minX = -WALL_THICKNESS,
          minY = 0f,
          minZ = DEPTH,
          sizeX = WIDTH + 2 * WALL_THICKNESS,
          sizeY = HEIGHT,
          sizeZ = WALL_THICKNESS,
          color = palette.warmWhite,
      )

  // The bed rests on the floor with a 0.1 m gap from the X and Z edges
  val bed =
      RoomBox(
          name = "bed",
          minX = 0.1f,
          minY = 0f,
          minZ = 0.1f,
          sizeX = 0.9f,
          sizeY = 0.45f,
          sizeZ = 1.9f,
          color = palette.skyBlue,
      )

  companion object {
    const val WIDTH = 3f
    const val DEPTH = 4f
    const val HEIGHT = 2.5f

    private const val FLOOR_THICKNESS = 0.1f
    private const val WALL_THICKNESS = 0.1f
  }
}
