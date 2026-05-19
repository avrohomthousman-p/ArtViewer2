package com.deviantart.artviewer.ui.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deviantart.artviewer.R
import com.deviantart.artviewer.ui.themes.AppColors
import com.deviantart.artviewer.ui.util.ToolbarButtonData


const val TOOLBAR_HEIGHT = 70


/**
 * Toolbar with customizable buttons
 */
@Composable
fun Toolbar(
    includeBackButton: Boolean,
    title: String,
    otherButtons: List<ToolbarButtonData> = emptyList()
) {
    Surface(
        tonalElevation = 4.dp,   // Standard Material 3 elevation
        shadowElevation = 4.dp   // fallback for older devices
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TOOLBAR_HEIGHT.dp)
                .background(AppColors.PrimarySurfaceColor),
            verticalAlignment = Alignment.CenterVertically
        ){

            if(includeBackButton){
                val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

                Spacer(modifier = Modifier.width(10.dp))
                ToolbarButton(
                    data = ToolbarButtonData(
                        icon = R.drawable.ic_arrow_back,
                        contentDescription = "",
                        onClick = { dispatcher?.onBackPressed() }
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
            }


            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = AppColors.White,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )


            otherButtons.forEach {
                ToolbarButton(it)
                Spacer(modifier = Modifier.width(14.dp))
            }
        }
    }
}



/**
 * Generic clickable icon that can be embedded in the toolbar
 */
@Composable
private fun ToolbarButton(
    data: ToolbarButtonData
){
    Icon(
        imageVector = ImageVector.vectorResource(data.icon),
        contentDescription = data.contentDescription,
        modifier = Modifier.clickable { data.onClick() },
        tint = AppColors.White
    )
}