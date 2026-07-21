package com.youme24.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.youme24.app.domain.model.MessageLocation
import com.youme24.app.ui.theme.HibiscusCoral
import com.youme24.app.ui.theme.Spacing
import com.youme24.app.ui.theme.WarningYellow

/**
 * Bulle de localisation dans le chat — équivalent de LocationBubble.tsx.
 *
 * Techno : Maps Compose pour la mini-carte, Material 3 pour les badges.
 * Animations : MotionLayout (expansion mini-carte → plein écran) est gérée
 * via LocationMapModal qui s'ouvre en fullscreen Dialog.
 */
@Composable
fun LocationBubble(
    location: MessageLocation?,
    isOwn: Boolean,
    modifier: Modifier = Modifier,
) {
    if (location == null) {
        Row(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.LocationOn, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text("Position partagée", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    var showMap by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { showMap = true }
    ) {
        // Mini map preview (160×120 dp)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude), 15f
            )
        }
        GoogleMap(
            modifier = Modifier.width(200.dp).height(130.dp),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                scrollGesturesEnabled = false,
                zoomGesturesEnabled   = false,
                rotationGesturesEnabled = false,
            ),
        ) {
            Marker(
                state = MarkerState(LatLng(location.latitude, location.longitude))
            )
        }

        // Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = Spacing.sm, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null,
                    Modifier.size(14.dp),
                    MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text("Position en direct", style = MaterialTheme.typography.labelSmall)
            }
            if (location.isMocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Warning, null,
                        Modifier.size(12.dp), WarningYellow)
                    Spacer(Modifier.width(2.dp))
                    Text("Simulée", style = MaterialTheme.typography.labelSmall,
                        color = WarningYellow)
                }
            }
        }
    }

    if (showMap) {
        LocationMapModal(location = location, onDismiss = { showMap = false })
    }
}

/**
 * Modal plein écran avec la carte — équivalent de LocationMapModal.tsx.
 *
 * Techno animations : MotionLayout décrit dans la classe parente (ConstraintLayout JSON5).
 * Ici, on utilise un Dialog Compose fullscreen pour simplifier.
 */
@Composable
fun LocationMapModal(location: MessageLocation, onDismiss: () -> Unit) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(location.latitude, location.longitude), 16f
        )
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
        ) {
            Box {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                ) {
                    Marker(state = MarkerState(LatLng(location.latitude, location.longitude)))
                }
                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.xs),
                ) {
                    Icon(androidx.compose.material.icons.Icons.Outlined.Close, "Fermer")
                }
            }
        }
    }
}
