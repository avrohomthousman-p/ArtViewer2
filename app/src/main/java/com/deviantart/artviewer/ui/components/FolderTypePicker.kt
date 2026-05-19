package com.deviantart.artviewer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deviantart.artviewer.common.StorageLocation



/**
 * Composable for showing radio buttons to let the user choose the kind of folder they are
 * looking for: collection or gallery.
 *
 * @param currentlySelected - The label of the radio button currently selected.
 * @param onSelected - A function to call when an item is selected.
 */
@Composable
fun FolderTypePicker(
    currentlySelected: StorageLocation,
    onSelected: (StorageLocation) -> Unit
) {
    Column(
        modifier = Modifier.wrapContentSize(Alignment.CenterStart)
    ) {

        SingleRadioButton(
            option = StorageLocation.GALLERY,
            isSelected = currentlySelected == StorageLocation.GALLERY,
            onSelected = onSelected
        )


        SingleRadioButton(
            option = StorageLocation.COLLECTION,
            isSelected = currentlySelected == StorageLocation.COLLECTION,
            onSelected = onSelected
        )
    }
}



@Composable
private fun SingleRadioButton(
    option: StorageLocation,
    isSelected: Boolean,
    onSelected: (StorageLocation) -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onSelected(option) }
            .padding(top = 6.dp, bottom = 6.dp, end = 16.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelected(option) }
        )
        Text(
            text = option.asUiFriendlyLabel(),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
