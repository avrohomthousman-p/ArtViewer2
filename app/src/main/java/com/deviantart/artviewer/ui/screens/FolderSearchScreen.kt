package com.deviantart.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.deviantart.artviewer.R
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.ui.activities.FolderSearchResultsActivity
import com.deviantart.artviewer.ui.activities.LoginActivity
import com.deviantart.artviewer.ui.activities.MainActivity
import com.deviantart.artviewer.ui.components.FolderTypePicker
import com.deviantart.artviewer.ui.components.LabeledCheckbox
import com.deviantart.artviewer.ui.components.StandardButton
import com.deviantart.artviewer.ui.components.Toolbar
import com.deviantart.artviewer.ui.util.NavDestination


/**
 * Screen for the SearchFoldersActivity
 */
@Composable
fun FolderSearchScreen(viewModel: FolderSearchViewModel) {
    val context = LocalContext.current
    val resources = LocalResources.current
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { messageId: Int ->
            Toast.makeText(context, resources.getString(messageId), Toast.LENGTH_LONG).show()
        }
    }


    LaunchedEffect(Unit){
        viewModel.navigation.collect { destination ->
            when(destination){
                NavDestination.ToMainActivity -> {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                }
                NavDestination.ToLoginActivity -> {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }

                else -> { }
            }
        }
    }


    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkIfAccessTokenExpired()
        }
    }


    FolderSearchScreenContent(
        saveFullCollection = { username, location, shouldRandomize ->
            viewModel.saveFullCollectionAsFolder(username, location, shouldRandomize)
        },
        checkIfLoginExpired = { viewModel.checkIfAccessTokenExpired() }
    )
}



/**
 * Content of the FOlderSearchScreen.
 *
 * @param saveFullCollection - A function that saves the full gallery or collection as a folder, to
 *          be called if the user makes that selection.
 * @param checkIfLoginExpired - A function that checks if the user needs to log in again, and
 *          triggers a navigation to the login screen in that case.
 */
@Composable
fun FolderSearchScreenContent(
    saveFullCollection: (String, StorageLocation, Boolean) -> Unit,
    checkIfLoginExpired: () -> Boolean
) {
    val context = LocalContext.current
    var usernameInput by remember { mutableStateOf("") }
    var isValidUsername by remember { mutableStateOf(true) }
    var saveFullGallery by remember { mutableStateOf(false) }
    var radioSelection by remember { mutableStateOf(StorageLocation.GALLERY) }
    var shouldRandomize by remember { mutableStateOf(true) }
    


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Toolbar(
            includeBackButton = true,
            title = stringResource(R.string.folder_search_toolbar_title),
            otherButtons = listOf()
        )
        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = stringResource(R.string.folder_search_prompt),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))


        OutlinedTextField(
            value = usernameInput,
            onValueChange = { newValue ->
                usernameInput = newValue
                isValidUsername = newValue.isNotBlank()
            },
            isError = !isValidUsername,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            label = { Text(stringResource(R.string.folder_search_username_prompt)) },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(30.dp))



        LabeledCheckbox(
            label = stringResource(R.string.folder_search_checkbox_label),
            checked = saveFullGallery,
            onCheckedChange = { saveFullGallery = it }
        )
        Spacer(modifier = Modifier.height(30.dp))



        FolderTypePicker(
            currentlySelected = radioSelection,
            onSelected = { radioSelection = it }
        )
        Spacer(modifier = Modifier.height(30.dp))



        if (saveFullGallery){
            val labelId = if (shouldRandomize) R.string.should_randomize_text else R.string.should_not_randomize_text

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(labelId)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = shouldRandomize,
                    onCheckedChange = { shouldRandomize = !shouldRandomize }
                )
            }
        }



        Spacer(modifier = Modifier.weight(1f))



        val btnText =
            if (saveFullGallery) R.string.folder_search_save_full_gallery_button
            else R.string.folder_search_run_search_button

        StandardButton(
            modifier = Modifier,
            text = stringResource(btnText),
            onClick = {
                if(checkIfLoginExpired()){
                    return@StandardButton
                }
                if (usernameInput.isBlank()){
                    isValidUsername = false
                    return@StandardButton
                }


                if (saveFullGallery){
                    saveFullCollection(usernameInput, radioSelection, shouldRandomize)
                }
                else {
                    val intent = Intent(context, FolderSearchResultsActivity::class.java)
                    intent.putExtra(FolderSearchResultsActivity.USERNAME_KEY, usernameInput.trim())
                    intent.putExtra(FolderSearchResultsActivity.LOCATION_KEY, radioSelection.toString())
                    context.startActivity(intent)
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}



@Preview
@Composable
private fun SearchFoldersActivityPreview(){
    FolderSearchScreenContent(
        saveFullCollection = { _, _, _ -> },
        checkIfLoginExpired = { true },
    )
}
