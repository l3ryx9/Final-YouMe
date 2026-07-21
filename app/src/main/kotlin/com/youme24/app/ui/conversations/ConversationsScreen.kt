package com.youme24.app.ui.conversations

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.domain.model.Conversation
import com.youme24.app.ui.components.Avatar
import com.youme24.app.ui.theme.Spacing

/**
 * Liste des conversations — équivalent de app/(app)/(tabs)/index.tsx.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel = hiltViewModel(),
    onOpenChat: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YouMe", style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp)) },
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            modifier    = Modifier.padding(padding),
            label       = "conversations",
        ) { loading ->
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.conversations.isEmpty()) {
                EmptyConversations()
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(uiState.conversations, key = { it.id }) { conv ->
                        ConversationItem(conversation = conv, onClick = { onOpenChat(conv.id) })
                        HorizontalDivider(Modifier.padding(start = 80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(photoUrl = null, size = 52.dp)
        Spacer(Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text  = conversation.participantIds.firstOrNull() ?: "Partenaire",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                conversation.lastMessage?.let {
                    Text(
                        text  = formatTime(it.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text      = conversation.lastMessage?.content ?: "Aucun message",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                    modifier  = Modifier.weight(1f),
                )
                if (conversation.unreadCount > 0) {
                    Badge { Text("${conversation.unreadCount}") }
                }
            }
        }
    }
}

@Composable
private fun EmptyConversations() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Chat, null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Pas encore de conversations",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Ajoutez un partenaire pour commencer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatTime(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMs
    return when {
        diff < 60_000L -> "maintenant"
        diff < 3_600_000L -> "${diff / 60_000}min"
        diff < 86_400_000L -> "${diff / 3_600_000}h"
        else -> "${diff / 86_400_000}j"
    }
}

private val Int.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
private val Float.sp get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)
