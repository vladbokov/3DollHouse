package com.dollhouse.lab.studio

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.dollhouse.lab.R
import com.dollhouse.lab.model.RoomLayout

private const val GRID_STEP_METERS = 0.5f

@Composable
internal fun RoomPlan(layout: RoomLayout, modifier: Modifier = Modifier) {
    val floorColor = colorResource(R.color.room_wood)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)

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
    }
}
