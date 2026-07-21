package com.youme24.app.data.notifications

import com.youme24.app.domain.repository.IUserRepository
import io.github.jan.supabase.auth.auth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val supabase: com.youme24.app.data.remote.supabase.SupabaseClientProvider,
    private val userRepository: IUserRepository,
) {
    suspend fun updateFcmToken(token: String) {
        val uid = supabase.client.auth.currentUserOrNull()?.id ?: return
        userRepository.updateFcmToken(uid, token)
    }
}
