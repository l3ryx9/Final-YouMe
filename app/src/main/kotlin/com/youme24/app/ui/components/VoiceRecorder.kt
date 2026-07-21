package com.youme24.app.ui.components

import android.media.MediaRecorder
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.ErrorCoral
import com.youme24.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import java.io.File

/**
 * Enregistreur de messages vocaux — équivalent de VoiceRecorder.tsx.
 *
 * Techno : MediaRecorder (Android natif) remplace expo-av.
 * Animations : rememberInfiniteTransition → pulsation du bouton micro (Compose natif).
 * Format : AAC dans conteneur 3GP ou M4A.
 */
@Composable
fun VoiceRecorder(
    onDismiss: () -> Unit,
    onSend: (localPath: String, durationSecs: Int) -> Unit,
) {
    val context = LocalContext.current

    var isRecording  by remember { mutableStateOf(false) }
    var durationSecs by remember { mutableStateOf(0) }
    var recorder     by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputPath   by remember { mutableStateOf("") }

    // Pulsation micro
    val infiniteTransition = rememberInfiniteTransition(label = "mic-pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "micScale",
    )

    // Timer while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            durationSecs = 0
            while (isRecording) {
                delay(1000L)
                durationSecs++
                if (durationSecs >= 120) { /* Auto-stop at 2 min */ isRecording = false }
            }
        }
    }

    fun startRecording() {
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.aac")
        outputPath = file.absolutePath
        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputPath)
            prepare()
            start()
        }
        recorder = mr
        isRecording = true
    }

    fun stopRecording() {
        recorder?.apply { stop(); release() }
        recorder = null
        isRecording = false
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) stopRecording()
            onDismiss()
        },
        title = { Text("Message vocal") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Mic icon (pulsating when recording)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isRecording) micScale else 1f)
                        .background(
                            if (isRecording) ErrorCoral.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Mic, "Micro",
                        Modifier.size(36.dp),
                        tint = if (isRecording) ErrorCoral else MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                Text(
                    text  = if (isRecording) "Enregistrement… ${durationSecs}s" else "Appuyez pour enregistrer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (!isRecording) {
                    // Start recording
                    Button(onClick = { startRecording() }) {
                        Icon(Icons.Outlined.Mic, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Enregistrer")
                    }
                } else {
                    // Stop + send
                    Button(
                        onClick = {
                            stopRecording()
                            onSend(outputPath, durationSecs)
                        }
                    ) {
                        Icon(Icons.Outlined.Check, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Envoyer")
                    }
                    // Cancel recording
                    OutlinedButton(onClick = { stopRecording() }) {
                        Icon(Icons.Outlined.Close, null)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (isRecording) stopRecording(); onDismiss() }) {
                Text("Annuler")
            }
        },
    )
}
