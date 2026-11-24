package com.dollhouse.lab.studio

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

internal class PlanTransform private constructor(val pixelsPerMeter: Float, val origin: Offset) {
    fun toCanvas(xMeters: Float, zMeters: Float): Offset =
        origin + Offset(xMeters, zMeters) * pixelsPerMeter

    // The returned offset stores room X and Z in meters
    fun toRoom(canvasPoint: Offset): Offset = (canvasPoint - origin) / pixelsPerMeter

    companion object {
        fun fit(
            roomWidth: Float,
            roomDepth: Float,
            viewport: Size,
            paddingPx: Float,
        ): PlanTransform? {
            val availableWidth = viewport.width - 2 * paddingPx
            val availableHeight = viewport.height - 2 * paddingPx
            if (availableWidth <= 0f || availableHeight <= 0f) return null

            val scale = minOf(availableWidth / roomWidth, availableHeight / roomDepth)
            val origin =
                Offset(
                    (viewport.width - roomWidth * scale) / 2,
                    (viewport.height - roomDepth * scale) / 2,
                )
            return PlanTransform(scale, origin)
        }
    }
}
