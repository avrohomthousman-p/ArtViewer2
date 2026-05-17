package com.deviantart.artviewer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp



//TODO: use an enum instead of this
/**
 * Composable for letting the user choose the kind of folder they are looking for:
 * collection or gallery.
 *
 * @param currentlySelected - The label of the radio button currently selected.
 * @param onSelected - A function to call when an item is selected.
 */
@Composable
fun FolderTypePicker(
    currentlySelected: String,
    onSelected: (String) -> Unit
) {
    Column {
        SingleRadioButton(
            optionName = TODO(),
            isSelected = TODO(),
            onSelected = TODO()
        )
    }
}



@Composable
private fun SingleRadioButton(
    optionName: String,
    isSelected: Boolean,
    onSelected: (String) -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(optionName) }
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelected(optionName) }
        )
        Text(
            text = optionName,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
