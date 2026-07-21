package com.youme24.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Domaine : entité Partner (liaison couple).
 * Équivalent de src/domain/Partner.ts (React Native).
 */
@Serializable
data class Partner(
    val userId: String,
    val partnerId: String,
    val partnerUsername: String,
    val partnerDisplayName: String,
    val partnerPhotoUrl: String? = null,
    val partnerIsOnline: Boolean = false,
    val partnerLastSeen: Long? = null,    // epoch ms
    val conversationId: String,
    val createdAt: Long = 0L,
)

/**
 * Demande de liaison couple (pending).
 */
@Serializable
data class PartnerRequest(
    val id: String,
    val fromUserId: String,
    val fromUsername: String,
    val fromDisplayName: String,
    val fromPhotoUrl: String? = null,
    val toUserId: String,
    val status: PartnerRequestStatus,
    val createdAt: Long = 0L,
)

enum class PartnerRequestStatus { PENDING, ACCEPTED, REJECTED }
