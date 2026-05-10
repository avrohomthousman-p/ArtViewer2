package com.deviantart.artviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deviantart.artviewer.R
import com.deviantart.artviewer.ui.themes.AppColors


/**
 * Displays a folder entry along with action buttons for Edit, Delete, and Save.
 * Each action button is shown only when the caller supplies a non-null handler
 * for that action.
 */
@Composable
fun FolderDisplay(
    imageUrl: String? = null,
    folderName: String = "",
    onClickEditBtn: (() -> Unit)? = null,
    onClickDeleteBtn: (() -> Unit)? = null,
    onClickSaveBtn: (() -> Unit)? = null
){
    FolderContainer(
        onClick = { /* TODO */ },
        content = {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val imageSize = 64.dp
                if (!imageUrl.isNullOrEmpty()){
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(imageSize)
                            .padding(end = 4.dp)
                    )
                }
                else {
                    Spacer(modifier = Modifier.width(imageSize))
                }


                if (folderName.isNotBlank()){
                    Text(
                        text = folderName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))


                ActionButtons(
                    onClickEditBtn = onClickEditBtn,
                    onClickDeleteBtn = onClickDeleteBtn,
                    onClickSaveBtn = onClickSaveBtn
                )
            }
        }
    )
}



/**
 * Displays a box that looks like a folder, but instead of showing folder information,
 * it can be clicked to add a new folder.
 */
@Composable
fun NewFolderPrompt(){
    FolderContainer(
        onClick = { /* TODO */ },
        content = {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                    contentDescription = null,
                    tint = AppColors.Black
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.add_new_folder_prompt),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}



/**
 * Clickable area that can be used to display a folder.
 */
@Composable
private fun FolderContainer(onClick: () -> Unit, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 20.dp)
            .background(
                color = AppColors.AltBackgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onClick() }
            ),
        contentAlignment = Alignment.Center
    ){
        content()
    }
}



/**
 * Buttons on the folder display that the user can tap to do something.
 */
@Composable
private fun ActionButtons(
    onClickEditBtn: (() -> Unit)?,
    onClickDeleteBtn: (() -> Unit)?,
    onClickSaveBtn: (() -> Unit)?
){
    val gapBetweenButtons = 18.dp

    if (onClickEditBtn != null) {
        val interactionSource = remember { MutableInteractionSource() }

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
            contentDescription = null,
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onClickEditBtn() }
            ),
            tint = AppColors.GreenSuccessColor
        )
        Spacer(modifier = Modifier.width(gapBetweenButtons))
    }

    if (onClickDeleteBtn != null) {
        val interactionSource = remember { MutableInteractionSource() }

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
            contentDescription = null,
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onClickDeleteBtn() }
            ),
            tint = AppColors.RedErrorColor
        )
        Spacer(modifier = Modifier.width(gapBetweenButtons))
    }

    if (onClickSaveBtn != null) {
        val interactionSource = remember { MutableInteractionSource() }

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_save),
            contentDescription = null,
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onClickSaveBtn() }
            ),
            tint = AppColors.GreenSuccessColor
        )
        Spacer(modifier = Modifier.width(gapBetweenButtons))
    }
}



@Composable
@Preview
fun FolderDisplayPreview(){
    FolderDisplay(
        folderName = "Sample Art",
        imageUrl = null,
        onClickEditBtn = { },
        onClickDeleteBtn = { },
        onClickSaveBtn = { }
    )
}
