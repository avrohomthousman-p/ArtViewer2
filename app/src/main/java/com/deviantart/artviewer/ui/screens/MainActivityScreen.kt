package com.deviantart.artviewer.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deviantart.artviewer.R
import com.deviantart.artviewer.ui.activities.LoginActivity
import com.deviantart.artviewer.ui.components.FolderDisplay
import com.deviantart.artviewer.ui.components.Toolbar
import com.deviantart.artviewer.util.NavDestination
import com.deviantart.artviewer.util.ToolbarButtonData
import com.deviantart.artviewer.util.UiState


/**
 * Screen for the main activity that shows all the folders the user has saved.
 */
@Composable
fun MainActivityScreen(viewModel: MainActivityViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadFolders()
    }


    // Navigation
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { destination ->
            when (destination) {
                is NavDestination.ToLoginActivity -> {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    //TODO: finish
                }

                else -> {}
            }
        }
    }


    val state = viewModel.uiState.collectAsState()
    val toolbarButtons = listOf(
        ToolbarButtonData(
            icon = R.drawable.ic_logout,
            contentDescription = stringResource(R.string.logout_btn_content_desc),
            onClick = { viewModel.logout() }
        )
        //TODO: add more icons here
    )
    MainActivityScreenContent(state.value, toolbarButtons)
}


@Composable
private fun MainActivityScreenContent(
    state: UiState<List<Folder>>,
    toolbarButtons: List<ToolbarButtonData>
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
            is UiState.Success<List<Folder>> -> FoldersDisplay(state.data)
        }
    }
}


/**
 * Display a loading message while the folders are being loaded.
 */
@Composable
private fun LoadingDisplay() {
    Text(
        text = stringResource(R.string.loading_message),
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
        fontSize = 28.sp
    )
}


@Composable
private fun FoldersDisplay(folders: List<Folder>) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        folders.forEach { folder ->
            //TODO: need real live data here
            FolderDisplay(
                imageUrl = null,
                folderName = "",
                showEditBtn = true,
                showDeleteBtn = true,
                showSaveBtn = false
            )
            Spacer(modifier = Modifier.height(30.dp))
        }


        //TODO: need a "add new folder" item
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
fun LoadingFoldersPreview() {
    MainActivityScreenContent(state = UiState.Loading, toolbarButtons = toolbarButtonsForPreviews)
}


@Preview(showBackground = true)
@Composable
fun FailedToLoadFoldersPreview() {
    MainActivityScreenContent(state = UiState.Error(), toolbarButtons = toolbarButtonsForPreviews)
}


@Preview(showBackground = true)
@Composable
fun NoFoldersPreview() {
    MainActivityScreenContent(state = UiState.Success(emptyList()), toolbarButtons = toolbarButtonsForPreviews)
}

//TODO: preview with a folder
