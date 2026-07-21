package com.youme24.app.domain.repository

import com.youme24.app.domain.model.ConversationSummary
import com.youme24.app.domain.model.DailyAnalysisReport
import com.youme24.app.domain.model.InconsistencyRecord
import com.youme24.app.domain.model.MemoryEntry
import com.youme24.app.domain.model.RelationalFlag

/**
 * Interface domaine — mémoire IA, résumés, incohérences, flags.
 * Équivalent de src/domain/repositories/IMemoryRepository.ts
 */
interface IMemoryRepository {
    // Memory entries
    suspend fun saveMemoryEntry(entry: MemoryEntry): Result<MemoryEntry>
    suspend fun getMemoryEntries(conversationId: String): Result<List<MemoryEntry>>
    suspend fun searchMemory(conversationId: String, query: String): Result<List<MemoryEntry>>
    suspend fun deleteAllMemory(conversationId: String): Result<Unit>
    suspend fun exportMemory(conversationId: String): Result<String>   // JSON string
    suspend fun getMemoryStats(conversationId: String): Result<Map<String, Int>>

    // Summaries
    suspend fun saveSummary(summary: ConversationSummary): Result<ConversationSummary>
    suspend fun getSummaries(conversationId: String): Result<List<ConversationSummary>>

    // Inconsistencies
    suspend fun saveInconsistency(record: InconsistencyRecord): Result<InconsistencyRecord>
    suspend fun getInconsistencies(conversationId: String): Result<List<InconsistencyRecord>>
    suspend fun updateInconsistencyReviewed(id: String, reviewed: Boolean): Result<Unit>

    // Flags
    suspend fun saveFlag(flag: RelationalFlag): Result<RelationalFlag>
    suspend fun getFlags(conversationId: String): Result<List<RelationalFlag>>
    suspend fun deleteFlag(flagId: String): Result<Unit>

    // Daily analysis
    suspend fun getDailyAnalysis(conversationId: String, date: String): Result<DailyAnalysisReport?>
    suspend fun saveDailyAnalysis(report: DailyAnalysisReport): Result<DailyAnalysisReport>
}
