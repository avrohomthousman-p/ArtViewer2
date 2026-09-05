package com.housmantech.artviewer.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.housmantech.artviewer.R
import com.housmantech.artviewer.ui.components.Toolbar
import com.housmantech.artviewer.ui.util.topAndBottomBorder


/**
 * Screen for thr settings page
 */
@Composable
fun SettingsScreen() {
    SettingsScreenContent()
}



@Composable
private fun SettingsScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Toolbar(
            includeBackButton = true,
            title = stringResource(R.string.settings_title),
            otherButtons = listOf()
        )
        Spacer(modifier = Modifier.height(20.dp))


        MatureContentSetting()
        PrivacyPolicySetting()
    }
}



/**
 * Shows a settings item for toggling mature content
 */
@Composable
private fun MatureContentSetting() {
    var matureContentAllowed by remember { mutableStateOf(true) } //TODO: fetch this from shared prefs


    val (iconId, contentDescriptionId, textId) =
        if (matureContentAllowed) {
            Triple(
                R.drawable.ic_visibility_on,
                R.string.visible_icon_content_desc,
                R.string.settings_mature_content_allowed
            )
        } else {
            Triple(
                R.drawable.ic_visibility_off,
                R.string.hidden_icon_content_desc,
                R.string.settings_mature_content_blocked
            )
        }


    SettingsItem(
        iconId = iconId,
        contentDesc = stringResource(contentDescriptionId),
        text = stringResource(textId),
        extraContent = {
            Switch(
                checked = matureContentAllowed,
                onCheckedChange = {
                    //TODO: update settings
                    matureContentAllowed = !matureContentAllowed
                },
                enabled = true, //TODO: not if logged in as guest
            )
        }
    )
}



@Composable
private fun PrivacyPolicySetting() {
    val context = LocalContext.current

    SettingsItem(
        iconId = R.drawable.ic_privacy_policy,
        contentDesc = "Privacy Policy icon",
        text = stringResource(R.string.settings_privacy_policy_text),
        extraContent = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_open_in_browser),
                contentDescription = "Privacy Policy icon",
                modifier = Modifier.clickable(
                    onClick = {
                        val url = "https://deviantart-app-tools.avrohomthousman.workers.dev/privacyPolicy"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.startActivity(intent)
                    }
                )
            )
        }
    )
}



/**
 * Displays a single settings item
 */
@Composable
private fun SettingsItem(
    iconId: Int,
    contentDesc: String,
    text: String,
    extraContent: @Composable () -> Unit
){
    Row(
        modifier = Modifier
            .height(70.dp)
            .topAndBottomBorder()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconId),
            contentDescription = contentDesc
        )
        Spacer(modifier = Modifier.width(8.dp))


        Text(
            text = text,
            fontSize = 18.sp
        )


        Spacer(modifier = Modifier.weight(1f))

        extraContent()
    }
}
