package com.youme24.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Domaine : entité User (sans dépendance Android).
 * Équivalent de src/domain/User.ts (React Native).
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val photoUrl: String? = null,
    val bio: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,           // epoch ms
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isEmailVerified: Boolean = false,
    val aiEnabled: Boolean = true,
    val fcmToken: String? = null,
    val e2ePublicKey: String? = null,     // Base64-encoded X25519 public key
)
