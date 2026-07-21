package com.youme24.app.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.components.*
import com.youme24.app.ui.theme.Spacing

/**
 * Écran de chat 1-to-1 — équivalent de app/(app)/chat/[id].tsx.
 *
 * Techno animations :
 * - Apparition des bulles : AnimatedVisibility + slideIn (Compose natif)
 * - Jauge IA : animateFloatAsState + pulsation rememberInfiniteTransition (Compose natif)
 * - Bouton flottant IA : IAFloatingButton (Compose natif + MotionLayout pour ouverture panneau)
 * - Badges émotion : EmotionBadge (Compose natif)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    viewModel: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onOpenFlags: () -> Unit,
) {
    val uiState     by viewModel.uiState.collectAsState()
    val listState   = rememberLazyListState()
    var inputText   by remember { mutableStateOf("") }
    var showGemini  by remember { mutableStateOf(false) }
    var showVoice   by remember { mutableStateOf(false) }

    LaunchedEffect(conversationId) { viewModel.init(conversationId) }

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, "Retour")
                    }
                },
                title = {
                    Column {
                        Text(
                            uiState.partnerDisplayName.ifEmpty { "Chat" },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.partnerIsOnline) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .background(
                                            MaterialTheme.youMeColors.onlineIndicator,
                                            shape = androidx.compose.foundation.shape.CircleShape,
                                        )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("En ligne", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                },
                actions = {
                    // Jauge IA — pulsation animée (Compose natif)
                    uiState.coherenceScore?.let { score ->
                        CoherenceGauge(score = score)
                        Spacer(Modifier.width(Spacing.sm))
                    }
                    IconButton(onClick = onOpenLocation) {
                        Icon(Icons.Outlined.LocationOn, "Position en direct")
                    }
                    IconButton(onClick = onOpenFlags) {
                        Icon(Icons.Outlined.Flag, "Red flags")
                    }
                },
            )
        },
        bottomBar = {
            ChatInputBar(
                text          = inputText,
                onTextChange  = { inputText = it },
                onSend        = { viewModel.sendTextMessage(inputText); inputText = "" },
                onVoice       = { showVoice = true },
                onAttach      = { /* Media picker */ },
                isSending     = uiState.isSending,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Message list
            LazyColumn(
                state   = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = slideInVertically { it / 2 } + fadeIn(),
                    ) {
                        MessageBubble(
                            message    = message,
                            isOwn      = message.senderId == (uiState.partnerDisplayName),
                            onReaction = { emoji -> viewModel.addReaction(message.id, emoji) },
                            onDelete   = { viewModel.deleteMessage(message.id) },
                            onAskAi    = { /* Show AI analysis panel */ },
                        )
                    }
                }
            }

            // Bouton flottant IA (Compose natif + MotionLayout pour panneau)
            IAFloatingButton(
                modifier  = Modifier.align(Alignment.BottomEnd).padding(Spacing.md),
                onAskAi   = { showGemini = true },
                onAnalysis = onOpenAnalysis,
            )
        }
    }

    // Modal "Demander à l'IA"
    if (showGemini) {
        GeminiAskModal(
            onDismiss = { showGemini = false },
            onAsk     = { prompt -> viewModel.askGemini(prompt) { /* show result */ } },
        )
    }

    // Enregistreur vocal (Compose natif + MediaRecorder)
    if (showVoice) {
        VoiceRecorder(
            onDismiss  = { showVoice = false },
            onSend     = { path, duration ->
                viewModel.sendVoiceMessage(path, duration)
                showVoice = false
            },
        )
    }
}

// ── Jauge de cohérence IA — pulsation Compose ─────────────────
@Composable
private fun CoherenceGauge(score: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "gaugeAlpha",
    )
    val color = when {
        score >= 0.7f -> MaterialTheme.colorScheme.primary
        score >= 0.4f -> MaterialTheme.colorScheme.tertiary
        else          -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color.copy(alpha = alpha),
                shape = androidx.compose.foundation.shape.CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = "${(score * 100).toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

// ── Barre de saisie ───────────────────────────────────────────
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit,
    onAttach: () -> Unit,
    isSending: Boolean,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onAttach) {
                Icon(Icons.Outlined.AttachFile, "Pièce jointe")
            }
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Message…") },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                shape = MaterialTheme.shapes.extraLarge,
            )
            if (text.isBlank()) {
                IconButton(onClick = onVoice) {
                    Icon(Icons.Outlined.Mic, "Message vocal")
                }
            } else {
                IconButton(onClick = onSend, enabled = !isSending) {
                    if (isSending) CircularProgressIndicator(Modifier.size(20.dp))
                    else Icon(Icons.Outlined.Send, "Envoyer",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
