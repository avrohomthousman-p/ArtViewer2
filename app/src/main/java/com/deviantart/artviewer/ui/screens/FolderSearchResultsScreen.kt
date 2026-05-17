package com.deviantart.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deviantart.artviewer.R
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.remote.DeviantArtFolder
import com.deviantart.artviewer.ui.activities.MainActivity
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


    LaunchedEffect(Unit) {
        viewModel.loadFolders(ownerUsername, location)
    }


    FolderSearchResultsScreenContent(state.value, ownerUsername, location)
}



@Composable
fun FolderSearchResultsScreenContent(
    state: UiState<List<DeviantArtFolder>>,
    ownerUsername: String,
    location: StorageLocation
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
            is UiState.Success<List<DeviantArtFolder>> -> DisplayFoldersList(state.data)
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
                    (context as? Activity)?.finish()
                }
            )
        )
    )
}



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



@Composable
private fun DisplayFoldersList(folders: List<DeviantArtFolder>){
    Text(
        text = stringResource(R.string.pick_your_folders_prompt),
        fontSize = 26.sp
    )


    folders.forEach { it ->
        Text(it.folderName) //TODO: need to show actual folder
    }
}
