package com.youme24.app.domain.repository

import com.youme24.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface domaine — authentification (Supabase Auth côté Android).
 * Pas de dépendance sur supabase-kt ici.
 */
interface IAuthRepository {
    val currentUserId: String?
    fun observeAuthState(): Flow<User?>

    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        displayName: String,
    ): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun verifyAntiBot(captchaToken: String): Result<Boolean>
    suspend fun refreshSession(): Result<Unit>
    fun isAuthenticated(): Boolean
}
