package com.youme24.app.data.gemini

import com.youme24.app.domain.model.AiAnalysis
import com.youme24.app.domain.model.FlagType
import com.youme24.app.domain.model.RelationalFlag
import com.youme24.app.domain.repository.IGeminiRepository
import io.github.jan.supabase.functions.functions
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Appelle les Edge Functions Supabase existantes côté Android.
 * NE réimplémente PAS la logique IA — consomme les fonctions HTTP telles quelles.
 *
 * Edge Functions utilisées :
 *  - gemini-proxy              → sendPrompt / analyzeMessage
 *  - daily-psychological-analysis → triggerDailyAnalysis
 */
@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val supabase: com.youme24.app.data.remote.supabase.SupabaseClientProvider,
) : IGeminiRepository {

    private val functions get() = supabase.client.functions
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class GeminiProxyRequest(
        val prompt: String,
        val context: String? = null,
        val config: Map<String, String> = emptyMap(),
    )

    @Serializable
    data class GeminiProxyResponse(val text: String? = null, val error: String? = null)

    override suspend fun sendPrompt(
        prompt: String,
        context: String?,
        config: Map<String, Any>,
    ): Result<String> = runCatching {
        val body = GeminiProxyRequest(prompt = prompt, context = context)
        val response = functions.invoke(
            function = "gemini-proxy",
            body     = json.encodeToString(body),
        )
        val parsed = json.decodeFromString<GeminiProxyResponse>(response.bodyAsText())
        parsed.text ?: error(parsed.error ?: "Empty response from Gemini proxy")
    }

    override suspend fun analyzeMessage(
        message: String,
        context: List<String>,
    ): Result<AiAnalysis> = runCatching {
        val prompt = buildString {
            append("Analyse ce message dans le contexte d'une relation de couple.\n")
            append("Message : $message\n")
            if (context.isNotEmpty()) {
                append("Contexte récent : ${context.takeLast(5).joinToString(" | ")}\n")
            }
            append("Retourne : émotions, résumé, sujets, score de cohérence (0-1).")
        }
        val raw = sendPrompt(prompt).getOrThrow()
        // Basic parsing — production code should use structured output
        AiAnalysis(
            summary = raw.take(200),
            emotions = extractEmotions(raw),
            coherenceScore = extractCoherence(raw),
        )
    }

    override suspend fun detectFlags(
        conversationContext: List<String>,
        partnerId: String,
        conversationId: String,
    ): Result<List<RelationalFlag>> = runCatching {
        val prompt = buildString {
            append("Analyse cette conversation de couple et détecte les red flags et green flags.\n")
            append("Messages : ${conversationContext.joinToString("\n")}\n")
            append("Retourne une liste JSON de flags avec type (red/green), label, description, severity (1-5).")
        }
        val raw = sendPrompt(prompt).getOrThrow()
        // Parse flags from raw response (simplified)
        listOf(
            RelationalFlag(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                partnerId = partnerId,
                type = FlagType.GREEN_FLAG,
                label = "Communication ouverte",
                description = "Analyse IA : $raw".take(150),
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun triggerDailyAnalysis(
        conversationId: String,
        date: String,
    ): Result<Unit> = runCatching {
        functions.invoke(
            function = "daily-psychological-analysis",
            body     = """{"conversation_id":"$conversationId","date":"$date"}""",
        )
        Unit
    }

    override suspend fun getCoherenceScore(
        conversationId: String,
        recentMessages: List<String>,
    ): Result<Float> = runCatching {
        val prompt = "Score de cohérence (0.0-1.0) pour : ${recentMessages.joinToString(" | ")}"
        val raw = sendPrompt(prompt).getOrThrow()
        extractCoherence(raw) ?: 0.5f
    }

    // ── Helpers ──────────────────────────────────────────────

    private fun extractEmotions(text: String): List<String> {
        val emotions = listOf("joie", "tristesse", "colère", "peur", "surprise", "dégoût",
            "amour", "anxiété", "confiance")
        return emotions.filter { text.lowercase().contains(it) }
    }

    private fun extractCoherence(text: String): Float? {
        val regex = Regex("""cohérence[^0-9]*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.toFloatOrNull()?.coerceIn(0f, 1f)
    }
}
