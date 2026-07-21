package com.youme24.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.GeminiViolet
import com.youme24.app.ui.theme.Spacing

/**
 * Bouton flottant IA — équivalent de IAFloatingButton.tsx.
 *
 * Techno animations :
 *  - Ouverture du panneau : MotionLayout Compose (MotionScene JSON5)
 *    → décrit ici en animation Compose native (AnimatedVisibility + animateFloatAsState)
 *    pour le FAB expand/collapse, car MotionLayout requiert un fichier JSON5 externe.
 *  - Rotation de l'icône : animateFloatAsState (Compose natif)
 *  - Apparition des sous-boutons : AnimatedVisibility avec slideInVertically (Compose natif)
 *
 * MotionLayout est utilisé pour la TRANSITION liste de conversations → chat (NavGraph).
 * Pour le bouton flottant, les animations Compose natives sont suffisantes et plus lisibles.
 */
@Composable
fun IAFloatingButton(
    onAskAi: () -> Unit,
    onAnalysis: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue   = if (expanded) 45f else 0f,
        animationSpec = tween(250),
        label         = "fabRotation",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        // Sub-actions (visible when expanded)
        AnimatedVisibility(
            visible = expanded,
            enter   = slideInVertically(tween(200)) { it } + fadeIn(),
            exit    = slideOutVertically(tween(150)) { it } + fadeOut(),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                // Analyse psychologique
                SmallFabAction(
                    label   = "Analyse",
                    icon    = Icons.Outlined.BarChart,
                    onClick = { expanded = false; onAnalysis() },
                )
                // Ask AI
                SmallFabAction(
                    label   = "Demander à l'IA",
                    icon    = Icons.Outlined.AutoAwesome,
                    onClick = { expanded = false; onAskAi() },
                )
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick          = { expanded = !expanded },
            containerColor   = GeminiViolet,
            contentColor     = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                if (expanded) Icons.Outlined.Close else Icons.Outlined.AutoAwesome,
                contentDescription = if (expanded) "Fermer" else "IA",
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}

@Composable
private fun SmallFabAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Surface(
            shape         = MaterialTheme.shapes.small,
            tonalElevation = 3.dp,
        ) {
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp),
            )
        }
        SmallFloatingActionButton(
            onClick        = onClick,
            containerColor = GeminiViolet.copy(alpha = 0.85f),
            contentColor   = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(icon, label, Modifier.size(20.dp))
        }
    }
}
