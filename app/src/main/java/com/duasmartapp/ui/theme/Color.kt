package com.duasmartapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------
// Brand & ink (deep charcoal/slate for high-contrast readability)
// ---------------------------------------------------------------
val Ink = Color(0xFF0F172A)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF64748B)
val Slate300 = Color(0xFFCBD5E1)
val Slate100 = Color(0xFFF1F5F9)
val White = Color(0xFFFFFFFF)

// ---------------------------------------------------------------
// Background canvas: cool slate / silver-grey mesh gradient
// ---------------------------------------------------------------
val BgStop1 = Color(0xFFCBD5E1)
val BgStop2 = Color(0xFFE2E8F0)
val BgStop3 = Color(0xFF94A3B8)

// ---------------------------------------------------------------
// Frosted glass surfaces
// ---------------------------------------------------------------
val GlassFillLight = Color(0xC7FFFFFF) // rgba(255,255,255,0.78)
val GlassFillSoft = Color(0xA6FFFFFF)  // rgba(255,255,255,0.65)
val GlassBorder = Color(0xD9FFFFFF)    // rgba(255,255,255,0.85)
val GlassShadow = Color(0x141E293B)    // rgba(30,41,59,0.08)

// ---------------------------------------------------------------
// Status pills
// ---------------------------------------------------------------
val Emerald = Color(0xFF047857)
val EmeraldFill = Color(0x2E10B981)   // rgba(16,185,129,0.18)
val EmeraldBorder = Color(0x8010B981)

val Crimson = Color(0xFFB91C1C)
val CrimsonFill = Color(0x2EEF4444)   // rgba(239,68,68,0.18)
val CrimsonBorder = Color(0x80EF4444)

val Amber = Color(0xFFB45309)
val AmberFill = Color(0x2EF59E0B)     // rgba(245,158,11,0.18)
val AmberBorder = Color(0x80F59E0B)

// ---------------------------------------------------------------
// Background mesh brush
// ---------------------------------------------------------------
val BackgroundBrush = Brush.linearGradient(
    colors = listOf(BgStop1, BgStop2, BgStop3, BgStop1),
    start = Alignment.TopLeft,
    end = Alignment.BottomRight
)

val Orb1 = Color(0x4DCBD5E1) // translucent slate orb
val Orb2 = Color(0x4494A3B8)
val Orb3 = Color(0x33FFFFFF)
