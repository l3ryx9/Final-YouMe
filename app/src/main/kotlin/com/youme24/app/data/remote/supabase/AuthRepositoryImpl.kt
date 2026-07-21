package com.youme24.app.data.remote.supabase

import com.youme24.app.domain.model.User
import com.youme24.app.domain.repository.IAuthRepository
import com.youme24.app.domain.repository.IUserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation Supabase de IAuthRepository.
 * Utilise le SDK Kotlin supabase-kt / Auth.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider,
    private val userRepository: IUserRepository,
) : IAuthRepository {

    private val auth get() = supabase.client.auth

    override val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override fun observeAuthState(): Flow<User?> =
        auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val uid = status.session.user?.id ?: return@map null
                    userRepository.getUserById(uid).getOrNull()
                }
                else -> null
            }
        }

    override suspend fun signIn(email: String, password: String): Result<User> =
        runCatching {
            auth.signInWith(Email) {
                this.email    = email
                this.password = password
            }
            val uid = auth.currentUserOrNull()?.id
                ?: error("No user after sign-in")
            userRepository.getUserById(uid).getOrThrow()
                ?: error("User record not found")
        }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String,
        displayName: String,
    ): Result<User> = runCatching {
        auth.signUpWith(Email) {
            this.email    = email
            this.password = password
        }
        val uid = auth.currentUserOrNull()?.id ?: error("No user after sign-up")
        val user = User(
            id = uid, email = email, username = username,
            displayName = displayName,
        )
        userRepository.createUser(user).getOrThrow()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.resetPasswordForEmail(email)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.updateUser { password = newPassword }
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val uid = currentUserId ?: error("Not authenticated")
        userRepository.deleteUser(uid).getOrThrow()
        auth.signOut()
    }

    override suspend fun verifyAntiBot(captchaToken: String): Result<Boolean> =
        Result.success(true) // Handled by the anti-bot-guard Edge Function

    override suspend fun refreshSession(): Result<Unit> = runCatching {
        auth.refreshCurrentSession()
    }

    override fun isAuthenticated(): Boolean =
        auth.currentUserOrNull() != null
}
