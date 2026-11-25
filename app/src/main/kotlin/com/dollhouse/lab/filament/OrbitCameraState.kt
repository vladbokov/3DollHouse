package com.dollhouse.lab.filament

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset

private const val DEGREES_PER_PIXEL = 0.25

private val OrbitSaver =
    listSaver<OrbitCamera, Double>(
        save = { listOf(it.yawDeg, it.pitchDeg, it.distance) },
        restore = { (yaw, pitch, distance) -> OrbitCamera(yaw, pitch, distance) },
    )

@Composable
internal fun rememberRoomOrbitCamera(): MutableState<OrbitCamera> =
    rememberSaveable(stateSaver = OrbitSaver) {
        // Start above the front wall
        mutableStateOf(OrbitCamera(yawDeg = 0.0, pitchDeg = 49.4, distance = 9.22))
    }

internal fun OrbitCamera.applyGesture(pan: Offset, zoom: Float): OrbitCamera =
    rotate(deltaYawDeg = -pan.x * DEGREES_PER_PIXEL, deltaPitchDeg = pan.y * DEGREES_PER_PIXEL)
        .zoom(zoom)
