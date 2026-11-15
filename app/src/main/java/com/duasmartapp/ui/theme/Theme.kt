package com.duasmartapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.readonly
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DuaSmartAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.ColorScheme.light(
            surface = White,
            onSurface = Ink,
            primary = Ink
        ),
        typography = Typography(
            h1 = DuaTypography.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 22.sp),
            body1 = Body,
            body2 = BodyMuted,
            labelLarge = ButtonText
        ),
        content = content
    )
}

val LocalDuaColors = readonly(DuaColors)

private object DuaColors {
    val ink = Ink
    val slate700 = Slate700
    val slate500 = Slate500
    val glassFill = GlassFillLight
    val glassBorder = GlassBorder
    val emerald = Emerald
    val emeraldFill = EmeraldFill
    val emeraldBorder = EmeraldBorder
    val crimson = Crimson
    val crimsonFill = CrimsonFill
    val crimsonBorder = CrimsonBorder
    val amber = Amber
    val amberFill = AmberFill
    val amberBorder = AmberBorder
}
