package com.youme24.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.youme24.app.ui.theme.AvatarShape

/**
 * Avatar circulaire — équivalent de Avatar.tsx.
 *
 * Techno : Coil 3 (io.coil-kt.coil3:coil-compose) avec :
 *   - cache mémoire + disque (par défaut dans Coil 3)
 *   - transformation en cercle (clip AvatarShape = RoundedCornerShape(50%))
 *   - crossfade d'entrée (crossfade = true)
 *   - placeholder (icône Person de Material Icons)
 */
@Composable
fun Avatar(
    photoUrl: String?,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
) {
    if (photoUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            contentScale       = ContentScale.Crop,
            modifier           = modifier
                .size(size)
                .clip(AvatarShape),
            loading = { AvatarPlaceholder(size) },
            error   = { AvatarPlaceholder(size) },
        )
    } else {
        AvatarPlaceholder(size, modifier)
    }
}

@Composable
private fun AvatarPlaceholder(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(AvatarShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Person, contentDescription = null,
            tint     = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}
