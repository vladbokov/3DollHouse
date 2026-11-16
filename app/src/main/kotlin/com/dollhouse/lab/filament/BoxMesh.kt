package com.dollhouse.lab.filament

import com.google.android.filament.Box
import com.google.android.filament.Engine
import com.google.android.filament.IndexBuffer
import com.google.android.filament.MathUtils
import com.google.android.filament.VertexBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Geometry for a cube of size 1 x 1 x 1, centered at the origin. Each face has its own four
 * vertices so lighting keeps the edges sharp. Objects can share these buffers and use transforms to
 * change size and position
 */
class BoxMesh(engine: Engine) {
    // Coordinates and surface orientations for 24 vertices
    val vertexBuffer: VertexBuffer

    // Vertex references for 12 triangles, three indices per triangle
    val indexBuffer: IndexBuffer

    // Center followed by half-size. This describes the bounds, not the visible surface
    val boundingBox = Box(0f, 0f, 0f, 0.5f, 0.5f, 0.5f)

    init {
        val positions = directBuffer(VERTEX_COUNT * 3 * Float.SIZE_BYTES).asFloatBuffer()
        val tangents = directBuffer(VERTEX_COUNT * 4 * Float.SIZE_BYTES).asFloatBuffer()
        val indices = directBuffer(INDEX_COUNT * Short.SIZE_BYTES).asShortBuffer()
        val quaternion = FloatArray(4)

        FACES.forEachIndexed { face, (normal, tangent) ->
            // The cross product gives the third direction perpendicular to the first two
            val bitangent = cross(normal, tangent)

            // Filament's TANGENTS attribute stores the surface orientation as a quaternion
            // packTangentFrame converts our three directions into those four numbers
            MathUtils.packTangentFrame(
                tangent[0],
                tangent[1],
                tangent[2],
                bitangent[0],
                bitangent[1],
                bitangent[2],
                normal[0],
                normal[1],
                normal[2],
                quaternion,
            )

            // Start at the face center, then move half a unit along each face axis
            // Corners run counter-clockwise when viewed from outside the cube
            for ((tangentSign, bitangentSign) in CORNERS) {
                for (axis in 0..2) {
                    positions.put(
                        0.5f *
                            (normal[axis] +
                                tangentSign * tangent[axis] +
                                bitangentSign * bitangent[axis])
                    )
                }
                tangents.put(quaternion)
            }

            // Each face uses local triangles 0-1-2 and 0-2-3
            // Offset their indices to point into the full vertex buffer
            val firstVertex = face * 4
            for (index in intArrayOf(0, 1, 2, 0, 2, 3)) {
                indices.put((firstVertex + index).toShort())
            }
        }

        // Switch the filled buffers from writing to reading
        positions.flip()
        tangents.flip()
        indices.flip()

        vertexBuffer =
            VertexBuffer.Builder()
                .vertexCount(VERTEX_COUNT)
                .bufferCount(2)
                .attribute(
                    VertexBuffer.VertexAttribute.POSITION,
                    0,
                    VertexBuffer.AttributeType.FLOAT3,
                )
                .attribute(
                    VertexBuffer.VertexAttribute.TANGENTS,
                    1,
                    VertexBuffer.AttributeType.FLOAT4,
                )
                .build(engine)
        vertexBuffer.setBufferAt(engine, 0, positions)
        vertexBuffer.setBufferAt(engine, 1, tangents)

        indexBuffer =
            IndexBuffer.Builder()
                .indexCount(INDEX_COUNT)
                .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                .build(engine)
        indexBuffer.setBuffer(engine, indices)
    }

    /** Release after destroying all renderable components that use these buffers. */
    fun destroy(engine: Engine) {
        engine.destroyVertexBuffer(vertexBuffer)
        engine.destroyIndexBuffer(indexBuffer)
    }

    private companion object {
        const val VERTEX_COUNT = 24
        const val INDEX_COUNT = 36

        // Each face has an outward normal and a tangent running along its surface
        val FACES =
            listOf(
                floatArrayOf(1f, 0f, 0f) to floatArrayOf(0f, 0f, -1f), // +X
                floatArrayOf(-1f, 0f, 0f) to floatArrayOf(0f, 0f, 1f), // -X
                floatArrayOf(0f, 1f, 0f) to floatArrayOf(1f, 0f, 0f), // +Y
                floatArrayOf(0f, -1f, 0f) to floatArrayOf(1f, 0f, 0f), // -Y
                floatArrayOf(0f, 0f, 1f) to floatArrayOf(1f, 0f, 0f), // +Z
                floatArrayOf(0f, 0f, -1f) to floatArrayOf(-1f, 0f, 0f), // -Z
            )

        // Four corners expressed as signs along the two face axes
        val CORNERS = listOf(-1f to -1f, 1f to -1f, 1f to 1f, -1f to 1f)

        // Native code reads these bytes directly using the device's byte order
        fun directBuffer(bytes: Int): ByteBuffer =
            ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder())

        fun cross(a: FloatArray, b: FloatArray) =
            floatArrayOf(
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0],
            )
    }
}
