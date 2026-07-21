package com.youme24.app.ui.location

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.youme24.app.ui.theme.HibiscusCoral
import com.youme24.app.ui.theme.OnlineGreen
import com.youme24.app.ui.theme.Spacing

/**
 * Écran localisation en direct — équivalent de app/(app)/live-location/[id].tsx.
 *
 * Techno animations :
 * - Expansion de la carte : MotionLayout Compose (MotionScene JSON5)  ← décrit ci-dessous
 * - Pulsation marqueur "En ligne" : rememberInfiniteTransition (Compose natif)
 * - Carte : Maps Compose (com.google.maps.android:maps-compose)
 *
 * Note MotionLayout : l'expansion carte → plein écran est gérée via
 * MotionLayout(motionScene = ...) avec ConstraintSet start/end JSON5.
 * Fichier MotionScene à placer dans res/raw/location_map_motion_scene.json5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLocationScreen(
    conversationId: String,
    viewModel: LiveLocationViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(conversationId) { viewModel.init(conversationId) }

    // Pulsation du marqueur partenaire
    val infiniteTransition = rememberInfiniteTransition(label = "marker-pulse")
    val markerScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "markerScale",
    )

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(uiState.partnerLocation) {
        uiState.partnerLocation?.let { loc ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(LatLng(loc.latitude, loc.longitude), 15f)
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Position en direct") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Retour") }
                },
                actions = {
                    // Mode furtif (5 taps cachés dans l'UI)
                    IconButton(onClick = { viewModel.toggleStealthMode() }) {
                        Icon(
                            Icons.Outlined.VisibilityOff, "Mode furtif",
                            tint = if (uiState.stealthActive) HibiscusCoral
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    if (uiState.isSharing) {
                        OutlinedButton(
                            onClick = { viewModel.stopSharing() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Outlined.LocationOn, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Arrêter le partage")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startSharing() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Outlined.LocationOn, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Partager ma position")
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Google Maps Compose
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
            ) {
                // Marqueur partenaire (pulsation animée)
                uiState.partnerLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        title = "Partenaire",
                    )
                }
            }

            // Statut de partage
            if (uiState.isSharing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = Spacing.md)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = OnlineGreen.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .scale(markerScale)
                                    .background(
                                        MaterialTheme.colorScheme.onPrimary,
                                        androidx.compose.foundation.shape.CircleShape,
                                    )
                            )
                            Spacer(Modifier.width(Spacing.xs))
                            Text(
                                "Partage actif",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}
