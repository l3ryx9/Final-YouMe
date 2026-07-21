package com.youme24.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.*

/**
 * Indicateur de force du mot de passe — équivalent de PasswordStrengthBar.tsx.
 *
 * Techno animations : Compose natif.
 *  - animateFloatAsState → progression fluide de la barre
 *  - animateColorAsState → transition de couleur (rouge → orange → vert)
 *
 * Critères (reproduit les validateurs authValidators.ts) :
 *  - 8+ caractères       → +1
 *  - Majuscule           → +1
 *  - Chiffre             → +1
 *  - Caractère spécial   → +1
 */
@Composable
fun PasswordStrengthBar(
    password: String,
    modifier: Modifier = Modifier,
) {
    val strength = remember(password) { computeStrength(password) }
    val progress = strength / 4f

    val targetColor = when (strength) {
        0    -> MaterialTheme.colorScheme.surfaceVariant
        1    -> ErrorCoral
        2    -> WarningYellow
        3    -> OrangeAnanas
        else -> OnlineGreen
    }

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(400),
        label         = "strengthProgress",
    )
    val animatedColor by animateColorAsState(
        targetValue   = targetColor,
        animationSpec = tween(400),
        label         = "strengthColor",
    )

    val label = when (strength) {
        0    -> ""
        1    -> "Très faible"
        2    -> "Faible"
        3    -> "Moyen"
        else -> "Fort"
    }

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress      = { animatedProgress },
            modifier      = Modifier.fillMaxWidth().height(6.dp),
            color         = animatedColor,
            trackColor    = MaterialTheme.colorScheme.surfaceVariant,
        )
        if (label.isNotEmpty()) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = animatedColor,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private fun computeStrength(password: String): Int {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) score++
    return score
}
