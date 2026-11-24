package com.dollhouse.lab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomLayoutTest {
    @Test
    fun `tap on overlapping furniture selects the last drawn item`() {
        val rug = Furniture("rug", FurnitureType.Rug, centerX = 1.5f, centerZ = 2f)
        val bed = Furniture("bed", FurnitureType.Bed, centerX = 1.5f, centerZ = 2f)
        val layout = defaultRoomLayout.copy(furniture = listOf(rug, bed))

        assertEquals(bed, layout.furnitureAt(x = 1.5f, z = 2f))
        assertEquals(rug, layout.furnitureAt(x = 2.1f, z = 2f))
    }

    @Test
    fun `tap on empty floor finds no furniture`() {
        assertNull(defaultRoomLayout.furnitureAt(x = 1.5f, z = 3.5f))
    }

    @Test
    fun `moving the bed inside the room changes only its center`() {
        val layout = defaultRoomLayout
        val (rug, bed, wardrobe) = layout.furniture

        val moved = layout.moveFurniture(bed.id, centerX = 1.5f, centerZ = 2f)

        assertEquals(listOf(rug, bed.copy(centerX = 1.5f, centerZ = 2f), wardrobe), moved.furniture)
    }

    @Test
    fun `moving past a wall keeps the bed inside the room`() {
        val moved = defaultRoomLayout.moveFurniture("bed", centerX = -1f, centerZ = 5f)
        val bed = moved.furniture.first { it.id == "bed" }

        assertEquals(0.45f, bed.centerX, 0.0001f)
        assertEquals(3.05f, bed.centerZ, 0.0001f)
    }

    @Test
    fun `a bed wider than the room is centered across its width`() {
        val narrowRoom = defaultRoomLayout.copy(width = 0.5f)
        val moved = narrowRoom.moveFurniture("bed", centerX = 1f, centerZ = 2f)
        val bed = moved.furniture.first { it.id == "bed" }

        assertEquals(0.25f, bed.centerX, 0.0001f)
        assertEquals(2f, bed.centerZ, 0.0001f)
    }
}
