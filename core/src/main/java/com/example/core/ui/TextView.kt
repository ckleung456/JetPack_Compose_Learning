package com.example.core.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

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