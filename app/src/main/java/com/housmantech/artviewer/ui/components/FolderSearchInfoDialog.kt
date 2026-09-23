package com.housmantech.artviewer.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.housmantech.artviewer.R


/**
 * Dialog that shows some instructions on how to get data needed for a folder search.
 */
@Composable
fun FolderSearchInfoDialog(dismissDialog: () -> Unit) {
    Dialog(onDismissRequest = { dismissDialog() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dialog_search_info_title),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                MainInfoText()

                Spacer(Modifier.height(40.dp))

                StandardButton(
                    modifier = Modifier,
                    onClick = { dismissDialog() },
                    text = stringResource(R.string.got_it)
                )
            }
        }
    }
}



@Composable
private fun MainInfoText() {
    val context = LocalContext.current

    val navigateToDeviantArt = {
        val linkUrl = "https://www.deviantart.com"
        val intent = Intent(Intent.ACTION_VIEW, linkUrl.toUri())
        context.startActivity(intent)
    }

    ParagraphWithLinks(
        fullText = stringResource(R.string.dialog_search_info_text),
        links = mapOf(
            stringResource(R.string.dialog_search_info_clickable_text) to navigateToDeviantArt
        ),
        fontSize = 18.sp
    )
}
