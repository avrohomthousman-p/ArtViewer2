package com.housmantech.artviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.housmantech.artviewer.ui.themes.AppColors



/**
 * Splits the right and left side of the screen into two seperate panes, each with its own
 * content. This is ideal for landscape mode.
 */
@Composable
fun TwoPaneLayout(
    modifier: Modifier = Modifier,
    contentHorizontalPadding: Dp = 20.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    dividerWidth: Dp = 1.dp,
    dividerColor: Color = AppColors.Black,
    leftContent: @Composable ColumnScope.() -> Unit,
    rightContent: @Composable ColumnScope.() -> Unit
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = contentHorizontalPadding),
            horizontalAlignment = horizontalAlignment
        ) {
            leftContent()
        }



        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(dividerWidth)
                .background(dividerColor)
        )



        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = contentHorizontalPadding),
            horizontalAlignment = horizontalAlignment
        ) {
            rightContent()
        }
    }
}
