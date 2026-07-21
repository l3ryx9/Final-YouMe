package com.youme24.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.youme24.app.domain.model.Message
import com.youme24.app.domain.model.MessageStatus
import com.youme24.app.domain.model.MessageType
import com.youme24.app.ui.theme.*

/**
 * Bulle de message — équivalent de MessageBubble.tsx.
 *
 * Techno animations : Compose natif.
 *  - Apparition : slideInVertically + fadeIn (AnimatedVisibility)
 *  - Badges émotion IA : EmotionBadge (Compose natif, animateColorAsState)
 *  - Long press → menu réactions (DropdownMenu)
 *  - Bouton sparkle IA → onAskAi callback
 *
 * Note : le déchiffrement E2E est effectué dans le ViewModel avant d'afficher
 * le contenu — la bulle n'expose jamais le texte chiffré brut.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOwn: Boolean,
    onReaction: (String) -> Unit,
    onDelete: () -> Unit,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReactions by remember { mutableStateOf(false) }
    var showAiPanel   by remember { mutableStateOf(false) }

    val bubbleColor = if (isOwn)
        Brush.linearGradient(listOf(BubbleOwn, BubbleOwnGradientEnd))
    else null

    val bubbleSolidColor = if (!isOwn) MaterialTheme.colorScheme.surfaceVariant else null
    val textColor        = if (isOwn) Color(0xFF1A1200) else MaterialTheme.colorScheme.onSurface
    val bubbleShape      = if (isOwn) BubbleOwnShape else BubbleOtherShape

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
    ) {
        // Avatar (partenaire seulement)
        if (!isOwn) {
            Avatar(photoUrl = null, size = AvatarSize.sm)
            Spacer(Modifier.width(Spacing.xs))
        }

        Column(horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start) {
            // Bubble
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(bubbleShape)
                    .then(
                        if (bubbleColor != null)
                            Modifier.background(bubbleColor)
                        else
                            Modifier.background(bubbleSolidColor ?: MaterialTheme.colorScheme.surfaceVariant)
                    )
                    .combinedClickable(
                        onClick      = {},
                        onLongClick  = { showReactions = !showReactions },
                    )
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            ) {
                Column {
                    when (message.type) {
                        MessageType.TEXT -> {
                            Text(
                                text  = message.content ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                            )
                        }
                        MessageType.VOICE -> {
                            VoiceMessagePlayer(
                                localPath    = message.voiceLocalPath,
                                storageUrl   = message.storageUrl,
                                durationSecs = message.voiceDuration ?: 0,
                                isOwn        = isOwn,
                            )
                            message.voiceTranscription?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text      = "\"$it\"",
                                    style     = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                    color     = textColor.copy(alpha = 0.8f),
                                )
                            }
                        }
                        MessageType.LOCATION -> {
                            LocationBubble(
                                location = message.location,
                                isOwn    = isOwn,
                            )
                        }
                        MessageType.IMAGE -> {
                            // Coil 3 loads image from storageUrl or localPath
                            coil.compose.AsyncImage(
                                model             = message.storageUrl ?: message.imageLocalPath,
                                contentDescription = "Image",
                                modifier          = Modifier
                                    .widthIn(max = 240.dp)
                                    .heightIn(max = 200.dp)
                                    .clip(MaterialTheme.shapes.medium),
                            )
                        }
                        MessageType.SYSTEM -> {
                            Text(
                                text  = message.content ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                color = textColor.copy(alpha = 0.7f),
                            )
                        }
                        else -> {
                            Text(
                                text  = message.content ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                            )
                        }
                    }

                    // Timestamp + status row
                    Row(
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text  = formatMessageTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.6f),
                        )
                        if (isOwn) {
                            MessageStatusIcon(status = message.status)
                        }
                    }
                }
            }

            // Emotion badges (IA)
            message.aiAnalysis?.emotions?.takeIf { it.isNotEmpty() }?.let { emotions ->
                AnimatedVisibility(visible = showAiPanel, enter = fadeIn() + slideInVertically()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        emotions.take(3).forEach { emotion -> EmotionBadge(emotion = emotion) }
                    }
                }
            }

            // Reactions
            message.reactions.takeIf { it.isNotEmpty() }?.let { reactions ->
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    reactions.values.distinct().forEach { emoji ->
                        val count = reactions.values.count { it == emoji }
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            tonalElevation = 2.dp,
                        ) {
                            Text(
                                text     = "$emoji $count",
                                style    = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }

            // AI sparkle button
            if (message.aiAnalysis != null) {
                IconButton(
                    onClick = { showAiPanel = !showAiPanel },
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome, "Analyse IA",
                        tint = GeminiViolet, modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        if (isOwn) Spacer(Modifier.width(Spacing.xs))
    }

    // Reaction picker dropdown
    DropdownMenu(
        expanded         = showReactions,
        onDismissRequest = { showReactions = false },
    ) {
        listOf("❤️", "😂", "😮", "😢", "👏", "🔥").forEach { emoji ->
            DropdownMenuItem(
                text    = { Text(emoji) },
                onClick = { onReaction(emoji); showReactions = false },
            )
        }
        DropdownMenuItem(
            text    = { Text("Supprimer", color = MaterialTheme.colorScheme.error) },
            onClick = { onDelete(); showReactions = false },
        )
        DropdownMenuItem(
            text    = { Text("Demander à l'IA") },
            onClick = { onAskAi(); showReactions = false },
        )
    }
}

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    val (tint, label) = when (status) {
        MessageStatus.SENDING   -> MaterialTheme.colorScheme.onSurfaceVariant to "Envoi…"
        MessageStatus.SENT      -> MaterialTheme.colorScheme.onSurfaceVariant to "Envoyé"
        MessageStatus.DELIVERED -> MaterialTheme.colorScheme.onSurfaceVariant to "Livré"
        MessageStatus.READ      -> ReadOrange to "Lu"
        MessageStatus.FAILED    -> ErrorCoral to "Erreur"
    }
    Text(
        text  = when (status) {
            MessageStatus.SENDING   -> "○"
            MessageStatus.SENT      -> "✓"
            MessageStatus.DELIVERED -> "✓✓"
            MessageStatus.READ      -> "✓✓"
            MessageStatus.FAILED    -> "!"
        },
        style = MaterialTheme.typography.labelSmall,
        color = tint,
    )
}

private fun formatMessageTime(epochMs: Long): String {
    val cal = java.util.Calendar.getInstance().also { it.timeInMillis = epochMs }
    return "%02d:%02d".format(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
}
