package com.deviantart.artviewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deviantart.artviewer.R
import com.deviantart.artviewer.ui.themes.AppColors


@Composable
fun DeleteFolderDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.delete_folder_conformation),
                    fontSize = 18.sp,
                    color = AppColors.RedErrorColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.secondary_delete_warning),
                    fontSize = 14.sp,
                    color = AppColors.MutedTextColor
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave() },
                content = {
                    Text(stringResource(R.string.confirm))
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