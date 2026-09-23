package com.housmantech.artviewer.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.housmantech.artviewer.ui.themes.AppColors



private const val TAG = "hyperlink"


/**
 * Displays a paragraph of text that includes one or more hyperlinks
 */
@Composable
fun ParagraphWithLinks(
    modifier: Modifier = Modifier,
    fullText: String,
    links: Map<String, (() -> Unit)>, // linkText → click event
    fontSize: TextUnit = 16.sp
) {
    val annotated = remember(fullText, links) {
        buildAnnotatedString {
            append(fullText)

            links.forEach { (linkText, onClick) ->
                val start = fullText.indexOf(linkText)
                if (start != -1) {
                    addStyle(
                        SpanStyle(
                            color = AppColors.LinkColor,
                            textDecoration = TextDecoration.Underline
                        ),
                        start,
                        start + linkText.length
                    )

                    addStringAnnotation(
                        tag = TAG,
                        annotation = linkText,
                        start,
                        start + linkText.length
                    )
                }
            }
        }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        fontSize = fontSize,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { pos ->
                val result = layout ?: return@detectTapGestures
                val offset = result.getOffsetForPosition(pos)

                annotated.getStringAnnotations(TAG, offset, offset)
                    .firstOrNull()
                    ?.let { annotation ->
                        // Lookup the click event from the map
                        links[annotation.item]?.invoke()
                    }
            }
        },
        onTextLayout = { layout = it }
    )
}
