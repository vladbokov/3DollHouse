package com.dollhouse.lab.filament

import com.dollhouse.lab.model.Furniture
import com.dollhouse.lab.model.FurnitureType
import com.dollhouse.lab.model.RoomLayout
import com.dollhouse.lab.ui.theme.ToyRoomPalette

class ToyRoom(val layout: RoomLayout, private val palette: ToyRoomPalette) {
    private val door = layout.door
    private val window = layout.window

    // The floor extends under the walls, keeping its top surface at Y = 0
    val floor =
        RoomBox(
            name = "floor",
            minX = -WALL_THICKNESS,
            minY = -FLOOR_THICKNESS,
            minZ = -WALL_THICKNESS,
            sizeX = layout.width + 2 * WALL_THICKNESS,
            sizeY = FLOOR_THICKNESS,
            sizeZ = layout.depth + 2 * WALL_THICKNESS,
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
            sizeY = layout.height,
            sizeZ = layout.depth,
            color = palette.warmWhite,
        )

    // The inner face stays at X = layout.width, with thickness extending outward
    val wallRight =
        RoomBox(
            name = "wall +X",
            minX = layout.width,
            minY = 0f,
            minZ = 0f,
            sizeX = WALL_THICKNESS,
            sizeY = layout.height,
            sizeZ = layout.depth,
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
                sizeX = window.minX + WALL_THICKNESS,
                sizeY = layout.height,
                sizeZ = WALL_THICKNESS,
                color = palette.warmWhite,
            ),
            RoomBox(
                name = "wall -Z right of window",
                minX = window.minX + window.width,
                minY = 0f,
                minZ = -WALL_THICKNESS,
                sizeX = layout.width + WALL_THICKNESS - (window.minX + window.width),
                sizeY = layout.height,
                sizeZ = WALL_THICKNESS,
                color = palette.warmWhite,
            ),
            RoomBox(
                name = "wall -Z below window",
                minX = window.minX,
                minY = 0f,
                minZ = -WALL_THICKNESS,
                sizeX = window.width,
                sizeY = window.minY,
                sizeZ = WALL_THICKNESS,
                color = palette.warmWhite,
            ),
            RoomBox(
                name = "wall -Z above window",
                minX = window.minX,
                minY = window.minY + window.height,
                minZ = -WALL_THICKNESS,
                sizeX = window.width,
                sizeY = layout.height - (window.minY + window.height),
                sizeZ = WALL_THICKNESS,
                color = palette.warmWhite,
            ),
        )

    // The frame sits inside the opening and protrudes slightly on both sides of the wall
    val windowFrameParts =
        listOf(
            RoomBox(
                name = "window frame left",
                minX = window.minX,
                minY = window.minY,
                minZ = -WALL_THICKNESS - FRAME_OVERHANG,
                sizeX = FRAME_THICKNESS,
                sizeY = window.height,
                sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
                color = palette.wood,
            ),
            RoomBox(
                name = "window frame right",
                minX = window.minX + window.width - FRAME_THICKNESS,
                minY = window.minY,
                minZ = -WALL_THICKNESS - FRAME_OVERHANG,
                sizeX = FRAME_THICKNESS,
                sizeY = window.height,
                sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
                color = palette.wood,
            ),
            // Horizontal rails fit between the vertical rails without overlapping
            RoomBox(
                name = "window frame bottom",
                minX = window.minX + FRAME_THICKNESS,
                minY = window.minY,
                minZ = -WALL_THICKNESS - FRAME_OVERHANG,
                sizeX = window.width - 2 * FRAME_THICKNESS,
                sizeY = FRAME_THICKNESS,
                sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
                color = palette.wood,
            ),
            RoomBox(
                name = "window frame top",
                minX = window.minX + FRAME_THICKNESS,
                minY = window.minY + window.height - FRAME_THICKNESS,
                minZ = -WALL_THICKNESS - FRAME_OVERHANG,
                sizeX = window.width - 2 * FRAME_THICKNESS,
                sizeY = FRAME_THICKNESS,
                sizeZ = WALL_THICKNESS + 2 * FRAME_OVERHANG,
                color = palette.wood,
            ),
        )

    // Wall segments surround the doorway, with the inner face at Z = layout.depth
    val wallFrontParts =
        listOfNotNull(
            RoomBox(
                name = "wall +Z left of door",
                minX = -WALL_THICKNESS,
                minY = 0f,
                minZ = layout.depth,
                sizeX = door.minX + WALL_THICKNESS,
                sizeY = layout.height,
                sizeZ = WALL_THICKNESS,
                color = palette.warmWhite,
            ),
            RoomBox(
                name = "wall +Z right of door",
                minX = door.minX + door.width,
                minY = 0f,
                minZ = layout.depth,
                sizeX = layout.width + WALL_THICKNESS - (door.minX + door.width),
                sizeY = layout.height,
                sizeZ = WALL_THICKNESS,
                color = palette.warmWhite,
            ),
            // The header fills only the space above the opening
            RoomBox(
                name = "wall +Z above door",
                minX = door.minX,
                minY = door.minY + door.height,
                minZ = layout.depth,
                sizeX = door.width,
                sizeY = layout.height - (door.minY + door.height),
                sizeZ = WALL_THICKNESS,
                color = palette.warmWhite,
            ),
            if (door.minY > 0f) {
                RoomBox(
                    name = "wall +Z below door",
                    minX = door.minX,
                    minY = 0f,
                    minZ = layout.depth,
                    sizeX = door.width,
                    sizeY = door.minY,
                    sizeZ = WALL_THICKNESS,
                    color = palette.warmWhite,
                )
            } else {
                null
            },
        )

    // Build each box relative to the furniture center on the floor
    fun furnitureBoxes(item: Furniture): List<RoomBox> = buildList {
        val type = item.type
        val box =
            RoomBox(
                name = item.id,
                minX = -type.sizeX / 2,
                minY = 0f,
                minZ = -type.sizeZ / 2,
                sizeX = type.sizeX,
                sizeY = type.sizeY,
                sizeZ = type.sizeZ,
                color = palette.furnitureColor(type),
            )
        add(box)

        if (type == FurnitureType.Bed) {
            add(
                RoomBox(
                    name = "pillow",
                    minX = box.minX + 0.1f,
                    minY = box.minY + box.sizeY,
                    minZ = box.minZ + 0.1f,
                    sizeX = box.sizeX - 0.2f,
                    sizeY = 0.12f,
                    sizeZ = 0.4f,
                    color = palette.warmWhite,
                )
            )
        }
    }

    companion object {
        private const val FLOOR_THICKNESS = 0.1f
        private const val WALL_THICKNESS = 0.1f

        private const val FRAME_THICKNESS = 0.06f
        private const val FRAME_OVERHANG = 0.02f
    }
}
