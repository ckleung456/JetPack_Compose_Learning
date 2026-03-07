package com.example.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.ui.model.ImageItemType
import com.example.core.ui.model.ImageListItem

@Composable
fun FullScreenCenteredContent(
    imageUrl: String? = null,
    icon: ImageVector? = null,
    iconResId: Int? = null,
    title: String? = null,
    description: String? = null,
    descriptions: List<String>? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    descriptionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Icon
            if (imageUrl.isNullOrBlank().not()) {
                ImageItemView(
                    item = ImageListItem(
                        imageUrl = imageUrl,
                        type = ImageItemType.LARGE
                    )
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(120.dp)
                )
            } else if (iconResId != null) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Title
            if (title.isNullOrBlank().not()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = titleColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }


            // Description
            if (description.isNullOrBlank().not()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = descriptionColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.8f)
                )
            }

            if (descriptions.isNullOrEmpty().not()) {
                descriptions.filter { it.isNotBlank() }.let {
                    LazyColumn(
                        modifier = modifier,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(descriptions) {
                            SimpleListRowItem(
                                title = it,
                                hasArrowIcon = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenContentWithActions(
    icon: ImageVector,
    title: String,
    description: String,
    primaryButtonText: String? = null,
    secondaryButtonText: String? = null,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon at the top
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        // Buttons at the bottom
        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (primaryButtonText != null) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = primaryButtonText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (secondaryButtonText != null) {
                TextButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(
                        text = secondaryButtonText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}