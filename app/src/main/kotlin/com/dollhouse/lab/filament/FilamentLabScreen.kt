package com.dollhouse.lab.filament

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dollhouse.lab.R
import com.dollhouse.lab.model.defaultRoomLayout
import com.dollhouse.lab.ui.theme.DollhouseTheme
import com.google.android.filament.Engine

private enum class LabBackend(@StringRes val labelRes: Int, val filament: Engine.Backend) {
    OpenGlEs(R.string.filament_backend_opengl_es, Engine.Backend.OPENGL),
    Vulkan(R.string.filament_backend_vulkan, Engine.Backend.VULKAN),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentLabScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var requested by rememberSaveable { mutableStateOf(LabBackend.OpenGlEs) }
    var actual by remember(requested) { mutableStateOf<Engine.Backend?>(null) }
    var frameStats by remember(requested) { mutableStateOf<FrameStats?>(null) }

    // Keep the camera across screen recreation and backend changes
    var orbit by rememberRoomOrbitCamera()

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
        bottomBar = {
            BackendControls(
                requested = requested,
                actual = actual,
                frameStats = frameStats,
                onSelect = { requested = it },
            )
        },
    ) { padding ->
        // A new backend needs a new Engine and SurfaceView
        key(requested) {
            FilamentSurface(
                layout = defaultRoomLayout,
                requestedBackend = requested.filament,
                orbit = orbit,
                onCameraGesture = { pan, zoom -> orbit = orbit.applyGesture(pan, zoom) },
                onActualBackend = { actual = it },
                onFrameStats = { frameStats = it },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackendControls(
    requested: LabBackend,
    actual: Engine.Backend?,
    frameStats: FrameStats?,
    onSelect: (LabBackend) -> Unit,
) {
    Surface {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                LabBackend.entries.forEachIndexed { index, backend ->
                    SegmentedButton(
                        selected = backend == requested,
                        onClick = { onSelect(backend) },
                        shape = SegmentedButtonDefaults.itemShape(index, LabBackend.entries.size),
                    ) {
                        Text(stringResource(backend.labelRes))
                    }
                }
            }

            val status =
                if (actual == null) {
                    stringResource(R.string.filament_backend_starting)
                } else {
                    val backend = LabBackend.entries.firstOrNull { it.filament == actual }
                    val label = backend?.let { stringResource(it.labelRes) } ?: actual.name
                    if (actual == requested.filament) {
                        stringResource(R.string.filament_backend_active, label)
                    } else {
                        stringResource(
                            R.string.filament_backend_fallback,
                            label,
                            stringResource(requested.labelRes),
                        )
                    }
                }
            Text(status)
            Text(frameStatsLabel(frameStats))
        }
    }
}

@Composable
private fun frameStatsLabel(stats: FrameStats?): String {
    if (stats == null) return stringResource(R.string.filament_frame_stats_pending)

    val gpuMs = stats.gpuMs
    return if (gpuMs == null) {
        stringResource(R.string.filament_frame_stats, stats.fps, stats.frameMs)
    } else {
        stringResource(R.string.filament_frame_stats_with_gpu, stats.fps, stats.frameMs, gpuMs)
    }
}

@Preview(showBackground = true)
@Composable
private fun FilamentLabScreenPreview() {
    DollhouseTheme { FilamentLabScreen(onBack = {}) }
}
