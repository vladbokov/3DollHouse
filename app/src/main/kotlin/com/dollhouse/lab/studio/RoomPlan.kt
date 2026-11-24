package com.dollhouse.lab.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.dollhouse.lab.R
import com.dollhouse.lab.model.FurnitureType
import com.dollhouse.lab.model.RoomLayout
import com.dollhouse.lab.ui.theme.ToyRoomPalette

private const val GRID_STEP_METERS = 0.5f

@Composable
internal fun RoomPlan(
    layout: RoomLayout,
    onMoveFurniture: (id: String, centerX: Float, centerZ: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFurnitureId by rememberSaveable { mutableStateOf<String?>(null) }
    val currentLayout by rememberUpdatedState(layout)
    val moveFurniture by rememberUpdatedState(onMoveFurniture)
    val paddingPx = with(LocalDensity.current) { 16.dp.toPx() }
    val palette = ToyRoomPalette.fromResources(LocalContext.current)
    val floorColor = Color(palette.wood)
    val wallColor = Color(palette.warmWhite)
    val outlineColor = MaterialTheme.colorScheme.onSurface
    val gridColor = outlineColor.copy(alpha = 0.24f)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall
    val labels =
        mapOf(
            FurnitureType.Rug to
                textMeasurer.measure(stringResource(R.string.room_plan_rug), labelStyle),
            FurnitureType.Bed to
                textMeasurer.measure(stringResource(R.string.room_plan_bed), labelStyle),
            FurnitureType.Wardrobe to
                textMeasurer.measure(stringResource(R.string.room_plan_wardrobe), labelStyle),
        )

    Canvas(
        modifier
            .pointerInput(paddingPx) {
                detectTapGestures { tap ->
                    val room = currentLayout
                    val transform =
                        PlanTransform.fit(room.width, room.depth, size.toSize(), paddingPx)
                    val roomPoint = transform?.toRoom(tap)
                    selectedFurnitureId = roomPoint?.let { room.furnitureAt(it.x, it.y)?.id }
                }
            }
            .pointerInput(paddingPx) {
                var grab: FurnitureGrab? = null
                detectDragGestures(
                    orientationLock = null,
                    onDragStart = start@{ down, _, _ ->
                            grab = null
                            val room = currentLayout
                            val transform =
                                PlanTransform.fit(room.width, room.depth, size.toSize(), paddingPx)
                                    ?: return@start
                            val point = transform.toRoom(down.position)
                            val item = room.furnitureAt(point.x, point.y) ?: return@start
                            val grabOffset = point - Offset(item.centerX, item.centerZ)
                            grab = FurnitureGrab(item.id, down.id, grabOffset, transform)
                            selectedFurnitureId = item.id
                        },
                    onDrag = drag@{ change, _ ->
                            val activeGrab = grab ?: return@drag
                            if (change.id != activeGrab.pointerId) return@drag
                            val center =
                                activeGrab.transform.toRoom(change.position) - activeGrab.offset
                            moveFurniture(activeGrab.itemId, center.x, center.y)
                        },
                    onDragEnd = { grab = null },
                    onDragCancel = { grab = null },
                )
            }) {
            val transform =
                PlanTransform.fit(layout.width, layout.depth, size, paddingPx) ?: return@Canvas

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

        for (item in layout.furniture) {
            val center = transform.toCanvas(item.centerX, item.centerZ)
            val itemSize = Size(item.type.sizeX, item.type.sizeZ) * transform.pixelsPerMeter
            val topLeft = center - Offset(itemSize.width / 2, itemSize.height / 2)
            drawRect(
                color = Color(palette.furnitureColor(item.type)),
                topLeft = topLeft,
                size = itemSize,
            )
                if (item.id == selectedFurnitureId) {
                    drawRect(
                        color = outlineColor,
                        topLeft = topLeft,
                        size = itemSize,
                        style = Stroke(width = 3.dp.toPx()),
                    )
                }

            val label = labels.getValue(item.type)
            if (label.size.width <= itemSize.width && label.size.height <= itemSize.height) {
                drawText(
                    textLayoutResult = label,
                    color = outlineColor,
                    topLeft = center - Offset(label.size.width / 2f, label.size.height / 2f),
                )
            }
        }

        drawWalls(layout, transform, wallColor, outlineColor)
    }
}

private data class FurnitureGrab(
    val itemId: String,
    val pointerId: PointerId,
    val offset: Offset,
    val transform: PlanTransform,
)

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
