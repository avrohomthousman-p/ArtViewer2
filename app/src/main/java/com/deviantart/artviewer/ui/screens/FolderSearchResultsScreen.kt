package com.deviantart.artviewer.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
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
import com.deviantart.artviewer.data.remote.DeviantArtFolder
import com.deviantart.artviewer.ui.activities.MainActivity
import com.deviantart.artviewer.ui.components.EditOrCreateFolderDialog
import com.deviantart.artviewer.ui.components.FolderInteraction
import com.deviantart.artviewer.ui.components.SingleFolderDisplay
import com.deviantart.artviewer.ui.components.TitleWithBullets
import com.deviantart.artviewer.ui.components.Toolbar
import com.deviantart.artviewer.ui.util.ToolbarButtonData
import com.deviantart.artviewer.ui.util.UiState



@Composable
fun FolderSearchResultsScreen(
    viewModel: FolderSearchResultsViewModel,
    ownerUsername: String,
    location: StorageLocation
) {
    val state = viewModel.uiState.collectAsState()
    var folderBeingSaved by remember { mutableStateOf<Int?>(null) }


    LaunchedEffect(Unit) {
        viewModel.loadDeviantArtFolders(ownerUsername, location)
    }


    FolderSearchResultsScreenContent(
        state = state.value,
        ownerUsername = ownerUsername,
        location = location,
        onSwipeFolder = { index -> viewModel.removeFolderFromDisplayList(index) },
        onClickSaveBtn = { index -> folderBeingSaved = index}
    )


    folderBeingSaved?.let { index ->
        val folders = (state.value as? UiState.Success)?.data ?: return@let

        EditOrCreateFolderDialog(
            folder = folders[index],
            onDismiss = { folderBeingSaved = null },
            onSave = { folder ->
                if (folder != null){
                    viewModel.saveFolderToDB(folder, index)
                }

                folderBeingSaved = null
            }
        )
    }
}



@Composable
private fun FolderSearchResultsScreenContent(
    state: UiState<List<Folder>>,
    ownerUsername: String,
    location: StorageLocation,
    onSwipeFolder: (Int) -> Unit,
    onClickSaveBtn: (Int) -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SearchResultsScreenToolbar(stringResource(R.string.search_results_toolbar_title))
        Spacer(modifier = Modifier.height(30.dp))


        when(state) {
            is UiState.Error -> DisplayError(ownerUsername, location)
            UiState.Loading -> DisplayLoadingMessage()
            is UiState.Success<List<Folder>> ->
                DisplayFoldersList(
                    folders = state.data,
                    onClickSaveBtn = onClickSaveBtn,
                    onSwipeFolder = onSwipeFolder
                )
        }
    }
}



/**
 * Toolbar just for the FolderSearchResults screen.
 */
@Composable
private fun SearchResultsScreenToolbar(toolbarTitle: String){
    val context = LocalContext.current

    Toolbar(
        includeBackButton = true,
        title = toolbarTitle,
        otherButtons = listOf(
            ToolbarButtonData(
                icon = R.drawable.ic_home,
                contentDescription = stringResource(R.string.home_icon_content_description),
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                }
            )
        )
    )
}



/**
 * Screen content for when the API fails
 */
@Composable
private fun DisplayError(ownerUsername: String, location: StorageLocation){
    val oppositeLocation = when(location){
        StorageLocation.COLLECTION -> StorageLocation.GALLERY
        StorageLocation.GALLERY -> StorageLocation.COLLECTION
    }


    Text(
        text = stringResource(R.string.no_deviantart_folders_found),
        fontSize = 30.sp,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(30.dp))


    TitleWithBullets(
        title = stringResource(R.string.suggested_solutions),
        bulletPoints = listOf(
            stringResource(R.string.check_internet_solution),
            stringResource(R.string.check_username_suggestion, ownerUsername),
            stringResource(R.string.check_location_suggestion, location, oppositeLocation)
        )
    )
}



@Composable
private fun DisplayLoadingMessage(){
    Text(
        text = stringResource(R.string.loading_folders_text),
        fontSize = 30.sp,
        textAlign = TextAlign.Center
    )
}



/**
 * Display primary content of the screen.
 */
@Composable
private fun DisplayFoldersList(
    folders: List<Folder>,
    onSwipeFolder: (Int) -> Unit,
    onClickSaveBtn: (Int) -> Unit
) {
    val textResource = if (folders.isEmpty()) R.string.no_folders_left_to_pick else R.string.pick_your_folders_prompt

    Text(
        text = stringResource(textResource),
        fontSize = 26.sp
    )
    Spacer(modifier = Modifier.height(30.dp))


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = folders.size,
            key = { index -> folders[index].remoteId ?: "ALL_$index" },
            contentType = { DeviantArtFolder }
        ){ index ->

            val folder = folders[index]


            Box(
                modifier = Modifier.animateItem(),
                contentAlignment = Alignment.Center
            ) {
                SingleFolderDisplay(
                    imageUrl = folder.thumbnailUrl,
                    folderName = folder.displayName,//TODO: tack on number of images
                    folderInteraction = FolderInteraction.Swipeable,
                    onFolderInteraction = { onSwipeFolder(index) },
                    onClickSaveBtn = { onClickSaveBtn(index) },
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}



@Preview
@Composable
private fun LoadingPreview(){
    FolderSearchResultsScreenContent(
        state = UiState.Loading,
        ownerUsername = "someone",
        location = StorageLocation.GALLERY,
        onSwipeFolder = { },
        onClickSaveBtn = { }
    )
}



@Preview
@Composable
private fun LoadFailedPreview(){
    FolderSearchResultsScreenContent(
        state = UiState.Error(),
        ownerUsername = "someone",
        location = StorageLocation.GALLERY,
        onSwipeFolder = { },
        onClickSaveBtn = { }
    )
}



@Preview
@Composable
private fun NoFoldersPreview(){
    FolderSearchResultsScreenContent(
        state = UiState.Success(emptyList()),
        ownerUsername = "someone",
        location = StorageLocation.GALLERY,
        onSwipeFolder = { },
        onClickSaveBtn = { }
    )
}



@Preview
@Composable
private fun TwoFoldersPreview(){
    FolderSearchResultsScreenContent(
        state = UiState.Success(
            listOf(
                Folder(
                    localId = 1,
                    remoteId = "1",
                    ownerUsername = "someone",
                    storedIn = StorageLocation.GALLERY,
                    displayName = "Sample Folder",
                    shouldRandomize = true,
                    thumbnailUrl = null,
                    totalImages = 200
                ),
                Folder(
                    localId = 2,
                    remoteId = "2",
                    ownerUsername = "someone else",
                    storedIn = StorageLocation.GALLERY,
                    displayName = "Sample Folder",
                    shouldRandomize = true,
                    thumbnailUrl = null,
                    totalImages = 200
                )
            )
        ),
        ownerUsername = "someone",
        location = StorageLocation.GALLERY,
        onSwipeFolder = { },
        onClickSaveBtn = { }
    )
}
