package com.dollhouse.lab.studio

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dollhouse.lab.model.RoomLayout
import com.dollhouse.lab.ui.theme.ToyRoomPalette

private const val GRID_STEP_METERS = 0.5f

@Composable
internal fun RoomPlan(layout: RoomLayout, modifier: Modifier = Modifier) {
    val palette = ToyRoomPalette.fromResources(LocalContext.current)
    val floorColor = Color(palette.wood)
    val wallColor = Color(palette.warmWhite)
    val outlineColor = MaterialTheme.colorScheme.onSurface
    val gridColor = outlineColor.copy(alpha = 0.24f)

    Canvas(modifier) {
        val transform =
            PlanTransform.fit(layout.width, layout.depth, size, paddingPx = 16.dp.toPx())
                ?: return@Canvas

        drawRect(
            color = floorColor,
            topLeft = transform.origin,
            size = Size(layout.width, layout.depth) * transform.pixelsPerMeter,
        )

        var x = GRID_STEP_METERS
        while (x < layout.width) {
            drawLine(gridColor, transform.toCanvas(x, 0f), transform.toCanvas(x, layout.depth))
            x += GRID_STEP_METERS
        }

        var z = GRID_STEP_METERS
        while (z < layout.depth) {
            drawLine(gridColor, transform.toCanvas(0f, z), transform.toCanvas(layout.width, z))
            z += GRID_STEP_METERS
        }

        drawWalls(layout, transform, wallColor, outlineColor)
    }
}

private fun DrawScope.drawWalls(
    layout: RoomLayout,
    transform: PlanTransform,
    wallColor: Color,
    outlineColor: Color,
) {
    val topLeft = transform.origin
    val bottomRight = transform.toCanvas(layout.width, layout.depth)
    val door = layout.door
    val window = layout.window
    val windowLeft = transform.toCanvas(window.minX, 0f).x
    val windowRight = transform.toCanvas(window.minX + window.width, 0f).x
    val doorLeft = transform.toCanvas(door.minX, layout.depth).x
    val doorRight = transform.toCanvas(door.minX + door.width, layout.depth).x

    val walls =
        Path().apply {
            moveTo(windowLeft, topLeft.y)
            lineTo(topLeft.x, topLeft.y)
            lineTo(topLeft.x, bottomRight.y)
            lineTo(doorLeft, bottomRight.y)

            moveTo(windowRight, topLeft.y)
            lineTo(bottomRight.x, topLeft.y)
            lineTo(bottomRight.x, bottomRight.y)
            lineTo(doorRight, bottomRight.y)
        }

    drawPath(walls, outlineColor, style = Stroke(width = 6.dp.toPx()))
    drawPath(walls, wallColor, style = Stroke(width = 4.dp.toPx()))
}
