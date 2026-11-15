package com.duasmartapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.duasmartapp.R
import com.duasmartapp.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassFillLight)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .shadow(shadowElevation, shape = RoundedCornerShape(20.dp), clip = true),
        contentAlignment = Alignment.TopStart
    ) {
        content()
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 2.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassFillSoft)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .shadow(shadowElevation, shape = RoundedCornerShape(16.dp), clip = true),
        contentAlignment = Alignment.TopStart
    ) {
        content()
    }
}

enum class StatusType { Present, Absent, Leave }

@Composable
fun StatusPill(
    type: StatusType,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = PillText
) {
    val (fill, border, textColor) = when (type) {
        StatusType.Present -> (EmeraldFill, EmeraldBorder, Emerald)
        StatusType.Absent -> (CrimsonFill, CrimsonBorder, Crimson)
        StatusType.Leave -> (AmberFill, AmberBorder, Amber)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(fill)
            .border(1.dp, border, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = textStyle, color = textColor)
    }
}

@Composable
fun DuaIcon(
    painter: Painter,
    tint: Color = Ink,
    size: Dp = 24.dp,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun DuaIconButton(
    painter: Painter,
    onClick: () -> Unit,
    tint: Color = Ink,
    size: Dp = 26.dp,
    contentDescription: String? = null
) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        DuaIcon(
            painter = painter,
            tint = tint,
            size = size,
            contentDescription = contentDescription,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun AppHeader(
    title: String = "DUA SMART APP",
    subtitle: String? = null,
    onLogoClick: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, top = 8.dp),
        shadowElevation = 0.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF64748B), RoundedCornerShape(12.dp))
                    .clickable(onClick = onLogoClick)
            ) {
                DuaIcon(
                    painter = painterResource(R.drawable.ic_launcher),
                    tint = White,
                    size = 28.dp,
                    contentDescription = "DUA SMART APP logo"
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = Heading2, color = Ink)
                subtitle?.let {
                    Text(text = it, style = Caption, color = Slate500)
                }
            }
        }
    }
}

data class NavItem(val route: String, val label: String, val iconId: Int)

@Composable
fun BottomNav(
    items: List<NavItem>,
    selected: String,
    onItemClick: (String) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, bottom = 24.dp, top = 6.dp),
        shadowElevation = 0.dp
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            items.forEach { item ->
                val isSelected = item.route == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = { onItemClick(item.route) }),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DuaIcon(
                        painter = painterResource(item.iconId),
                        tint = if (isSelected) Ink else Slate500,
                        size = 24.dp,
                        contentDescription = item.label
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = Caption.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Ink else Slate500
                    )
                }
            }
        }
    }
}
