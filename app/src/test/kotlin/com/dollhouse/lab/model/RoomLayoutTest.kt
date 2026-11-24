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
}
