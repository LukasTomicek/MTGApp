package mtg.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import mtg.app.core.presentation.theme.AppTheme

@Composable
fun CardImage(
    imageUrl: String?,
    heightDp: Int,
    modifier: Modifier = Modifier,
    previewEnabled: Boolean = true,
) {
    // MTG standard card ratio: 63x88 mm.
    val cardAspectRatio = 63f / 88f
    val resolvedHeight = heightDp.toFloat()
    val resolvedWidth = resolvedHeight * cardAspectRatio
    val cornerDp = (resolvedHeight * 0.045f).coerceIn(2f, 16f).dp
    var showPreview by remember(imageUrl, previewEnabled) { mutableStateOf(false) }

    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .size(width = resolvedWidth.dp, height = resolvedHeight.dp)
                .background(AppTheme.colors.primary100),
        )
    } else {
        AsyncImage(
            modifier = modifier
                .size(width = resolvedWidth.dp, height = resolvedHeight.dp)
                .clip(shape = RoundedCornerShape(cornerDp))
                .then(
                    if (previewEnabled) {
                        Modifier.clickable { showPreview = true }
                    } else {
                        Modifier
                    },
                ),
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }

    if (showPreview && !imageUrl.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showPreview = false },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showPreview = false },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 600.dp)
                        .clickable { showPreview = false },
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
