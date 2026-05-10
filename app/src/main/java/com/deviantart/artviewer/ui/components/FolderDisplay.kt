package com.deviantart.artviewer.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deviantart.artviewer.R
import com.deviantart.artviewer.ui.themes.AppColors


/**
 * Displays a single folder
 */
@Composable
fun FolderDisplay(
    imageUrl: String? = null,
    folderName: String = "",
    showEditBtn: Boolean = true,
    showDeleteBtn: Boolean = true,
    showSaveBtn: Boolean = false
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 20.dp)
            .background(
                color = AppColors.AltBackgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
        //TODO: make this clickable
    ){
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val imageSize = 64.dp
            if (!imageUrl.isNullOrEmpty()){
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(imageSize).padding(end = 4.dp)
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
                showEditBtn = showEditBtn,
                showDeleteBtn = showDeleteBtn,
                showSaveBtn = showSaveBtn
            )
        }
    }
}


/**
 * Buttons on the folder display that the user can tap to do something.
 */
@Composable
private fun ActionButtons(
    showEditBtn: Boolean,
    showDeleteBtn: Boolean,
    showSaveBtn: Boolean
){
    //TODO: add click animation
    val gapBetweenButtons = 18.dp

    if (showEditBtn) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
            contentDescription = null,
            modifier = Modifier.clickable { /* TODO */ },
            tint = AppColors.GreenSuccessColor
        )
        Spacer(modifier = Modifier.width(gapBetweenButtons))
    }

    if (showDeleteBtn) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
            contentDescription = null,
            modifier = Modifier.clickable { /* TODO */ },
            tint = AppColors.RedErrorColor
        )
        Spacer(modifier = Modifier.width(gapBetweenButtons))
    }

    if (showSaveBtn) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_save),
            contentDescription = null,
            modifier = Modifier.clickable { /* TODO */ },
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
        showEditBtn = true,
        showDeleteBtn = true,
        showSaveBtn = true
    )
}
