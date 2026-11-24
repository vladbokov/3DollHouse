package com.dollhouse.lab.model

data class RoomLayout(val width: Float, val depth: Float, val height: Float)

val defaultRoomLayout = RoomLayout(width = 3f, depth = 4f, height = 2.5f)
