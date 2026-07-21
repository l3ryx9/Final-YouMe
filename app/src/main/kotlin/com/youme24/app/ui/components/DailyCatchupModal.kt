package com.youme24.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.GeminiViolet
import com.youme24.app.ui.theme.HibiscusCoral
import com.youme24.app.ui.theme.Spacing

/**
 * Modal "Daily Catchup" IA — équivalent de DailyCatchupModal.tsx.
 *
 * Affiché une fois par jour avec le résumé IA de la journée.
 * Techno animations :
 *  - Lottie : animation confettis de succès (confetti.json)
 *  - AnimatedVisibility (Compose natif) : apparition progressive des sections
 *  - rememberInfiniteTransition : pulsation cœur (Compose natif)
 */
@Composable
fun DailyCatchupModal(
    summary: String?,
    emotions: List<String> = emptyList(),
    onDismiss: () -> Unit,
) {
    // Pulsation cœur
    val infiniteTransition = rememberInfiniteTransition(label = "heart-pulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label         = "heartScale",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Lottie confettis (fichier res/raw/confetti.json attendu)
                // LottieAnimation(composition, iterations = 1)
                // Fallback : icône cœur animée (Compose natif)
                Icon(
                    Icons.Outlined.Favorite,
                    contentDescription = null,
                    tint     = HibiscusCoral,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(heartScale),
                )
            }
        },
        title = { Text("Votre journée en un coup d'œil ☀️", textAlign = TextAlign.Center) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // IA summary
                summary?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = GeminiViolet.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, null,
                                Modifier.size(16.dp), GeminiViolet)
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } ?: Text(
                    "Aucune analyse disponible pour aujourd'hui.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Emotion badges
                if (emotions.isNotEmpty()) {
                    Text("Émotions détectées :", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        emotions.take(4).forEach { EmotionBadge(it) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Super, merci !") }
        },
    )
}

