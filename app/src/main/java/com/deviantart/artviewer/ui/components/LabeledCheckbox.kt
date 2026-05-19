package com.deviantart.artviewer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp



/**
 * Checkbox that also has a label to the right describing what it is for.
 */
@Composable
fun LabeledCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(1.3f)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}



@Preview
@Composable
private fun CheckedCheckboxPreview(){
    LabeledCheckbox(
        label = "Check if you want to",
        checked = true,
        onCheckedChange = { }
    )
}



@Preview
@Composable
private fun UncheckedCheckboxPreview(){
    LabeledCheckbox(
        label = "Check if you want to",
        checked = false,
        onCheckedChange = { }
    )
}
