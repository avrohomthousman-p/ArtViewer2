package com.housmantech.artviewer.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.housmantech.artviewer.R
import com.housmantech.artviewer.ui.themes.AppColors


/**
 * A dialog that allows the user to modify settings.
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current

    val settingsCategoryStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp,
        textDecoration = TextDecoration.Underline
    )

    val settingsItemStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.settings_title)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))


                // --- Section: Content ---
                Text(
                    text = stringResource(R.string.settings_content_section_title),
                    style = settingsCategoryStyle,
                    color = AppColors.SectionHeaderColor
                )
                Spacer(modifier = Modifier.height(10.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_mature_content_prompt),
                            style = settingsItemStyle
                        )
                        Text(
                            text = stringResource(R.string.coming_soon),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light,
                            lineHeight = 18.sp,
                            color = AppColors.MutedTextColor
                        )
                    }

                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        enabled = false
                    )
                }



                Spacer(modifier = Modifier.height(30.dp))



                // --- Section: Legal ---
                Text(
                    text = stringResource(R.string.settings_legal_section_title),
                    style = settingsCategoryStyle,
                    color = AppColors.SectionHeaderColor
                )
                Spacer(modifier = Modifier.height(10.dp))


                Text(
                    text = stringResource(R.string.settings_privacy_policy_prompt),
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://deviantart-app-tools.avrohomthousman.workers.dev/privacyPolicy".toUri()
                        )
                        context.startActivity(intent)
                    },
                    color = AppColors.LinkColor,
                    style = settingsItemStyle
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave() },
                content = {
                    Text(stringResource(R.string.settings_save_button_text))
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                content = {
                    Text(stringResource(R.string.cancel))
                }
            )
        }
    )
}
