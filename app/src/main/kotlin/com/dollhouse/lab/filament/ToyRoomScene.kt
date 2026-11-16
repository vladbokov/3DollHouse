package com.dollhouse.lab.filament

import com.google.android.filament.Camera
import com.google.android.filament.Colors
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Material
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
class ToyRoomScene(private val engine: Engine, materialData: ByteBuffer) : FilamentContent {
    // Holds the cube, lights and background
    private val scene = engine.createScene()

    // An entity is an ID. The camera component is attached to it below.
    private val cameraEntity = EntityManager.get().create()

    // Defines where we look from and how 3D projects onto the screen.
    private val camera = engine.createCamera(cameraEntity)

    // Solid background color in linear RGBA. This does not light the objects.
    private val skybox = Skybox.Builder().color(0.21f, 0.64f, 0.49f, 1f).build(engine)

    // The compiled material shared by objects with the same surface model.
    private val material =
        Material.Builder().payload(materialData, materialData.remaining()).build(engine)

    // Surface values used by the cube
    private val materialInstance =
        material.createInstance().apply {
            setParameter(ColorRoughnessLit.BASE_COLOR, Colors.RgbType.SRGB, 0.85f, 0.25f, 0.15f)
            setParameter(ColorRoughnessLit.ROUGHNESS, 0.7f)
        }

    // Shared cube geometry
    private val boxMesh = BoxMesh(engine)

    // ID of the object that uses the geometry and material
    private val cubeEntity = EntityManager.get().create()

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
        // Look from slightly above and in front of the room center, with +Y up.
        camera.lookAt(0.0, 1.5, 4.0, 0.0, 0.5, 0.0, 0.0, 1.0, 0.0)

        // One renderable primitive with its geometry and material
        RenderableManager.Builder(1)
            .boundingBox(boxMesh.boundingBox)
            .geometry(
                0,
                RenderableManager.PrimitiveType.TRIANGLES,
                boxMesh.vertexBuffer,
                boxMesh.indexBuffer,
            )
            .material(0, materialInstance)
            .build(engine, cubeEntity)

        scene.addEntity(cubeEntity)

        // Parallel rays like sunlight, coming from above and in front, slightly from the right
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1f, 0.96f, 0.9f)
            .intensity(100_000f)
            .direction(SUN_DIRECTION[0], SUN_DIRECTION[1], SUN_DIRECTION[2])
            .build(engine, sunEntity)

        scene.addEntity(sunEntity)
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
        // Release the object before its material and geometry
        engine.destroyEntity(cubeEntity)
        EntityManager.get().destroy(cubeEntity)

        boxMesh.destroy(engine)
        engine.destroyEntity(sunEntity)
        EntityManager.get().destroy(sunEntity)
        engine.destroyScene(scene)
        engine.destroyIndirectLight(ambientLight)
        engine.destroyMaterialInstance(materialInstance)
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
    }
}
