package com.duasmartapp.ui.theme

import androidx.compose.foundation.text.FontFamily
import androidx.compose.ui.text.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DuaTypography = TextStyle(
    fontFamily = FontFamily.Default,
    fontStyle = FontStyle.Normal,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    color = Ink
)

val Heading1 = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Ink)
val Heading2 = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
val Body = TextStyle(fontSize = 14.sp, color = Ink)
val BodyMuted = TextStyle(fontSize = 13.sp, color = Slate700)
val Caption = TextStyle(fontSize = 12.sp, color = Slate500)
val ButtonText = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink)
val PillText = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp)
