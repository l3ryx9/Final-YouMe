package com.youme24.app.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.youme24.app.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Lecteur de message vocal — équivalent de VoiceMessagePlayer.tsx.
 *
 * Techno : ExoPlayer (Media3) remplace expo-av.
 * Animations : animateFloatAsState → progression de la barre (Compose natif).
 */
@OptIn(UnstableApi::class)
@Composable
fun VoiceMessagePlayer(
    localPath: String?,
    storageUrl: String?,
    durationSecs: Int,
    isOwn: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val source  = localPath ?: storageUrl ?: return

    var isPlaying by remember { mutableStateOf(false) }
    var position  by remember { mutableStateOf(0f) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(source))
            prepare()
        }
    }

    DisposableEffect(Unit) { onDispose { player.release() } }

    // Track playback position
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val dur = player.duration.takeIf { it > 0 } ?: 1L
            position = (player.currentPosition.toFloat() / dur).coerceIn(0f, 1f)
            if (!player.isPlaying) { isPlaying = false; position = 0f }
            delay(200L)
        }
    }

    val animatedPosition by animateFloatAsState(
        targetValue   = position,
        animationSpec = tween(200),
        label         = "voiceProgress",
    )

    val tintColor = if (isOwn) Color(0xFF1A1200) else MaterialTheme.colorScheme.onSurface

    Row(
        modifier           = modifier.width(200.dp),
        verticalAlignment  = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    player.pause()
                    isPlaying = false
                } else {
                    player.seekTo(0)
                    player.play()
                    isPlaying = true
                }
            },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Lecture",
                tint = tintColor,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress       = { animatedPosition },
                modifier       = Modifier.fillMaxWidth().height(4.dp),
                color          = if (isOwn) MaterialTheme.colorScheme.onPrimary
                                 else MaterialTheme.colorScheme.primary,
                trackColor     = tintColor.copy(alpha = 0.2f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = formatDuration(if (isPlaying) (position * durationSecs).toInt() else durationSecs),
                style = MaterialTheme.typography.labelSmall,
                color = tintColor.copy(alpha = 0.7f),
            )
        }
    }
}

private fun formatDuration(secs: Int): String =
    "%d:%02d".format(secs / 60, secs % 60)
