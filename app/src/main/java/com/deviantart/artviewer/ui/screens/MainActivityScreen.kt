package com.deviantart.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deviantart.artviewer.R
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.ui.activities.DisplayArtActivity
import com.deviantart.artviewer.ui.activities.LoginActivity
import com.deviantart.artviewer.ui.activities.FolderSearchActivity
import com.deviantart.artviewer.ui.components.DeleteFolderDialog
import com.deviantart.artviewer.ui.components.EditOrCreateFolderDialog
import com.deviantart.artviewer.ui.components.FolderInteraction
import com.deviantart.artviewer.ui.components.NewFolderPrompt
import com.deviantart.artviewer.ui.components.SingleFolderDisplay
import com.deviantart.artviewer.ui.components.Toolbar
import com.deviantart.artviewer.ui.themes.AppColors
import com.deviantart.artviewer.ui.util.NavDestination
import com.deviantart.artviewer.ui.util.ToolbarButtonData
import com.deviantart.artviewer.ui.util.UiState


/**
 * Screen for the main activity that shows all the folders the user has saved.
 */
@Composable
fun MainActivityScreen(viewModel: MainActivityViewModel) {
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        viewModel.loadFolders()
    }


    // Navigation
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { destination ->
            when (destination) {
                is NavDestination.ToLoginActivity -> {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }

                else -> {}
            }
        }
    }


    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }



    val state = viewModel.uiState.collectAsState()
    var folderBeingEdited by remember { mutableStateOf<Int?>(null) }
    var folderBeingDeleted by remember { mutableStateOf<Int?>(null) }



    // Toolbar buttons
    val toolbarButtons = listOf(
        ToolbarButtonData(
            icon = R.drawable.ic_logout,
            contentDescription = stringResource(R.string.logout_btn_content_desc),
            onClick = { viewModel.logout() }
        )
    )



    MainActivityScreenContent(
        state = state.value,
        toolbarButtons = toolbarButtons,
        onClickFolder = { folder ->
            val intent = Intent(context, DisplayArtActivity::class.java)
            intent.putExtra(DisplayArtActivity.FOLDER_ID_KEY, folder.localId)
            intent.putExtra(DisplayArtActivity.FOLDER_NAME_KEY, folder.displayName)
            context.startActivity(intent)
        },
        onClickFolderEdit = { folderIndex -> folderBeingEdited = folderIndex },
        onClickFolderDelete = { folderIndex -> folderBeingDeleted = folderIndex }
    )


    folderBeingEdited?.let { index ->
        val folders = (state.value as? UiState.Success)?.data ?: return@let

        EditOrCreateFolderDialog(
            folder = folders[index],
            onDismiss = { folderBeingEdited = null },
            onSave = { folder ->
                if (folder != null) {
                    viewModel.updateFolder(folder, index)
                }

                folderBeingEdited = null
            }
        )
    }


    folderBeingDeleted?.let { index ->
        DeleteFolderDialog(
            onDismiss = { folderBeingDeleted = null },
            onSave = {
                viewModel.deleteFolder(index)
                folderBeingDeleted = null
            }
        )
    }
}


@Composable
private fun MainActivityScreenContent(
    state: UiState<List<Folder>>,
    toolbarButtons: List<ToolbarButtonData>,
    onClickFolder: (Folder) -> Unit,
    onClickFolderEdit: (Int) -> Unit,
    onClickFolderDelete: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Toolbar(
            includeBackButton = false,
            title = stringResource(R.string.main_activity_title),
            otherButtons = toolbarButtons
        )

        Spacer(modifier = Modifier.height(20.dp))


        when(state) {
            is UiState.Error -> ErrorDisplay()
            UiState.Loading -> LoadingDisplay()
            is UiState.Success<List<Folder>> ->
                FoldersListDisplay(
                    folders = state.data,
                    onClickFolder = onClickFolder,
                    onClickFolderEdit = onClickFolderEdit,
                    onClickFolderDelete = onClickFolderDelete
                )
        }
    }
}


/**
 * Display a loading message while the folders are being loaded.
 */
@Composable
private fun LoadingDisplay() {
    Text(
        text = stringResource(R.string.loading_folders_message),
        fontSize = 28.sp
    )
}



/**
 * Display for when the DB folders could not be loaded.
 */
@Composable
private fun ErrorDisplay(){
    Text(
        text = stringResource(R.string.load_failed_error),
        fontSize = 28.sp,
        textAlign = TextAlign.Center
    )
}



@Composable
private fun FoldersListDisplay(
    folders: List<Folder>,
    onClickFolder: (Folder) -> Unit,
    onClickFolderEdit: (Int) -> Unit,
    onClickFolderDelete: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.White)
            .verticalScroll(scrollState)
    ) {
        folders.forEachIndexed { index, folder ->

            SingleFolderDisplay(
                imageUrl = folder.thumbnailUrl,
                folderName = folder.displayName,
                imageCount = null,
                folderInteraction = FolderInteraction.Clickable,
                onFolderInteraction = { onClickFolder(folder) },
                onClickEditBtn = { onClickFolderEdit(index) },
                onClickDeleteBtn = { onClickFolderDelete(index) },
                onClickSaveBtn = null
            )
            Spacer(modifier = Modifier.height(30.dp))
        }

        if (folders.isEmpty()){
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.no_db_folders_found),
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))
        }


        val context = LocalContext.current
        NewFolderPrompt(
            onClick = {
                val intent = Intent(context, FolderSearchActivity::class.java)
                context.startActivity(intent)
            }
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}



val toolbarButtonsForPreviews = listOf(
    ToolbarButtonData(
        icon = R.drawable.ic_logout,
        contentDescription = "",
        onClick = { }
    )
)


@Preview(showBackground = true)
@Composable
private fun LoadingFoldersPreview() {
    MainActivityScreenContent(
        state = UiState.Loading,
        toolbarButtons = toolbarButtonsForPreviews,
        onClickFolder = { },
        onClickFolderEdit = { },
        onClickFolderDelete = { },
    )
}


@Preview(showBackground = true)
@Composable
private fun FailedToLoadFoldersPreview() {
    MainActivityScreenContent(
        state = UiState.Error(),
        toolbarButtons = toolbarButtonsForPreviews,
        onClickFolder = { },
        onClickFolderEdit = { },
        onClickFolderDelete = { }
    )
}


@Preview(showBackground = true)
@Composable
private fun NoFoldersPreview() {
    MainActivityScreenContent(
        state = UiState.Success(emptyList()),
        toolbarButtons = toolbarButtonsForPreviews,
        onClickFolder = { },
        onClickFolderEdit = { },
        onClickFolderDelete = { }
    )
}



@Preview
@Composable
private fun PreviewWithFolders() {
    MainActivityScreenContent(
        state = UiState.Success(listOf(
            Folder(
                remoteId = "1",
                ownerUsername = "someone",
                storedIn = StorageLocation.COLLECTION,
                displayName = "Test Folder A",
                shouldRandomize = true,
                thumbnailUrl = null,
                totalImages = 200
            ),
            Folder(
                remoteId = "2",
                ownerUsername = "someone else",
                storedIn = StorageLocation.COLLECTION,
                displayName = "Test Folder B",
                shouldRandomize = false,
                thumbnailUrl = null,
                totalImages = 200
            )
        )),
        toolbarButtons = toolbarButtonsForPreviews,
        onClickFolder = { },
        onClickFolderEdit = { },
        onClickFolderDelete = { }
    )
}
