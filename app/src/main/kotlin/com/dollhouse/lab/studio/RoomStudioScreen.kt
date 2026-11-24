package com.dollhouse.lab.studio

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dollhouse.lab.R
import com.dollhouse.lab.filament.FilamentSurface
import com.dollhouse.lab.filament.applyGesture
import com.dollhouse.lab.filament.rememberRoomOrbitCamera
import com.dollhouse.lab.model.RoomLayout
import com.dollhouse.lab.model.defaultRoomLayout
import com.google.android.filament.Engine

private val RoomLayoutSaver =
    listSaver<RoomLayout, Any>(
        save = { layout -> layout.furniture.flatMap { listOf(it.id, it.centerX, it.centerZ) } },
        restore = { saved ->
            saved.chunked(3).fold(defaultRoomLayout) { layout, (id, centerX, centerZ) ->
                layout.moveFurniture(id as String, centerX as Float, centerZ as Float)
            }
        },
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomStudioScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var layout by
        rememberSaveable(stateSaver = RoomLayoutSaver) { mutableStateOf(defaultRoomLayout) }
    var orbit by rememberRoomOrbitCamera()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val panels: @Composable (Modifier) -> Unit = { panelModifier ->
        FilamentSurface(
            layout = layout,
            requestedBackend = Engine.Backend.OPENGL,
            orbit = orbit,
            onCameraGesture = { pan, zoom -> orbit = orbit.applyGesture(pan, zoom) },
            modifier = panelModifier,
        )
        RoomPlan(
            layout = layout,
            onMoveFurniture = { id, centerX, centerZ ->
                layout = layout.moveFurniture(id, centerX, centerZ)
            },
            modifier = panelModifier,
        )
    }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.room_studio_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            },
        )
        val contentModifier =
            Modifier.weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
        if (isLandscape) {
            Row(contentModifier) { panels(Modifier.weight(1f).fillMaxHeight()) }
        } else {
            Column(contentModifier) { panels(Modifier.weight(1f).fillMaxWidth()) }
        }
    }
}
