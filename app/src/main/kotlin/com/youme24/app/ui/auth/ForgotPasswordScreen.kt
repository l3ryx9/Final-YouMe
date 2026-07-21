package com.youme24.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.components.Bubble3DButton
import com.youme24.app.ui.components.ThemedAlert
import com.youme24.app.ui.theme.Spacing

/**
 * Écran mot de passe oublié — équivalent de app/(auth)/forgot-password.tsx.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mot de passe oublié") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, "Retour")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Réinitialiser le mot de passe",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "Entrez votre email pour recevoir un lien de réinitialisation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.xl))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(Spacing.md))

            AnimatedVisibility(visible = uiState.error != null) {
                uiState.error?.let {
                    ThemedAlert(message = it, onDismiss = viewModel::clearError)
                    Spacer(Modifier.height(Spacing.sm))
                }
            }
            AnimatedVisibility(visible = uiState.successMessage != null) {
                uiState.successMessage?.let {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )) {
                        Text(it, modifier = Modifier.padding(Spacing.md))
                    }
                    Spacer(Modifier.height(Spacing.sm))
                }
            }

            Bubble3DButton(
                text = "Envoyer le lien",
                onClick = { viewModel.sendPasswordReset(email) },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
