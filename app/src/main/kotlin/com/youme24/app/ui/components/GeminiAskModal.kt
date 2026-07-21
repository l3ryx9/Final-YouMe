package com.youme24.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.GeminiViolet
import com.youme24.app.ui.theme.GeminiVioletLight
import com.youme24.app.ui.theme.Spacing

/**
 * Modal "Demander à l'IA" — équivalent de GeminiAskModal.tsx.
 *
 * Techno animations : Compose natif.
 *  - rememberInfiniteTransition → rotation icône IA pendant chargement
 *  - AnimatedVisibility → apparition de la réponse
 */
@Composable
fun GeminiAskModal(
    onDismiss: () -> Unit,
    onAsk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var prompt    by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var response  by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "gemini-spin")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label         = "geminiRotation",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint     = GeminiViolet,
                modifier = if (isLoading) Modifier.rotate(iconRotation) else Modifier,
            )
        },
        title = { Text("Demander à l'IA") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "Posez une question sur votre conversation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value         = prompt,
                    onValueChange = { prompt = it },
                    placeholder   = { Text("Ex : Comment améliorer notre communication ?") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                    maxLines      = 4,
                    shape         = MaterialTheme.shapes.medium,
                )

                AnimatedVisibility(
                    visible = response != null,
                    enter   = fadeIn() + expandVertically(),
                ) {
                    response?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = GeminiVioletLight.copy(alpha = 0.15f)
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
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (prompt.isBlank()) return@Button
                    isLoading = true
                    onAsk(prompt)
                    // The parent sets isLoading = false via callback
                },
                enabled = prompt.isNotBlank() && !isLoading,
                colors  = ButtonDefaults.buttonColors(containerColor = GeminiViolet),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Send, null, Modifier.size(16.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(if (isLoading) "Analyse…" else "Envoyer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        },
    )
}
