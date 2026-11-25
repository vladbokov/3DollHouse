package com.dollhouse.lab.filament

import android.content.Context
import android.view.Choreographer
import android.view.Surface
import android.view.SurfaceView
import com.google.android.filament.Engine
import com.google.android.filament.Filament
import com.google.android.filament.Renderer
import com.google.android.filament.SwapChain
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper

/** [frameMs] is the average interval between drawn frames, [gpuMs] is one GPU sample. */
data class FrameStats(val fps: Float, val frameMs: Float, val gpuMs: Float?)

/**
 * Owns the Android surface and the frame loop. Scene setup belongs to the content created by
 * contentFactory. Use this class on the main thread.
 */
class FilamentHost<C : FilamentContent>(
    context: Context,
    requestedBackend: Engine.Backend,
    contentFactory: (Engine) -> C,
) {
    val surfaceView = SurfaceView(context)

    // Creates and owns the native graphics resources.
    private val engine =
        try {
            Engine.Builder().backend(requestedBackend).build()
        } catch (error: IllegalStateException) {
            if (requestedBackend != Engine.Backend.VULKAN) throw error
            // Retry with OpenGL ES if Vulkan initialization fails
            Engine.Builder().backend(Engine.Backend.OPENGL).build()
        }

    val actualBackend: Engine.Backend = engine.backend

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

    var onFrameStats: ((FrameStats?) -> Unit)? = null
        set(value) {
            val wasCollecting = field != null
            field = value
            if (wasCollecting && value == null) resetFrameStats()
        }

    private var statsWindowStartNanos = 0L
    private var statsFrames = 0
    private val frameInfos by
        lazy(LazyThreadSafetyMode.NONE) { Array(GPU_HISTORY_SIZE) { Renderer.FrameInfo() } }

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
                    countFrame(frameTimeNanos)
                }
            }
        }

    private fun countFrame(frameTimeNanos: Long) {
        val reportStats = onFrameStats ?: return
        if (statsWindowStartNanos == 0L) {
            statsWindowStartNanos = frameTimeNanos
            return
        }

        statsFrames++
        val elapsedNanos = frameTimeNanos - statsWindowStartNanos
        if (elapsedNanos < STATS_WINDOW_NANOS) return

        val fps = statsFrames * NANOS_PER_SECOND / elapsedNanos
        statsWindowStartNanos = frameTimeNanos
        statsFrames = 0
        reportStats(FrameStats(fps, frameMs = MILLIS_PER_SECOND / fps, gpuMs = latestGpuMs()))
    }

    private fun latestGpuMs(): Float? {
        val count = renderer.getFrameInfoHistory(frameInfos)
        for (index in 0 until count) {
            val nanos = frameInfos[index].gpuFrameDuration
            // Pending and unsupported timings have negative values
            if (nanos > 0) return nanos / NANOS_PER_MILLISECOND
        }
        return null
    }

    private fun resetFrameStats() {
        statsWindowStartNanos = 0L
        statsFrames = 0
        onFrameStats?.invoke(null)
    }

    private val surfaceCallback =
        object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                resetFrameStats()
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface, uiHelper.swapChainFlags)
                displayHelper.attach(renderer, surfaceView.display)
            }

            override fun onDetachedFromSurface() {
                resetFrameStats()
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
        resetFrameStats()
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
        const val STATS_WINDOW_NANOS = 500_000_000L
        const val GPU_HISTORY_SIZE = 8
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val MILLIS_PER_SECOND = 1_000f
        const val NANOS_PER_MILLISECOND = 1_000_000f

        init {
            // Load the native library before creating an Engine.
            Filament.init()
        }
    }
}
