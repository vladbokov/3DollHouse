package com.dollhouse.lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.dollhouse.lab.filament.FilamentLabScreen
import com.dollhouse.lab.home.HomeScreen
import com.dollhouse.lab.ui.theme.DollhouseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DollhouseTheme {
                // rememberSaveable: the current screen survives rotation and process death.
                var screen by rememberSaveable { mutableStateOf(Screen.Home) }
                val goHome = { screen = Screen.Home }

                BackHandler(enabled = screen != Screen.Home, onBack = goHome)

                when (screen) {
                    Screen.Home -> HomeScreen(onOpenFilamentLab = { screen = Screen.FilamentLab })
                    Screen.FilamentLab -> FilamentLabScreen(onBack = goHome)
                }
            }
        }
    }
}
