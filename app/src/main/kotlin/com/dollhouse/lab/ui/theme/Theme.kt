package com.dollhouse.lab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Colors =
    lightColorScheme(
        primary = Mint,
        onPrimary = Ink,
        secondary = Coral,
        onSecondary = Ink,
        background = Cream,
        onBackground = Ink,
        surface = Cream,
        onSurface = Ink,
    )

@Composable
fun DollhouseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
