package com.dollhouse.lab.studio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dollhouse.lab.R
import com.dollhouse.lab.model.RoomLayout
import com.dollhouse.lab.model.defaultRoomLayout

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
        RoomPlan(
            layout = layout,
            onMoveFurniture = { id, centerX, centerZ ->
                layout = layout.moveFurniture(id, centerX, centerZ)
            },
            modifier =
                Modifier.weight(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
        )
    }
}
