package com.youme24.app.data.remote.supabase

import com.youme24.app.data.remote.supabase.dto.UserDto
import com.youme24.app.data.remote.supabase.dto.toDto
import com.youme24.app.domain.model.User
import com.youme24.app.domain.repository.IUserRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider,
) : IUserRepository {

    private val db get() = supabase.client.postgrest["users"]

    override suspend fun createUser(user: User): Result<User> = runCatching {
        db.insert(user.toDto()).decodeSingle<UserDto>().toDomain()
    }

    override suspend fun getUserById(id: String): Result<User?> = runCatching {
        db.select { filter { eq("id", id) } }.decodeSingleOrNull<UserDto>()?.toDomain()
    }

    override suspend fun getUserByUsername(username: String): Result<User?> = runCatching {
        db.select { filter { eq("username", username) } }
            .decodeSingleOrNull<UserDto>()?.toDomain()
    }

    override suspend fun updateUser(user: User): Result<User> = runCatching {
        db.update(user.toDto()) { filter { eq("id", user.id) } }
            .decodeSingle<UserDto>().toDomain()
    }

    override suspend fun deleteUser(id: String): Result<Unit> = runCatching {
        db.delete { filter { eq("id", id) } }
    }

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> = runCatching {
        val existing = db.select { filter { eq("username", username) } }
            .decodeSingleOrNull<UserDto>()
        existing == null
    }

    override suspend fun updateOnlineStatus(userId: String, isOnline: Boolean): Result<Unit> =
        runCatching {
            db.update({ set("is_online", isOnline); set("last_seen", System.currentTimeMillis()) }) {
                filter { eq("id", userId) }
            }
        }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> =
        runCatching {
            db.update({ set("fcm_token", token) }) { filter { eq("id", userId) } }
        }

    override suspend fun updateAiEnabled(userId: String, enabled: Boolean): Result<Unit> =
        runCatching {
            db.update({ set("ai_enabled", enabled) }) { filter { eq("id", userId) } }
        }

    override suspend fun updateE2EPublicKey(userId: String, publicKey: String): Result<Unit> =
        runCatching {
            db.update({ set("e2e_public_key", publicKey) }) { filter { eq("id", userId) } }
        }

    override suspend fun searchUsersByUsername(query: String, limit: Int): Result<List<User>> =
        runCatching {
            db.select {
                filter { ilike("username", "%$query%") }
                limit(limit.toLong())
            }.decodeList<UserDto>().map { it.toDomain() }
        }

    override fun observeUser(userId: String): Flow<User?> = flow {
        // Realtime subscription — simplified; use Supabase Realtime channel in production
        val user = getUserById(userId).getOrNull()
        emit(user)
    }
}
