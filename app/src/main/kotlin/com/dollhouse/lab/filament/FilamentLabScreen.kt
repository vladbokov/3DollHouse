package com.dollhouse.lab.filament

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dollhouse.lab.R
import com.dollhouse.lab.ui.theme.DollhouseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentLabScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.filament_lab_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        FilamentSurface(modifier = Modifier.fillMaxSize().padding(padding))
    }
}

/** Places the surface in Compose and follows the screen lifecycle. */
@Composable
private fun FilamentSurface(modifier: Modifier = Modifier) {
    // Studio preview cannot create a native rendering surface.
    if (LocalInspectionMode.current) {
        Box(modifier, contentAlignment = Alignment.Center) { Text("Filament surface") }
        return
    }

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Keep the Engine and scene across recompositions.
    val filamentHost = remember {
        FilamentHost(context) { engine ->
            ToyRoomScene(engine, context.assets.readDirectBuffer(ColorRoughnessLit.ASSET_PATH))
        }
    }

    // Released together with the remembered host, so a destroyed host is never reused.
    DisposableEffect(filamentHost) { onDispose { filamentHost.destroy() } }

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

    AndroidView(factory = { filamentHost.surfaceView }, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun FilamentLabScreenPreview() {
    DollhouseTheme { FilamentLabScreen(onBack = {}) }
}
