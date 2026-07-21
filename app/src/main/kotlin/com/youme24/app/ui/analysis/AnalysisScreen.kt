package com.youme24.app.ui.analysis

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.theme.GeminiViolet
import com.youme24.app.ui.theme.Spacing

/**
 * Écran analyse psychologique IA — équivalent de app/(app)/analysis/[id].tsx.
 *
 * Techno animations :
 * - Score de cohérence : animateFloatAsState (Compose natif) pour progression fluide
 * - Icône IA : rememberInfiniteTransition + scale pulsation (Compose natif)
 * - Expansion sections : AnimatedVisibility (Compose natif)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    conversationId: String,
    viewModel: AnalysisViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(conversationId) { viewModel.init(conversationId) }

    // Pulsation IA
    val infiniteTransition = rememberInfiniteTransition(label = "ai-pulse")
    val aiScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "aiScale",
    )

    // Animated coherence progress
    val animatedScore by animateFloatAsState(
        targetValue = uiState.report?.coherenceScore ?: 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "coherenceProgress",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analyse psychologique") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Retour") }
                },
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.AutoAwesome, null,
                        Modifier.size(56.dp).scale(aiScale),
                        tint = GeminiViolet,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    CircularProgressIndicator(color = GeminiViolet)
                    Spacer(Modifier.height(Spacing.sm))
                    Text("Gemini analyse la conversation…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                // Coherence score card
                uiState.report?.let { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Score de cohérence", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(Spacing.sm))
                            LinearProgressIndicator(
                                progress = { animatedScore },
                                modifier = Modifier.fillMaxWidth().height(12.dp),
                                color = when {
                                    animatedScore >= 0.7f -> MaterialTheme.colorScheme.primary
                                    animatedScore >= 0.4f -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${(animatedScore * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Deception risk
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text("Risque de déception", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            val riskColor = when (report.deceptionRiskLabel) {
                                com.youme24.app.domain.model.DeceptionRiskLabel.LOW -> MaterialTheme.colorScheme.primary
                                com.youme24.app.domain.model.DeceptionRiskLabel.MODERATE -> MaterialTheme.colorScheme.tertiary
                                com.youme24.app.domain.model.DeceptionRiskLabel.HIGH -> MaterialTheme.colorScheme.error
                            }
                            Text(
                                report.deceptionRiskLabel.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = riskColor,
                            )
                        }
                    }

                    // Emotional journey
                    if (report.emotionalJourney.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text("Parcours émotionnel", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                report.emotionalJourney.forEachIndexed { i, emotion ->
                                    Text("${i + 1}. $emotion", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    // Contradictions
                    if (report.contradictions.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text("Contradictions détectées (${report.contradictions.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(4.dp))
                                report.contradictions.forEach { c ->
                                    Text("• ${c.statement1} ↔ ${c.statement2}",
                                        style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                } ?: run {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucune analyse disponible pour aujourd'hui.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
