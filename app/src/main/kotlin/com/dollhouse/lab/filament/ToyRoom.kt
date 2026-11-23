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

  // Four boxes surround the window opening and close the outer wall corners
  val wallBackParts =
      listOf(
          RoomBox(
              name = "wall -Z left of window",
              minX = -WALL_THICKNESS,
              minY = 0f,
              minZ = -WALL_THICKNESS,
              sizeX = WINDOW_MIN_X + WALL_THICKNESS,
              sizeY = HEIGHT,
              sizeZ = WALL_THICKNESS,
              color = palette.warmWhite,
          ),
          RoomBox(
              name = "wall -Z right of window",
              minX = WINDOW_MIN_X + WINDOW_WIDTH,
              minY = 0f,
              minZ = -WALL_THICKNESS,
              sizeX = WIDTH + WALL_THICKNESS - (WINDOW_MIN_X + WINDOW_WIDTH),
              sizeY = HEIGHT,
              sizeZ = WALL_THICKNESS,
              color = palette.warmWhite,
          ),
          RoomBox(
              name = "wall -Z below window",
              minX = WINDOW_MIN_X,
              minY = 0f,
              minZ = -WALL_THICKNESS,
              sizeX = WINDOW_WIDTH,
              sizeY = WINDOW_MIN_Y,
              sizeZ = WALL_THICKNESS,
              color = palette.warmWhite,
          ),
          RoomBox(
              name = "wall -Z above window",
              minX = WINDOW_MIN_X,
              minY = WINDOW_MIN_Y + WINDOW_HEIGHT,
              minZ = -WALL_THICKNESS,
              sizeX = WINDOW_WIDTH,
              sizeY = HEIGHT - (WINDOW_MIN_Y + WINDOW_HEIGHT),
              sizeZ = WALL_THICKNESS,
              color = palette.warmWhite,
          ),
      )

  // The frame sits inside the opening and protrudes slightly on both sides of the wall
  val windowFrameParts =
      listOf(
          RoomBox(
              name = "window frame left",
              minX = WINDOW_MIN_X,
              minY = WINDOW_MIN_Y,
              minZ = -WALL_THICKNESS - FRAME_OVERHANG,
              sizeX = FRAME_THICKNESS,
              sizeY = WINDOW_HEIGHT,
              sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
              color = palette.wood,
          ),
          RoomBox(
              name = "window frame right",
              minX = WINDOW_MIN_X + WINDOW_WIDTH - FRAME_THICKNESS,
              minY = WINDOW_MIN_Y,
              minZ = -WALL_THICKNESS - FRAME_OVERHANG,
              sizeX = FRAME_THICKNESS,
              sizeY = WINDOW_HEIGHT,
              sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
              color = palette.wood,
          ),
          // Horizontal rails fit between the vertical rails without overlapping
          RoomBox(
              name = "window frame bottom",
              minX = WINDOW_MIN_X + FRAME_THICKNESS,
              minY = WINDOW_MIN_Y,
              minZ = -WALL_THICKNESS - FRAME_OVERHANG,
              sizeX = WINDOW_WIDTH - 2 * FRAME_THICKNESS,
              sizeY = FRAME_THICKNESS,
              sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
              color = palette.wood,
          ),
          RoomBox(
              name = "window frame top",
              minX = WINDOW_MIN_X + FRAME_THICKNESS,
              minY = WINDOW_MIN_Y + WINDOW_HEIGHT - FRAME_THICKNESS,
              minZ = -WALL_THICKNESS - FRAME_OVERHANG,
              sizeX = WINDOW_WIDTH - 2 * FRAME_THICKNESS,
              sizeY = FRAME_THICKNESS,
              sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
              color = palette.wood,
          ),
      )

  // Three boxes leave an empty doorway, with the inner wall face at Z = DEPTH
  val wallFrontParts =
      listOf(
          RoomBox(
              name = "wall +Z left of door",
              minX = -WALL_THICKNESS,
              minY = 0f,
              minZ = DEPTH,
              sizeX = DOOR_MIN_X + WALL_THICKNESS,
              sizeY = HEIGHT,
              sizeZ = WALL_THICKNESS,
              color = palette.warmWhite,
          ),
          RoomBox(
              name = "wall +Z right of door",
              minX = DOOR_MIN_X + DOOR_WIDTH,
              minY = 0f,
              minZ = DEPTH,
              sizeX = WIDTH + WALL_THICKNESS - (DOOR_MIN_X + DOOR_WIDTH),
              sizeY = HEIGHT,
              sizeZ = WALL_THICKNESS,
              color = palette.warmWhite,
          ),
          // The header fills only the space above the opening
          RoomBox(
              name = "wall +Z above door",
              minX = DOOR_MIN_X,
              minY = DOOR_HEIGHT,
              minZ = DEPTH,
              sizeX = DOOR_WIDTH,
              sizeY = HEIGHT - DOOR_HEIGHT,
              sizeZ = WALL_THICKNESS,
              color = palette.warmWhite,
          ),
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

  // Follow the bed position and rest on its top surface
  val pillow =
      RoomBox(
          name = "pillow",
          minX = bed.minX + 0.1f,
          minY = bed.minY + bed.sizeY,
          minZ = bed.minZ + 0.1f,
          sizeX = bed.sizeX - 0.2f,
          sizeY = 0.12f,
          sizeZ = 0.4f,
          color = palette.warmWhite,
      )

  // A low toy wardrobe beside the right wall
  val wardrobe =
      RoomBox(
          name = "wardrobe",
          minX = WIDTH - 1.1f,
          minY = 0f,
          minZ = 0.05f,
          sizeX = 1f,
          sizeY = 0.8f,
          sizeZ = 0.5f,
          color = palette.coral,
      )

  // A thin box with its bottom on the floor, avoiding coplanar top surfaces
  val rug =
      RoomBox(
          name = "rug",
          minX = 1.2f,
          minY = 0f,
          minZ = 1.6f,
          sizeX = 1.4f,
          sizeY = 0.02f,
          sizeZ = 1.6f,
          color = palette.mustard,
      )

  companion object {
    const val WIDTH = 3f
    const val DEPTH = 4f
    const val HEIGHT = 2.5f

    private const val FLOOR_THICKNESS = 0.1f
    private const val WALL_THICKNESS = 0.1f

    private const val DOOR_WIDTH = 0.9f
    private const val DOOR_HEIGHT = 2f
    private const val DOOR_MIN_X = (WIDTH - DOOR_WIDTH) / 2

    private const val WINDOW_WIDTH = 1.2f
    private const val WINDOW_HEIGHT = 1f
    private const val WINDOW_MIN_X = (WIDTH - WINDOW_WIDTH) / 2
    private const val WINDOW_MIN_Y = 0.9f
    private const val FRAME_THICKNESS = 0.06f
    private const val FRAME_OVERHANG = 0.02f
  }
}
