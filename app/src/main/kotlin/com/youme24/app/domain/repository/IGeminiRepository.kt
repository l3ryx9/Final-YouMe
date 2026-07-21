package com.youme24.app.domain.repository

import com.youme24.app.domain.model.AiAnalysis
import com.youme24.app.domain.model.RelationalFlag

/**
 * Interface domaine — IA Gemini via Edge Functions Supabase.
 * Ne réimplémente PAS la logique IA côté client ; appelle les fonctions HTTP.
 */
interface IGeminiRepository {
    /** Envoie un prompt au proxy Gemini (Edge Function gemini-proxy). */
    suspend fun sendPrompt(
        prompt: String,
        context: String? = null,
        config: Map<String, Any> = emptyMap(),
    ): Result<String>

    /** Analyse un message ou une conversation pour en extraire des insights. */
    suspend fun analyzeMessage(
        message: String,
        context: List<String> = emptyList(),
    ): Result<AiAnalysis>

    /** Détecte les red flags / green flags dans un contexte de conversation. */
    suspend fun detectFlags(
        conversationContext: List<String>,
        partnerId: String,
        conversationId: String,
    ): Result<List<RelationalFlag>>

    /** Déclenche l'Edge Function d'analyse psychologique quotidienne. */
    suspend fun triggerDailyAnalysis(
        conversationId: String,
        date: String,
    ): Result<Unit>

    /** Obtient le score de cohérence en temps réel. */
    suspend fun getCoherenceScore(
        conversationId: String,
        recentMessages: List<String>,
    ): Result<Float>
}
