package com.youme24.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Domaine : entités Mémoire IA (résumés, incohérences, insights).
 * Équivalent de src/domain/Memory.ts (React Native).
 */

// ── Entrée mémoire individuelle ───────────────────────────────
@Serializable
data class MemoryEntry(
    val id: String,
    val conversationId: String,
    val partnerId: String,
    val category: MemoryCategory,
    val value: String,
    val citation: String? = null,
    val messageId: String? = null,
    val confidence: Float = 1.0f,       // 0-1
    val timestamp: Long = 0L,
    val createdAt: Long = 0L,
)

enum class MemoryCategory {
    TOPIC, PERSON, PREFERENCE, EVENT, EMOTION, FACT, OTHER
}

// ── Résumé de conversation ────────────────────────────────────
@Serializable
data class ConversationSummary(
    val id: String,
    val conversationId: String,
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val emotions: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val fromDate: Long = 0L,
    val toDate: Long = 0L,
    val createdAt: Long = 0L,
)

// ── Incohérence détectée ──────────────────────────────────────
@Serializable
data class InconsistencyRecord(
    val id: String,
    val conversationId: String,
    val statement1: String,
    val statement2: String,
    val citation1: String? = null,
    val citation2: String? = null,
    val inconsistencyType: InconsistencyType,
    val coherenceScore: Float,          // 0-1
    val geminiAnalysis: String? = null,
    val isReviewed: Boolean = false,
    val createdAt: Long = 0L,
)

enum class InconsistencyType { CONTRADICTION, FACTUAL, TEMPORAL, OTHER }

// ── Rapport d'analyse quotidienne ────────────────────────────
@Serializable
data class DailyAnalysisReport(
    val id: String,
    val conversationId: String,
    val date: String,                   // "YYYY-MM-DD"
    val contradictions: List<InconsistencyRecord> = emptyList(),
    val coherenceScore: Float,          // 0-1
    val emotionalJourney: List<String> = emptyList(),
    val deceptionRiskEstimate: Float,   // 0-1
    val deceptionRiskLabel: DeceptionRiskLabel,
    val createdAt: Long = 0L,
)

enum class DeceptionRiskLabel { LOW, MODERATE, HIGH }

// ── Flag relationnel ─────────────────────────────────────────
@Serializable
data class RelationalFlag(
    val id: String,
    val conversationId: String,
    val partnerId: String,
    val type: FlagType,
    val label: String,
    val description: String,
    val messageId: String? = null,
    val citation: String? = null,
    val severity: Int = 1,              // 1-5
    val geminiAnalysis: String? = null,
    val createdAt: Long = 0L,
)

enum class FlagType { RED_FLAG, GREEN_FLAG }
