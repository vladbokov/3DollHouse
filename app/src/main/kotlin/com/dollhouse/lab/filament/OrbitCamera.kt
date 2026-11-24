package com.dollhouse.lab.filament

import kotlin.math.cos
import kotlin.math.sin

data class OrbitCamera(
    val yawDeg: Double,
    val pitchDeg: Double,
    val distance: Double,
) {
    fun rotate(deltaYawDeg: Double, deltaPitchDeg: Double): OrbitCamera =
        copy(
            yawDeg = (yawDeg + deltaYawDeg).mod(FULL_TURN_DEGREES),
            pitchDeg = (pitchDeg + deltaPitchDeg).coerceIn(MIN_PITCH_DEGREES, MAX_PITCH_DEGREES),
        )

    fun eye(target: DoubleArray): DoubleArray {
        val yaw = Math.toRadians(yawDeg)
        val pitch = Math.toRadians(pitchDeg)

        // Project the orbit radius onto the horizontal XZ plane
        val horizontal = distance * cos(pitch)

        return doubleArrayOf(
            target[0] + horizontal * sin(yaw),
            target[1] + distance * sin(pitch),
            target[2] + horizontal * cos(yaw),
        )
    }

    private companion object {
        const val FULL_TURN_DEGREES = 360.0

        // Stay above the floor and avoid looking straight down along the up axis
        const val MIN_PITCH_DEGREES = 5.0
        const val MAX_PITCH_DEGREES = 85.0
    }
}
