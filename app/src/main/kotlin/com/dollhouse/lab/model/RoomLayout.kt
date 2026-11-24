package com.dollhouse.lab.model

data class RoomLayout(
    val width: Float,
    val depth: Float,
    val height: Float,
    val door: Opening,
    val window: Opening,
    val furniture: List<Furniture>,
) {
    internal fun furnitureAt(x: Float, z: Float): Furniture? = furniture.lastOrNull { item ->
        val halfWidth = item.type.sizeX / 2
        val halfDepth = item.type.sizeZ / 2
        x in (item.centerX - halfWidth)..(item.centerX + halfWidth) &&
            z in (item.centerZ - halfDepth)..(item.centerZ + halfDepth)
    }
}

// Position and size along the wall, measured in meters
data class Opening(val minX: Float, val minY: Float, val width: Float, val height: Float)

data class Furniture(
    val id: String,
    val type: FurnitureType,
    val centerX: Float,
    val centerZ: Float,
)

enum class FurnitureType(val sizeX: Float, val sizeY: Float, val sizeZ: Float) {
    Rug(1.4f, 0.02f, 1.6f),
    Bed(0.9f, 0.45f, 1.9f),
    Wardrobe(1f, 0.8f, 0.5f),
}

val defaultRoomLayout =
    RoomLayout(
        width = 3f,
        depth = 4f,
        height = 2.5f,
        door = Opening(minX = 1.05f, minY = 0f, width = 0.9f, height = 2f),
        window = Opening(minX = 0.9f, minY = 0.9f, width = 1.2f, height = 1f),
        furniture =
            listOf(
                Furniture("rug", FurnitureType.Rug, centerX = 1.9f, centerZ = 2.4f),
                Furniture("bed", FurnitureType.Bed, centerX = 0.55f, centerZ = 1.05f),
                Furniture("wardrobe", FurnitureType.Wardrobe, centerX = 2.4f, centerZ = 0.3f),
            ),
    )
