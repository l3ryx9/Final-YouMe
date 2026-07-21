package com.youme24.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.*

/**
 * Badge d'émotion IA — équivalent de EmotionBadge.tsx (src/presentation/components/ai/).
 *
 * Techno animations : Compose natif.
 *  - animateColorAsState → transition douce de couleur quand l'émotion change
 *
 * Mapping émotion → couleur (reproduit emotionColors.ts) :
 *   joie/amour/confiance → jaune/orange ananas
 *   tristesse/peur       → bleu lagon
 *   colère/dégoût        → corail/hibiscus
 *   surprise             → vert tropical
 *   anxiété              → orange foncé
 */
@Composable
fun EmotionBadge(
    emotion: String,
    modifier: Modifier = Modifier,
) {
    val targetColor = emotionToColor(emotion)
    val animatedColor by animateColorAsState(
        targetValue    = targetColor,
        animationSpec  = tween(400),
        label          = "emotionColor",
    )

    Text(
        text  = emotionLabel(emotion),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(animatedColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

private fun emotionToColor(emotion: String): Color = when (emotion.lowercase()) {
    "joie", "heureux", "happy", "joy"          -> PineappleYellow
    "amour", "love", "tendresse"                -> HibiscusCoral
    "confiance", "trust"                        -> TropicalGreenLight
    "tristesse", "sad", "sadness"               -> AccentLagoon
    "peur", "fear", "anxiété", "anxiety"        -> OrangeAnanas
    "colère", "anger", "angry"                  -> ErrorCoral
    "dégoût", "disgust"                         -> TropicalGreenDark
    "surprise"                                  -> OrangeLight
    else                                        -> PineappleYellow.copy(alpha = 0.8f)
}

private fun emotionLabel(emotion: String): String =
    emotion.replaceFirstChar { it.uppercaseChar() }.take(12)
