package com.deviantart.artviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



/**
 * Composable For showing text and some bullet points below it.
 */
@Composable
fun TitleWithBullets(
    title: String,
    bulletPoints: List<String>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp)
        ){
            bulletPoints.forEach { point ->
                Row(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text("• ")
                    Text(
                        text = point
                    )
                }
            }
        }
    }
}



@Preview
@Composable
private fun BulletPointsPreview(){
    TitleWithBullets(
        title = "Sample title",
        bulletPoints = listOf(
            "This is some information which needs to be displayed",
            "And this is another thing that should shown as its own bullet point",
            "I need at least 3 items for this to be a good representative of this component"
        )
    )
}
