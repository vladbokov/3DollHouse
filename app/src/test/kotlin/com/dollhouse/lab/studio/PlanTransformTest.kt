package com.dollhouse.lab.studio

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanTransformTest {
    @Test
    fun `converting to pixels and back preserves room coordinates`() {
        val transform = fitRoom(Size(332f, 640f))

        val roomPoint = transform.toRoom(transform.toCanvas(0.55f, 1.05f))

        assertEquals(0.55f, roomPoint.x, 0.0001f)
        assertEquals(1.05f, roomPoint.y, 0.0001f)
    }

    @Test
    fun `portrait plan fits the width and is centered vertically`() {
        val transform = fitRoom(Size(332f, 640f))

        assertEquals(Offset(16f, 120f), transform.toCanvas(0f, 0f))
        assertEquals(Offset(316f, 520f), transform.toCanvas(3f, 4f))
    }

    @Test
    fun `landscape plan fits the height and is centered horizontally`() {
        val transform = fitRoom(Size(640f, 432f))

        assertEquals(Offset(170f, 16f), transform.toCanvas(0f, 0f))
        assertEquals(Offset(470f, 416f), transform.toCanvas(3f, 4f))
    }

    private fun fitRoom(viewport: Size): PlanTransform =
        checkNotNull(
            PlanTransform.fit(roomWidth = 3f, roomDepth = 4f, viewport = viewport, paddingPx = 16f))
}
