package com.youme24.app.domain.repository

import com.youme24.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface domaine — aucune dépendance Android/Supabase.
 * Équivalent de src/domain/repositories/IUserRepository.ts
 */
interface IUserRepository {
    suspend fun createUser(user: User): Result<User>
    suspend fun getUserById(id: String): Result<User?>
    suspend fun getUserByUsername(username: String): Result<User?>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(id: String): Result<Unit>
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean): Result<Unit>
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>
    suspend fun updateAiEnabled(userId: String, enabled: Boolean): Result<Unit>
    suspend fun searchUsersByUsername(query: String, limit: Int = 20): Result<List<User>>
    suspend fun updateE2EPublicKey(userId: String, publicKey: String): Result<Unit>
    fun observeUser(userId: String): Flow<User?>
}
