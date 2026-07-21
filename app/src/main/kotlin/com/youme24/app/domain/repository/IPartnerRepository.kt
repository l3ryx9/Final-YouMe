package com.youme24.app.domain.repository

import com.youme24.app.domain.model.Partner
import com.youme24.app.domain.model.PartnerRequest
import kotlinx.coroutines.flow.Flow

/**
 * Interface domaine — gestion des liaisons couple.
 * Équivalent de src/domain/repositories/IPartnerRepository.ts
 */
interface IPartnerRepository {
    suspend fun sendPartnerRequest(toUserId: String): Result<PartnerRequest>
    suspend fun acceptPartnerRequest(requestId: String): Result<Partner>
    suspend fun rejectPartnerRequest(requestId: String): Result<Unit>
    suspend fun getPartners(userId: String): Result<List<Partner>>
    suspend fun getPendingRequests(userId: String): Result<List<PartnerRequest>>
    suspend fun getSentRequests(userId: String): Result<List<PartnerRequest>>
    suspend fun removePartner(partnerId: String): Result<Unit>
    suspend fun isPartner(userId: String, targetId: String): Result<Boolean>
    fun observePartners(userId: String): Flow<List<Partner>>
    fun observePendingRequests(userId: String): Flow<List<PartnerRequest>>
}
