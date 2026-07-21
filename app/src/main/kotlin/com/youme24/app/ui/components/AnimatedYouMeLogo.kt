package com.youme24.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.youme24.app.R

/**
 * Mascotte "YouMe" animée — équivalent de AnimatedYouMe.tsx.
 *
 * Techno : Lottie (com.airbnb.android:lottie-compose).
 *
 * Pourquoi Lottie et pas animations Compose natives ?
 *  - La mascotte est une animation illustrative/décorative complexe (personnage animé)
 *    avec des courbes de Bézier, des calques imbriqués, et une temporisation précise.
 *  - Lottie permet de réutiliser directement le fichier JSON créé par le designer
 *    (After Effects / Figma → Lottie plugin), sans le recoder frame par frame.
 *  - Les animations Compose natives (animateFloatAsState, etc.) sont réservées aux
 *    micro-interactions UI simples (boutons, jauges, transitions).
 *
 * Fichier attendu : app/src/main/res/raw/youme_logo.json
 * (exporter depuis After Effects via le plugin Lottie/Bodymovin)
 */
@Composable
fun AnimatedYouMeLogo(
    size: Dp,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.youme_logo)
    )

    LottieAnimation(
        composition = composition,
        iterations  = iterations,
        modifier    = modifier.size(size),
    )
}

/**
 * Version statique (fallback si le fichier Lottie n'est pas encore disponible).
 * Utilise le logo PNG placé dans res/drawable/.
 */
@Composable
fun YouMeLogoFallback(size: Dp, modifier: Modifier = Modifier) {
    coil.compose.AsyncImage(
        model              = R.drawable.ic_youme_logo,
        contentDescription = "YouMe",
        modifier           = modifier.size(size),
    )
}
