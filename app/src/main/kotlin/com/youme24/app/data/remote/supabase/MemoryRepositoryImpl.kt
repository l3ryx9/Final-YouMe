package com.youme24.app.data.remote.supabase

import com.youme24.app.domain.model.*
import com.youme24.app.domain.repository.IMemoryRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider,
) : IMemoryRepository {

    private val db get() = supabase.client.postgrest

    // ── Memory entries ─────────────────────────────────────────

    override suspend fun saveMemoryEntry(entry: MemoryEntry): Result<MemoryEntry> =
        runCatching {
            db["memory_entries"].insert(
                mapOf(
                    "id"              to entry.id,
                    "conversation_id" to entry.conversationId,
                    "partner_id"      to entry.partnerId,
                    "category"        to entry.category.name.lowercase(),
                    "value"           to entry.value,
                    "citation"        to entry.citation,
                    "message_id"      to entry.messageId,
                    "confidence"      to entry.confidence,
                    "timestamp"       to entry.timestamp,
                )
            )
            entry
        }

    override suspend fun getMemoryEntries(conversationId: String): Result<List<MemoryEntry>> =
        Result.success(emptyList()) // Implement with DB query

    override suspend fun searchMemory(conversationId: String, query: String): Result<List<MemoryEntry>> =
        Result.success(emptyList())

    override suspend fun deleteAllMemory(conversationId: String): Result<Unit> = runCatching {
        db["memory_entries"].delete { filter { eq("conversation_id", conversationId) } }
        Unit
    }

    override suspend fun exportMemory(conversationId: String): Result<String> = runCatching {
        val entries = getMemoryEntries(conversationId).getOrDefault(emptyList())
        Json.encodeToString(entries)
    }

    override suspend fun getMemoryStats(conversationId: String): Result<Map<String, Int>> =
        Result.success(mapOf("entries" to 0))

    // ── Summaries ──────────────────────────────────────────────

    override suspend fun saveSummary(summary: ConversationSummary): Result<ConversationSummary> =
        runCatching {
            db["conversation_summaries"].insert(
                mapOf(
                    "id"              to summary.id,
                    "conversation_id" to summary.conversationId,
                    "summary"         to summary.summary,
                    "from_date"       to summary.fromDate,
                    "to_date"         to summary.toDate,
                )
            )
            summary
        }

    override suspend fun getSummaries(conversationId: String): Result<List<ConversationSummary>> =
        Result.success(emptyList())

    // ── Inconsistencies ────────────────────────────────────────

    override suspend fun saveInconsistency(record: InconsistencyRecord): Result<InconsistencyRecord> =
        runCatching {
            db["inconsistencies"].insert(
                mapOf(
                    "id"                  to record.id,
                    "conversation_id"     to record.conversationId,
                    "statement1"          to record.statement1,
                    "statement2"          to record.statement2,
                    "inconsistency_type"  to record.inconsistencyType.name.lowercase(),
                    "coherence_score"     to record.coherenceScore,
                    "gemini_analysis"     to record.geminiAnalysis,
                )
            )
            record
        }

    override suspend fun getInconsistencies(conversationId: String): Result<List<InconsistencyRecord>> =
        Result.success(emptyList())

    override suspend fun updateInconsistencyReviewed(id: String, reviewed: Boolean): Result<Unit> =
        runCatching {
            db["inconsistencies"].update({ set("is_reviewed", reviewed) }) {
                filter { eq("id", id) }
            }
            Unit
        }

    // ── Flags ─────────────────────────────────────────────────

    override suspend fun saveFlag(flag: RelationalFlag): Result<RelationalFlag> = runCatching {
        db["relational_flags"].insert(
            mapOf(
                "id"              to flag.id,
                "conversation_id" to flag.conversationId,
                "partner_id"      to flag.partnerId,
                "type"            to flag.type.name.lowercase(),
                "label"           to flag.label,
                "description"     to flag.description,
                "severity"        to flag.severity,
                "message_id"      to flag.messageId,
                "citation"        to flag.citation,
            )
        )
        flag
    }

    override suspend fun getFlags(conversationId: String): Result<List<RelationalFlag>> =
        Result.success(emptyList())

    override suspend fun deleteFlag(flagId: String): Result<Unit> = runCatching {
        db["relational_flags"].delete { filter { eq("id", flagId) } }
        Unit
    }

    // ── Daily analysis ─────────────────────────────────────────

    override suspend fun getDailyAnalysis(
        conversationId: String,
        date: String,
    ): Result<DailyAnalysisReport?> = Result.success(null)

    override suspend fun saveDailyAnalysis(report: DailyAnalysisReport): Result<DailyAnalysisReport> =
        runCatching {
            db["daily_analysis_reports"].insert(
                mapOf(
                    "id"                    to report.id,
                    "conversation_id"       to report.conversationId,
                    "date"                  to report.date,
                    "coherence_score"       to report.coherenceScore,
                    "deception_risk_label"  to report.deceptionRiskLabel.name.lowercase(),
                    "deception_risk_estimate" to report.deceptionRiskEstimate,
                )
            )
            report
        }
}
