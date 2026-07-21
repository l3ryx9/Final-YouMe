package com.youme24.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.components.AnimatedYouMeLogo
import com.youme24.app.ui.components.Bubble3DButton
import com.youme24.app.ui.components.PineapplePatternBackground
import com.youme24.app.ui.components.ThemedAlert
import com.youme24.app.ui.theme.Spacing
import com.youme24.app.ui.theme.youMeColors

/**
 * Écran de connexion — équivalent de app/(auth)/login.tsx.
 *
 * Animations : PineapplePatternBackground (Canvas Compose, boucle lente),
 * AnimatedYouMeLogo (Lottie), Bubble3DButton (animation Compose native).
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var showPass  by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fond décoratif — ananas stylisés en pattern répété (Canvas Compose, faible opacité)
        PineapplePatternBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.xxl))

            // Logo animé (Lottie)
            AnimatedYouMeLogo(size = 120.dp)

            Spacer(Modifier.height(Spacing.sm))

            // Titre — Dancing Script
            Text(
                text  = "YouMe",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text  = "Connecte-toi à ton espace couple",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.xl))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, "Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.sm))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                leadingIcon  = { Icon(Icons.Outlined.Lock, "Mot de passe") },
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showPass) "Masquer" else "Afficher"
                        )
                    }
                },
                visualTransformation = if (showPass) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus(); viewModel.login(email, password) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.sm))

            // Mot de passe oublié
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Mot de passe oublié ?")
            }

            Spacer(Modifier.height(Spacing.md))

            // Error message
            AnimatedVisibility(visible = uiState.error != null) {
                uiState.error?.let {
                    ThemedAlert(message = it, onDismiss = viewModel::clearError)
                    Spacer(Modifier.height(Spacing.sm))
                }
            }

            // Bouton 3D principal — animation Compose native (Bubble3DButton)
            Bubble3DButton(
                text      = "Se connecter",
                onClick   = { viewModel.login(email, password) },
                isLoading = uiState.isLoading,
                modifier  = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Pas encore de compte ?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateRegister) {
                    Text("S'inscrire")
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
