package com.dollhouse.lab.filament

import com.google.android.filament.Camera
import com.google.android.filament.Colors
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Material
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.Viewport
import java.nio.ByteBuffer

/**
 * Sets up the room's scene, camera and background. FilamentHost handles drawing frames. The Engine
 * comes from outside and is not ours to destroy.
 */
class ToyRoomScene(
    private val engine: Engine,
    materialData: ByteBuffer,
) : FilamentContent {
    // Holds objects, lights and the background. For now, only the background is set.
    private val scene = engine.createScene()

    // An entity is an ID. The camera component is attached to it below.
    private val cameraEntity = EntityManager.get().create()

    // Defines where we look from and how 3D projects onto the screen.
    private val camera = engine.createCamera(cameraEntity)

    // Solid background color in linear RGBA. This does not light the objects.
    private val skybox = Skybox.Builder().color(0.21f, 0.64f, 0.49f, 1f).build(engine)

    // The compiled material shared by objects with the same surface model.
    private val material = Material.Builder()
        .payload(materialData, materialData.remaining())
        .build(engine)

    // One set of surface values. Not attached to any object yet.
    private val materialInstance = material.createInstance().apply {
        setParameter(ColorRoughnessLit.BASE_COLOR, Colors.RgbType.SRGB, 0.85f, 0.25f, 0.15f)
        setParameter(ColorRoughnessLit.ROUGHNESS, 0.7f)
    }

    // Connects the scene, camera and drawing area. This is a Filament View, not an Android View.
    override val view: View = engine.createView()

    init {
        scene.skybox = skybox
        view.scene = scene
        view.camera = camera
        // Look from slightly above and in front of the room center, with +Y up.
        camera.lookAt(0.0, 1.5, 4.0, 0.0, 0.5, 0.0, 0.0, 1.0, 0.0)
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
        engine.destroyScene(scene)
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
    }
}
