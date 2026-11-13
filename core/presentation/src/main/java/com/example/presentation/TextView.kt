package com.example.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.presentation.model.TextLinkItem

@Composable
fun SmartAutoSizeText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    minFontSize: TextUnit = 8.sp,
    maxFontSize: TextUnit = 32.sp,
) {
    var finalFontSize by remember { mutableStateOf(maxFontSize) }
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier
    ) {
        val maxWidth = constraints.maxWidth.toFloat()
        LaunchedEffect(text, maxWidth, minFontSize, maxFontSize) {
            var left: Float = minFontSize.value
            var right: Float = maxFontSize.value
            var bestSize = left

            while (left <= right) {
                val size = ((left + right) / 2).toInt()
                val textLayoutResult = textMeasurer.measure(
                    text = text,
                    style = style.copy(fontSize = size.sp)
                )

                if (textLayoutResult.size.width <= maxWidth) {
                    bestSize = size.toFloat()
                    left = size + 1f
                } else {
                    right = size - 1f
                }
            }

            finalFontSize = bestSize.sp
        }

        Text(
            modifier = modifier,
            text = text,
            fontSize = finalFontSize,
            maxLines = 1,
            softWrap = false,
            style = style
        )
    }
}

@Composable
fun LinkText(
    modifier: Modifier = Modifier,
    @StringRes textResId: Int,
    links: List<TextLinkItem>,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Center,
    linkStyles: TextLinkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline
        )
    )
) {
    val resolvedLinks = links.map { item ->
        item.name.orEmpty().ifBlank { stringResource(item.textResId) } to item.url
    }

    val labels = resolvedLinks.map { it.first }.toTypedArray()
    val fullText = stringResource(textResId, *labels)

    val annotatedText = remember(fullText, resolvedLinks, linkStyles) {
        buildAnnotatedString {
            append(fullText)

            var searchFromIndex = 0
            resolvedLinks.forEach { (label, url) ->
                val startIndex = fullText.indexOf(label, startIndex = searchFromIndex)
                if (startIndex >= 0) {
                    val endIndex = startIndex + label.length
                    addLink(
                        url = LinkAnnotation.Url(
                            url = url,
                            styles = linkStyles
                        ),
                        start = startIndex,
                        end = endIndex
                    )
                    // Advance search index to safely handle duplicate link labels
                    searchFromIndex = endIndex
                }
            }
        }
    }

    Text(
        text = annotatedText,
        modifier = modifier,
        style = style,
        textAlign = textAlign
    )
}