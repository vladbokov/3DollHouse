package com.dollhouse.lab.filament

import android.content.Context
import android.view.Choreographer
import android.view.Surface
import android.view.SurfaceView
import com.google.android.filament.Engine
import com.google.android.filament.Filament
import com.google.android.filament.SwapChain
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper

/**
 * Owns the Android surface and the frame loop. Scene setup belongs to the content created by
 * contentFactory. Use this class on the main thread.
 */
class FilamentHost<C : FilamentContent>(context: Context, contentFactory: (Engine) -> C) {
    val surfaceView = SurfaceView(context)

    // Creates and owns the native graphics resources.
    private val engine = Engine.Builder().backend(Engine.Backend.OPENGL).build()

    // Generates drawing commands for each frame.
    private val renderer = engine.createRenderer()
    val content: C = contentFactory(engine)

    private val choreographer = Choreographer.getInstance()

    // Follows the Android surface lifecycle.
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private val displayHelper = DisplayHelper(context)

    // Connects Filament to the current surface. Absent when the surface is gone.
    private var swapChain: SwapChain? = null
    private var running = false
    private var destroyed = false

    private val frameCallback =
        object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!running || destroyed) return
                choreographer.postFrameCallback(this)

                val target = swapChain ?: return
                if (!uiHelper.isReadyToRender) return

                // Filament can ask us to skip a frame when the GPU falls behind.
                if (renderer.beginFrame(target, frameTimeNanos)) {
                    renderer.render(content.view)
                    renderer.endFrame()
                }
            }
        }

    private val surfaceCallback =
        object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface, uiHelper.swapChainFlags)
                displayHelper.attach(renderer, surfaceView.display)
            }

            override fun onDetachedFromSurface() {
                displayHelper.detach()
                swapChain?.let {
                    engine.destroySwapChain(it)
                    // Finish using the surface before Android releases it.
                    engine.flushAndWait()
                    swapChain = null
                }
            }

            override fun onResized(width: Int, height: Int) {
                content.resize(width, height)
                // Finish pending work before returning from the resize callback.
                engine.flushAndWait()
            }
        }

    init {
        uiHelper.renderCallback = surfaceCallback
        uiHelper.attachTo(surfaceView)
    }

    /** Starts requesting frames while the screen is active. */
    fun resume() {
        if (destroyed || running) return
        running = true
        choreographer.postFrameCallback(frameCallback)
    }

    fun pause() {
        running = false
        choreographer.removeFrameCallback(frameCallback)
    }

    /** Stops drawing and releases resources. The Engine is destroyed last. */
    fun destroy() {
        if (destroyed) return
        destroyed = true
        pause()
        uiHelper.detach()
        content.destroy()
        engine.destroyRenderer(renderer)
        engine.destroy()
    }

    private companion object {
        init {
            // Load the native library before creating an Engine.
            Filament.init()
        }
    }
}
