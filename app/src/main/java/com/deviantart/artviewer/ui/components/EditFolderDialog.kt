package com.deviantart.artviewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deviantart.artviewer.R
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.ui.themes.AppColors


/**
 * Dialog popup for when the user clicks the edit button on a folder.
 *
 * @param folder - The folder the user wants to edit.
 * @param onDismiss - A function to be called when the close button is clicked. It should
 *          hide this composable (along with anything else you need done).
 * @param onSave - A function called when the save button is clicked. If changes were made
 *          to the folder, an updated folder will be passed in. Otherwise, a null will be
 *          passed in.
 */
@Composable
fun EditFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onSave: (Folder?) -> Unit
) {
    var folderNameDraft by remember { mutableStateOf(folder.displayName) }
    var shouldRandomize by remember { mutableStateOf(folder.shouldRandomize) }
    var showErrorOnFolderName by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.edit_dialog_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Editable Folder Name
                RenameFolderTextField(
                    textFieldValue = folderNameDraft,
                    onValueChanged = {
                        if (it.length >= 4) {
                            showErrorOnFolderName = false
                        }
                        folderNameDraft = it
                    },
                    isError = showErrorOnFolderName
                )


                // Switch For Folder Randomization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val textId = if (shouldRandomize) R.string.should_randomize_text else R.string.should_not_randomize_text
                    Text(text = stringResource(textId))
                    Switch(
                        checked = shouldRandomize,
                        onCheckedChange = { shouldRandomize = it }
                    )
                }


                // Uneditable Folder Info (Secondary Info)
                Column {
                    Text(
                        text = stringResource(
                            R.string.folder_info_text,
                            folder.ownerUsername,
                            folder.storedIn.toString().lowercase()
                        ),
                        fontSize = 10.sp,
                        color = AppColors.MutedTextColor
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (folderNameDraft.trim().length < 4){
                        showErrorOnFolderName = true
                        return@TextButton
                    }

                    val isDirty = (folder.displayName != folderNameDraft)
                            || (folder.shouldRandomize != shouldRandomize)


                    var updatedFolder: Folder? = null
                    if (isDirty){
                        updatedFolder = folder.copy(
                            localId = folder.localId,
                            remoteId = folder.remoteId,
                            ownerUsername = folder.ownerUsername,
                            storedIn = folder.storedIn,
                            displayName = folderNameDraft.trim(),
                            shouldRandomize = shouldRandomize,
                            thumbnailUrl = folder.thumbnailUrl,
                            totalImages = folder.totalImages
                        )
                    }

                    onSave(updatedFolder)
                },
                content = {
                    Text(stringResource(R.string.save))
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                content = {
                    Text(stringResource(R.string.cancel))
                }
            )
        }
    )
}



@Composable
private fun RenameFolderTextField(
    textFieldValue: String,
    onValueChanged: (String) -> Unit,
    isError: Boolean
){
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = onValueChanged,
        label = { Text(stringResource(R.string.folder_name)) },
        singleLine = true,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = stringResource(R.string.folder_name_error),
                    fontSize = 12.sp,
                    color = AppColors.RedErrorColor
                )
            }
        }
    )
}
