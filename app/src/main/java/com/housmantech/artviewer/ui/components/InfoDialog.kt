package com.housmantech.artviewer.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.housmantech.artviewer.R
import androidx.core.net.toUri
import com.housmantech.artviewer.ui.themes.AppColors


/**
 * Dialog that shows some information.
 */
@Composable
fun InfoDialog(dismissDialog: () -> Unit) {
    Dialog(onDismissRequest = { dismissDialog() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dialog_info_title),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                MainInfoText()

                Spacer(Modifier.height(32.dp))

                StandardButton(
                    modifier = Modifier,
                    onClick = { dismissDialog() },
                    text = stringResource(R.string.dialog_info_button_text)
                )
            }
        }
    }
}



@Composable
private fun MainInfoText() {
    val context = LocalContext.current

    val fullText = stringResource(R.string.dialog_info_text)
    val clickableText = stringResource(R.string.dialog_info_clickable_text)


    val annotatedText = buildAnnotatedString {
        append(fullText)

        val startIndex = fullText.indexOf(clickableText)
        if (startIndex != -1) {
            addStyle(
                style = SpanStyle(
                    color = AppColors.LinkColor,
                    textDecoration = TextDecoration.Underline
                ),
                start = startIndex,
                end = startIndex + clickableText.length
            )

            addStringAnnotation(
                tag = "URL",
                annotation = "URL",
                start = startIndex,
                end = startIndex + clickableText.length
            )
        }
    }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedText,
        fontSize = 18.sp,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { tapOffset ->
                layoutResult?.let { layout ->
                    val offset = layout.getOffsetForPosition(tapOffset)
                    annotatedText.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()
                        ?.let {
                            val linkUrl = "https://www.deviantart.com"
                            val intent = Intent(Intent.ACTION_VIEW, linkUrl.toUri())
                            context.startActivity(intent)
                        }
                }
            }
        },
        onTextLayout = { layoutResult = it }
    )
}
