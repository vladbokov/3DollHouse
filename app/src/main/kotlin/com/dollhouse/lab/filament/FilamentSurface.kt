package com.dollhouse.lab.filament

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dollhouse.lab.model.RoomLayout
import com.dollhouse.lab.ui.theme.ToyRoomPalette
import com.google.android.filament.Engine

/**
 * Places the surface in Compose and follows the screen lifecycle.
 *
 * Uses the first layout to build the scene. Later layouts update furniture positions.
 */
@Composable
internal fun FilamentSurface(
    layout: RoomLayout,
    requestedBackend: Engine.Backend,
    orbit: OrbitCamera,
    onCameraGesture: (Offset, Float) -> Unit,
    onActualBackend: ((Engine.Backend) -> Unit)? = null,
    onFrameStats: ((FrameStats?) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Studio preview cannot create a native rendering surface.
    if (LocalInspectionMode.current) {
        Box(modifier, contentAlignment = Alignment.Center) { Text("Filament surface") }
        return
    }

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Keep the Engine and scene while furniture moves
    val filamentHost = remember {
        val palette = ToyRoomPalette.fromResources(context)
        val room = ToyRoom(layout, palette)
        FilamentHost(context, requestedBackend) { engine ->
            ToyRoomScene(
                engine,
                context.assets.readDirectBuffer(ColorRoughnessLit.ASSET_PATH),
                room,
                initialOrbit = orbit,
            )
        }
    }

    SideEffect { filamentHost.onFrameStats = onFrameStats }

    // Released together with the remembered host, so a destroyed host is never reused.
    DisposableEffect(filamentHost) {
        onActualBackend?.invoke(filamentHost.actualBackend)
        onDispose {
            filamentHost.onFrameStats = null
            filamentHost.destroy()
        }
    }

    // Re-subscribes when the lifecycle owner changes without destroying the host.
    DisposableEffect(filamentHost, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> filamentHost.resume()
                Lifecycle.Event.ON_PAUSE -> filamentHost.pause()
                else -> Unit
            }
        }

        // Adding the observer brings it up to the owner's current lifecycle state.
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            filamentHost.pause()
        }
    }

    val scene = filamentHost.content
    LaunchedEffect(scene, orbit) { scene.setCamera(orbit) }
    LaunchedEffect(scene, layout) { scene.updateFurniture(layout.furniture) }
    val handleCameraGesture by rememberUpdatedState(onCameraGesture)

    Box(
        modifier =
            modifier.pointerInput(scene) {
                detectTransformGestures { _, pan, zoom, _ -> handleCameraGesture(pan, zoom) }
            }) {
            AndroidView(factory = { filamentHost.surfaceView }, modifier = Modifier.fillMaxSize())
    }
}
