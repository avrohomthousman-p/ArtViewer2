package com.housmantech.artviewer.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.housmantech.artviewer.ui.themes.AppColors


/**
 * Modifier that draws a border but only on the top and bottom of a composable
 */
fun Modifier.topAndBottomBorder(
    color: Color = AppColors.GreyBorderColor,
    strokeWidth: Dp = 1.dp
) = this.drawBehind {
    val strokePx = strokeWidth.toPx()

    // Top border
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = strokePx
    )

    // Bottom border
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = strokePx
    )
}
