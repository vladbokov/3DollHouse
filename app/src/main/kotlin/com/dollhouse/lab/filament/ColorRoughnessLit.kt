package com.dollhouse.lab.filament

import com.google.android.filament.Colors
import com.google.android.filament.MaterialInstance

/**
 * Names from app/src/main/materials/color_roughness_lit.mat. Keep them in sync with the .mat file:
 * a mismatch is only caught at runtime.
 */
object ColorRoughnessLit {
    const val ASSET_PATH = "materials/color_roughness_lit.filamat"

    const val BASE_COLOR = "baseColor"
    const val ROUGHNESS = "roughness"
}

// Splits 0xRRGGBB into channels from 0 to 1. SRGB makes Filament convert them to linear
fun MaterialInstance.setBaseColor(rgb: Int) {
    val red = (rgb shr 16 and 0xFF) / 255f
    val green = (rgb shr 8 and 0xFF) / 255f
    val blue = (rgb and 0xFF) / 255f
    setParameter(ColorRoughnessLit.BASE_COLOR, Colors.RgbType.SRGB, red, green, blue)
}
