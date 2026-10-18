package com.housmantech.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import com.housmantech.artviewer.R
import com.housmantech.artviewer.common.StorageLocation
import com.housmantech.artviewer.ui.activities.FolderSearchResultsActivity
import com.housmantech.artviewer.ui.activities.LoginActivity
import com.housmantech.artviewer.ui.activities.MainActivity
import com.housmantech.artviewer.ui.components.FolderSearchInfoDialog
import com.housmantech.artviewer.ui.components.FolderTypePicker
import com.housmantech.artviewer.ui.components.LabeledCheckbox
import com.housmantech.artviewer.ui.components.StandardButton
import com.housmantech.artviewer.ui.components.Toolbar
import com.housmantech.artviewer.ui.components.TwoPaneLayout
import com.housmantech.artviewer.ui.themes.AppColors
import com.housmantech.artviewer.ui.util.NavDestination



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
 * Content of the FolderSearchScreen.
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
    val usernameInput = rememberSaveable { mutableStateOf("") }
    val isValidUsername = rememberSaveable { mutableStateOf(true) }
    val saveFullGallery = rememberSaveable { mutableStateOf(false) }
    val storageLocation = rememberSaveable { mutableStateOf(StorageLocation.GALLERY) }
    val shouldRandomize = rememberSaveable { mutableStateOf(true) }
    var showInfoPopup by remember { mutableStateOf(false) }



    if (showInfoPopup){
        FolderSearchInfoDialog(
            dismissDialog = { showInfoPopup = false }
        )
    }


    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    if (isPortrait) {
        PortraitDisplay(
            usernameInput = usernameInput,
            isValidUsername = isValidUsername,
            saveFullGallery = saveFullGallery,
            storageLocation = storageLocation,
            shouldRandomize = shouldRandomize,
            showInfoPopup = { showInfoPopup = true },
            checkIfLoginExpired = checkIfLoginExpired,
            saveFullCollection = saveFullCollection
        )
    }
    else {
        LandscapeDisplay(
            usernameInput = usernameInput,
            isValidUsername = isValidUsername,
            saveFullGallery = saveFullGallery,
            storageLocation = storageLocation,
            shouldRandomize = shouldRandomize,
            showInfoPopup = { showInfoPopup = true },
            checkIfLoginExpired = checkIfLoginExpired,
            saveFullCollection = saveFullCollection
        )
    }
}



@Composable
private fun PortraitDisplay(
    //User input
    usernameInput: MutableState<String>,
    isValidUsername: MutableState<Boolean>,
    saveFullGallery: MutableState<Boolean>,
    storageLocation: MutableState<StorageLocation>,
    shouldRandomize: MutableState<Boolean>,

    //Functions
    showInfoPopup: () -> Unit,
    checkIfLoginExpired: () -> Boolean,
    saveFullCollection: (String, StorageLocation, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Toolbar(
            includeBackButton = true,
            title = stringResource(R.string.folder_search_toolbar_title),
            otherButtons = listOf()
        )
        Spacer(modifier = Modifier.height(16.dp))


        ScreenTitle()
        Spacer(modifier = Modifier.height(40.dp))


        UsernameInputFeild(
            usernameInput = usernameInput.value,
            setUsernameInput = { newValue -> usernameInput.value = newValue},
            isValidUsername = isValidUsername.value,
            setIsValidUsername = { newValue -> isValidUsername.value = newValue }
        )
        Spacer(modifier = Modifier.height(30.dp))



        SaveFullGalleryCheckbox(
            saveFullGallery = saveFullGallery.value,
            setSaveFullGallery = { newValue -> saveFullGallery.value = newValue}
        )
        Spacer(modifier = Modifier.height(30.dp))



        FolderTypePicker(
            currentlySelected = storageLocation.value,
            onSelected = { storageLocation.value = it }
        )
        Spacer(modifier = Modifier.height(30.dp))



        if (saveFullGallery.value){
            FolderRandomizationSwitch(
                shouldRandomize = shouldRandomize.value,
                setShouldRandomize = { newValue -> shouldRandomize.value = newValue }
            )
        }



        Spacer(modifier = Modifier.weight(1f))



        SearchButton(
            usernameInput = usernameInput.value,
            saveFullGallery = saveFullGallery.value,
            storageLocation = storageLocation.value,
            shouldRandomize = shouldRandomize.value,
            checkIfLoginExpired = checkIfLoginExpired,
            setIsValidUsername = { newValue -> isValidUsername.value = newValue },
            saveFullCollection = saveFullCollection
        )
        Spacer(modifier = Modifier.height(20.dp))


        HelpText(
            showInfoPopup = showInfoPopup
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}



@Composable
private fun LandscapeDisplay(
    //User input
    usernameInput: MutableState<String>,
    isValidUsername: MutableState<Boolean>,
    saveFullGallery: MutableState<Boolean>,
    storageLocation: MutableState<StorageLocation>,
    shouldRandomize: MutableState<Boolean>,

    //Functions
    showInfoPopup: () -> Unit,
    checkIfLoginExpired: () -> Boolean,
    saveFullCollection: (String, StorageLocation, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.White)
    ) {
        Toolbar(
            includeBackButton = true,
            title = stringResource(R.string.folder_search_toolbar_title),
            otherButtons = listOf()
        )


        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            dividerWidth = 10.dp,
            dividerColor = AppColors.AltBackgroundColor,
            leftContent = {
                Spacer(modifier = Modifier.height(20.dp))


                ScreenTitle()
                Spacer(modifier = Modifier.height(30.dp))


                UsernameInputFeild(
                    usernameInput = usernameInput.value,
                    setUsernameInput = { newValue -> usernameInput.value = newValue },
                    isValidUsername = isValidUsername.value,
                    setIsValidUsername = { newValue -> isValidUsername.value = newValue }
                )
                Spacer(modifier = Modifier.height(20.dp))


                FolderTypePicker(
                    currentlySelected = storageLocation.value,
                    onSelected = { storageLocation.value = it }
                )
                Spacer(modifier = Modifier.height(20.dp))
            },
            rightContent = {
                Spacer(modifier = Modifier.height(16.dp))


                SaveFullGalleryCheckbox(
                    saveFullGallery = saveFullGallery.value,
                    setSaveFullGallery = { newValue -> saveFullGallery.value = newValue}
                )
                Spacer(modifier = Modifier.height(20.dp))


                if (saveFullGallery.value){
                    FolderRandomizationSwitch(
                        shouldRandomize = shouldRandomize.value,
                        setShouldRandomize = { newValue -> shouldRandomize.value = newValue }
                    )
                }


                Spacer(modifier = Modifier.weight(1f))


                SearchButton(
                    usernameInput = usernameInput.value,
                    saveFullGallery = saveFullGallery.value,
                    storageLocation = storageLocation.value,
                    shouldRandomize = shouldRandomize.value,
                    checkIfLoginExpired = checkIfLoginExpired,
                    setIsValidUsername = { newValue -> isValidUsername.value = newValue },
                    saveFullCollection = saveFullCollection
                )
                Spacer(modifier = Modifier.height(20.dp))


                HelpText(
                    showInfoPopup = showInfoPopup
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        )
    }
}



@Composable
private fun ScreenTitle(){
    Text(
        text = stringResource(R.string.folder_search_prompt),
        modifier = Modifier.fillMaxWidth(),
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}



/**
 * Input field for entering the username of the artist whose folders you want to search for.
 */
@Composable
private fun UsernameInputFeild(
    usernameInput: String,
    setUsernameInput: (String) -> Unit,
    isValidUsername: Boolean,
    setIsValidUsername: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = usernameInput,
        onValueChange = { newValue ->
            setUsernameInput(newValue)
            setIsValidUsername(newValue.isNotBlank())
        },
        isError = !isValidUsername,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        label = { Text(stringResource(R.string.folder_search_username_prompt)) },
        singleLine = true
    )
}



/**
 * Displays a checkbox that lets the user select if they want to save the full gallery/collection,
 * or want to see individual folders they can save separately.
 */
@Composable
private fun SaveFullGalleryCheckbox(
    saveFullGallery: Boolean,
    setSaveFullGallery: (Boolean) -> Unit
) {
    LabeledCheckbox(
        label = stringResource(R.string.folder_search_checkbox_label),
        checked = saveFullGallery,
        onCheckedChange = setSaveFullGallery
    )
}



/**
 * Displays a switch that can be used to toggle the folder randomization setting
 */
@Composable
private fun FolderRandomizationSwitch(
    shouldRandomize: Boolean,
    setShouldRandomize: (Boolean) -> Unit
) {
    val labelId = if (shouldRandomize) R.string.should_randomize_text else R.string.should_not_randomize_text

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(labelId)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = shouldRandomize,
            onCheckedChange = setShouldRandomize
        )
    }
}



/**
 * Shows the button that submits the form and does the folder search (or gallery save if that is
 * what the user selected).
 */
@Composable
private fun SearchButton(
    //User input
    usernameInput: String,
    saveFullGallery: Boolean,
    storageLocation: StorageLocation,
    shouldRandomize: Boolean,

    //Functions
    checkIfLoginExpired: () -> Boolean,
    setIsValidUsername: (Boolean) -> Unit,
    saveFullCollection: (String, StorageLocation, Boolean) -> Unit
) {
    val context = LocalContext.current

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
                setIsValidUsername(false)
                return@StandardButton
            }


            if (saveFullGallery){
                saveFullCollection(usernameInput, storageLocation, shouldRandomize)
            }
            else {
                val intent = Intent(context, FolderSearchResultsActivity::class.java)
                intent.putExtra(FolderSearchResultsActivity.USERNAME_KEY, usernameInput.trim())
                intent.putExtra(FolderSearchResultsActivity.LOCATION_KEY, storageLocation.toString())
                context.startActivity(intent)
            }
        }
    )
}



/**
 * Displays help text the user can click on to get information about the folder search.
 */
@Composable
private fun HelpText(
    showInfoPopup: () -> Unit
) {
    Text(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = null,
            onClick = showInfoPopup
        ),
        text = stringResource(R.string.folder_search_help_text),
        color = AppColors.LinkColor,
        fontSize = 16.sp
    )
}



@Preview
@Composable
private fun SearchFoldersActivityPreview(){
    FolderSearchScreenContent(
        saveFullCollection = { _, _, _ -> },
        checkIfLoginExpired = { true },
    )
}
