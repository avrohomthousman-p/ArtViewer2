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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deviantart.artviewer.R
import com.deviantart.artviewer.ui.themes.AppColors



/**
 * Decides the behavior of the folder - clickable or swipeable
 */
sealed class FolderInteraction {
    data object Clickable : FolderInteraction()
    data object Swipeable : FolderInteraction()
}




/**
 * Displays a folder entry along with action buttons for Edit, Delete, and Save.
 * Each action button is shown only when the caller supplies a non-null handler
 * for that action.
 *
 * @param imageUrl - The url for the folder's thumbnail image, or null if it has none.
 * @param folderName - The name of the folder to be displayed as a title.
 * @param imageCount - The number of images in the folder, or null if you don't want that displayed.
 * @param folderInteraction - The kind of special action that can be taken with this folder:
 *              clickable or swipeable.
 * @param onFolderInteraction - A function you want executed when the special folder interaction happens.
 * @param onClickEditBtn - A function you want executed when the edit button is clicked, or null if
 *              there should be no edit button.
 * @param onClickDeleteBtn - A function you want executed when the delete button is clicked, or null
 *              if there should be no edit button.
 * @param onClickSaveBtn - A function you want executed when the save button is clicked, or null if
 *              there should be no edit button.
 *
 */
@Composable
fun SingleFolderDisplay(
    imageUrl: String?,
    folderName: String,
    imageCount: Int?,
    folderInteraction: FolderInteraction,
    onFolderInteraction: (() -> Unit),
    onClickEditBtn: (() -> Unit)? = null,
    onClickDeleteBtn: (() -> Unit)? = null,
    onClickSaveBtn: (() -> Unit)? = null
){
    FolderContainer(
        folderInteraction = folderInteraction,
        onFolderInteraction = onFolderInteraction,
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



                Text(
                    text = buildFolderDisplayName(folderName, imageCount),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )


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
 * Builds the text to display on the folder, which follows one of these formats:
 *
 * if a non-null image count is provided:
 *      folderName   ([imageCount] images)
 *
 * if imageCount is null:
 *      folderName
 */
@Composable
private fun buildFolderDisplayName(
    folderName: String,
    imageCount: Int?
): AnnotatedString {

    return buildAnnotatedString {

        // Folder name
        withStyle(
            style = SpanStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(folderName)
        }



        // If no image count, stop here
        if (imageCount == null) return@buildAnnotatedString



        append("   ")



        // Image count
        withStyle(
            style = SpanStyle(
                fontSize = 12.sp,
                color = AppColors.MutedTextColor,
                baselineShift = BaselineShift(0.2f)
            )
        ) {
            append("($imageCount images)")
        }
    }
}



/**
 * Displays a box that looks like a folder, but instead of showing folder information,
 * it can be clicked to add a new folder.
 */
@Composable
fun NewFolderPrompt(onClick: () -> Unit){
    FolderContainer(
        folderInteraction = FolderInteraction.Clickable,
        onFolderInteraction = { onClick() },
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

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = stringResource(R.string.add_new_folder_prompt),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}



private val baseFolderModifier =
    Modifier
        .fillMaxWidth()
        .height(90.dp)
        .padding(horizontal = 20.dp)
        .background(
            color = AppColors.AltBackgroundColor,
            shape = RoundedCornerShape(12.dp)
        )



/**
 * Clickable area that can be used to display a folder.
 */
@Composable
private fun FolderContainer(
    folderInteraction: FolderInteraction,
    onFolderInteraction: (() -> Unit),
    content: @Composable () -> Unit
) {

    when(folderInteraction){
        FolderInteraction.Clickable -> TappableFolderContainer(onFolderInteraction, content)
        FolderInteraction.Swipeable -> SwipeableFolderContainer(onFolderInteraction, content)
    }
}



@Composable
private fun TappableFolderContainer(
    onTapFolder: (() -> Unit),
    content: @Composable () -> Unit
){
    val interactionSource = remember { MutableInteractionSource() }


    Box(
        modifier =
            baseFolderModifier
                .then(
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = { onTapFolder() }
                    )
                ),
        contentAlignment = Alignment.Center
    ){
        content()
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableFolderContainer(
    onSwipe: (() -> Unit),
    content: @Composable () -> Unit
){
    val state = rememberSwipeToDismissBoxState(
        SwipeToDismissBoxValue.Settled,
        SwipeToDismissBoxDefaults.positionalThreshold
    )


    SwipeToDismissBox(
        state = state,
        backgroundContent = { },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        content = {
            Box(
                modifier = baseFolderModifier,
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        },
        onDismiss = { onSwipe() },
    )
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
private fun FolderNoImageCountPreview(){
    SingleFolderDisplay(
        imageUrl = null,
        folderName = "Sample Art",
        imageCount = null,
        folderInteraction = FolderInteraction.Clickable,
        onFolderInteraction = { },
        onClickEditBtn = { },
        onClickDeleteBtn = { },
        onClickSaveBtn = { },
    )
}



@Composable
@Preview
private fun FolderWithImageCountPreview(){
    SingleFolderDisplay(
        imageUrl = null,
        folderName = "Sample Art",
        imageCount = 239,
        folderInteraction = FolderInteraction.Clickable,
        onFolderInteraction = { },
        onClickSaveBtn = { }
    )
}
