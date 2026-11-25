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
        val bed = Furniture("bed", FurnitureType.Bed, centerX = 0.25f, centerZ = 2f)
        val narrowRoom =
            defaultRoomLayout.copy(
                width = 0.5f,
                door = Opening(minX = 0f, minY = 0f, width = 0.4f, height = 2f),
                window = Opening(minX = 0f, minY = 0.9f, width = 0.4f, height = 1f),
                furniture = listOf(bed),
            )
        val moved = narrowRoom.moveFurniture("bed", centerX = 1f, centerZ = 2f)
        val movedBed = moved.furniture.first { it.id == "bed" }

        assertEquals(0.25f, movedBed.centerX, 0.0001f)
        assertEquals(2f, movedBed.centerZ, 0.0001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate furniture ids are rejected`() {
        val rug = defaultRoomLayout.furniture.first()
        defaultRoomLayout.copy(furniture = listOf(rug, rug))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `furniture outside the room is rejected`() {
        val bed = Furniture("bed", FurnitureType.Bed, centerX = -1f, centerZ = 2f)
        defaultRoomLayout.copy(furniture = listOf(bed))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a window wider than the wall is rejected`() {
        defaultRoomLayout.copy(
            window = Opening(minX = 2.5f, minY = 0.9f, width = 1.2f, height = 1f)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a room without positive size is rejected`() {
        defaultRoomLayout.copy(depth = 0f)
    }
}
