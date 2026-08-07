package com.housmantech.artviewer.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.housmantech.artviewer.ui.themes.AppColors


/**
 * Button that has standardized padding, colors, and rounded edges
 */
@Composable
fun StandardButton(modifier: Modifier, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.PrimarySurfaceColor,
            contentColor = AppColors.White
        ),
        contentPadding = PaddingValues(20.dp)
    ){
        Text(text = text, fontSize = 18.sp)
    }
}



@Preview
@Composable
private fun ButtonPreview(){
    StandardButton(
        modifier = Modifier,
        text = "Sample Button",
        onClick = { }
    )
}
