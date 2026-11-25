package com.dollhouse.lab.filament

import android.opengl.Matrix
import com.dollhouse.lab.model.Furniture
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import com.google.android.filament.RenderableManager
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.Viewport
import java.nio.ByteBuffer
import kotlin.math.sqrt

/**
 * Sets up the room's scene, camera, lights and background. FilamentHost handles drawing frames. The
 * Engine comes from outside and is not ours to destroy.
 */
class ToyRoomScene(
    private val engine: Engine,
    materialData: ByteBuffer,
    room: ToyRoom,
    initialOrbit: OrbitCamera,
) : FilamentContent {
    // Holds the room boxes, lights and background
    private val scene = engine.createScene()

    // An entity is an ID. The camera component is attached to it below.
    private val cameraEntity = EntityManager.get().create()

    // Defines where we look from and how 3D projects onto the screen.
    private val camera = engine.createCamera(cameraEntity)

    // Aim at the center of the floor
    private val cameraTarget = doubleArrayOf(room.layout.width / 2.0, 0.0, room.layout.depth / 2.0)

    // Solid background color in linear RGBA. This does not light the objects.
    private val skybox = Skybox.Builder().color(0.21f, 0.64f, 0.49f, 1f).build(engine)

    // The compiled material shared by objects with the same surface model.
    private val material =
        Material.Builder().payload(materialData, materialData.remaining()).build(engine)

    // Keep each box's resources for cleanup
    private val boxEntities = mutableListOf<Int>()
    private val materialInstances = mutableListOf<MaterialInstance>()
    private val furnitureParents = mutableMapOf<String, Int>()

    // Last centers sent to Filament, so unchanged furniture is skipped
    private val appliedCenters = mutableMapOf<String, Pair<Float, Float>>()

    private class Wall(
        val centerX: Float,
        val centerZ: Float,
        val outwardX: Float,
        val outwardZ: Float,
        val entities: IntArray,
        var visible: Boolean = true,
    )

    private val walls = mutableListOf<Wall>()

    // Unit cube geometry, scaled into each room box
    private val boxMesh = BoxMesh(engine)

    // Entity carrying the directional light component
    private val sunEntity = EntityManager.get().create()

    // Simple uniform fill light without an environment texture
    private val ambientLight =
        IndirectLight.Builder()
            .irradiance(1, floatArrayOf(1f, 1f, 1f))
            .intensity(12_000f)
            .build(engine)

    // Connects the scene, camera and drawing area. This is a Filament View, not an Android View.
    override val view: View = engine.createView()

    init {
        scene.skybox = skybox
        scene.indirectLight = ambientLight
        view.scene = scene
        view.camera = camera

        addBox(room.floor)
        room.layout.furniture.forEach { item -> addFurniture(item, room.furnitureBoxes(item)) }

        addWall(listOf(room.wallLeft), outwardX = -1f, outwardZ = 0f)
        addWall(listOf(room.wallRight), outwardX = 1f, outwardZ = 0f)
        addWall(room.wallBackParts + room.windowFrameParts, outwardX = 0f, outwardZ = -1f)
        addWall(room.wallFrontParts, outwardX = 0f, outwardZ = 1f)
        setCamera(initialOrbit)

        // Parallel rays like sunlight, coming from above and in front, slightly from the right
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1f, 0.96f, 0.9f)
            .intensity(100_000f)
            .direction(SUN_DIRECTION[0], SUN_DIRECTION[1], SUN_DIRECTION[2])
            .build(engine, sunEntity)

        scene.addEntity(sunEntity)
    }

    fun setCamera(orbit: OrbitCamera) {
        val eye = orbit.eye(cameraTarget)
        camera.lookAt(eye = eye, target = cameraTarget)

        for (wall in walls) {
            // A positive dot product puts the camera on the outer side of the wall
            val cameraOutside =
                (eye[0] - wall.centerX) * wall.outwardX + (eye[2] - wall.centerZ) * wall.outwardZ >
                    0.0
            val visible = !cameraOutside
            if (visible == wall.visible) continue

            if (visible) {
                scene.addEntities(wall.entities)
            } else {
                scene.removeEntities(wall.entities)
            }
            wall.visible = visible
        }
    }

    internal fun updateFurniture(furniture: List<Furniture>) {
        val transformManager = engine.transformManager
        for (item in furniture) {
            val center = item.centerX to item.centerZ
            if (appliedCenters[item.id] == center) continue

            val parent = furnitureParents[item.id] ?: continue
            transformManager.setTransform(
                transformManager.getInstance(parent),
                translationTransform(item.centerX, 0f, item.centerZ),
            )
            appliedCenters[item.id] = center
        }
    }

    private fun addWall(boxes: List<RoomBox>, outwardX: Float, outwardZ: Float) {
        // The first box is a wall segment, so its center lies on the wall's midplane
        val anchor = boxes.first()
        walls +=
            Wall(
                centerX = anchor.centerX,
                centerZ = anchor.centerZ,
                outwardX = outwardX,
                outwardZ = outwardZ,
                entities = boxes.map { addBox(it) }.toIntArray(),
            )
    }

    private fun addFurniture(item: Furniture, boxes: List<RoomBox>) {
        val parent = EntityManager.get().create()
        val transformManager = engine.transformManager
        transformManager.create(parent, 0, translationTransform(item.centerX, 0f, item.centerZ))
        furnitureParents[item.id] = parent
        appliedCenters[item.id] = item.centerX to item.centerZ
        boxes.forEach { addBox(it, parent) }
    }

    private fun addBox(box: RoomBox, parent: Int? = null): Int {
        val instance =
            material.createInstance().apply {
                setBaseColor(box.color)
                setParameter(ColorRoughnessLit.ROUGHNESS, 0.7f)
            }
        materialInstances += instance

        val entity = EntityManager.get().create()

        val transformManager = engine.transformManager
        val parentInstance = parent?.let(transformManager::getInstance) ?: 0
        transformManager.create(entity, parentInstance, unitCubeTransform(box))

        // Shared geometry, separate material instance
        RenderableManager.Builder(1)
            .boundingBox(boxMesh.boundingBox)
            .geometry(
                0,
                RenderableManager.PrimitiveType.TRIANGLES,
                boxMesh.vertexBuffer,
                boxMesh.indexBuffer,
            )
            .material(0, instance)
            .build(engine, entity)

        scene.addEntity(entity)
        boxEntities += entity
        return entity
    }

    /** Updates the drawing area when the surface changes size. Width and height are in pixels. */
    override fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        // Viewport takes the origin and size of the drawing rectangle.
        view.viewport = Viewport(0, 0, width, height)
        // The projection must follow the aspect ratio, or objects look stretched.
        val aspect = width.toDouble() / height
        camera.setProjection(FOV_DEGREES, aspect, NEAR_METERS, FAR_METERS, Camera.Fov.VERTICAL)
    }

    /** Call once after drawing stops, before destroying the Engine. */
    override fun destroy() {
        // Native resources need explicit cleanup. Release the View before the objects it uses.
        engine.destroyView(view)
        // Release boxes before their shared geometry and materials
        for (entity in boxEntities) {
            engine.destroyEntity(entity)
            EntityManager.get().destroy(entity)
        }

        // Release furniture parents after the child boxes
        for (entity in furnitureParents.values) {
            engine.destroyEntity(entity)
            EntityManager.get().destroy(entity)
        }

        boxMesh.destroy(engine)
        engine.destroyEntity(sunEntity)
        EntityManager.get().destroy(sunEntity)
        engine.destroyScene(scene)
        engine.destroyIndirectLight(ambientLight)
        materialInstances.forEach { engine.destroyMaterialInstance(it) }
        engine.destroyMaterial(material)
        engine.destroySkybox(skybox)
        // The camera component and its entity ID are released separately.
        engine.destroyCameraComponent(cameraEntity)
        EntityManager.get().destroy(cameraEntity)
    }

    private companion object {
        const val FOV_DEGREES = 45.0
        const val NEAR_METERS = 0.1
        const val FAR_METERS = 50.0

        // Filament expects a unit vector
        val SUN_DIRECTION = normalized(-0.5f, -1f, -0.6f)

        fun normalized(x: Float, y: Float, z: Float): FloatArray {
            val length = sqrt(x * x + y * y + z * z)
            return floatArrayOf(x / length, y / length, z / length)
        }

        // Keeps +Y as the up direction
        fun Camera.lookAt(eye: DoubleArray, target: DoubleArray) {
            lookAt(eye[0], eye[1], eye[2], target[0], target[1], target[2], 0.0, 1.0, 0.0)
        }

        fun translationTransform(x: Float, y: Float, z: Float): FloatArray =
            FloatArray(16).also {
                Matrix.setIdentityM(it, 0)
                Matrix.translateM(it, 0, x, y, z)
            }

        // Each Matrix call multiplies on the right, so scale applies first
        fun unitCubeTransform(box: RoomBox): FloatArray =
            translationTransform(box.centerX, box.centerY, box.centerZ).also {
                Matrix.scaleM(it, 0, box.sizeX, box.sizeY, box.sizeZ)
            }
    }
}
