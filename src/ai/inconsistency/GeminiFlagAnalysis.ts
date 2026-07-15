/**
 * Module Gemini — Analyse Red Flags / Green Flags MESSAGE PAR MESSAGE
 *
 * Analyse chaque message individuellement (un appel Gemini par message)
 * plutôt qu'en envoyant toute la conversation en une seule requête.
 *
 * Avantages :
 * - Résultats plus précis et contextualisés par message
 * - Pas de limite de longueur de contexte Gemini
 * - Les flags sont tracés précisément jusqu'au message source
 *
 * Limite de débit : 1 appel toutes les 4 secondes (plan gratuit = 15 req/min).
 * Les messages trop courts (< 10 car.) sont ignorés silencieusement.
 *
 * FIX SÉCURITÉ : l'appel Gemini passe désormais par l'Edge Function
 * `gemini-proxy` (clé API et modèle définis côté serveur) au lieu d'un
 * fetch direct avec une clé embarquée dans le bundle client. Voir
 * GeminiProxyService.ts et supabase/functions/gemini-proxy/index.ts.
 */
import type { FlagAnalysisResult, RelationshipFlag, FlagSeverity } from '@domain/entities/Memory';
import type { Message } from '@domain/entities/Message';
import { geminiProxyService } from '@infrastructure/supabase/GeminiProxyService';
import { useAuthStore } from '@presentation/stores/authStore';

/** Longueur minimale d'un message pour déclencher l'analyse */
const MIN_TEXT_LENGTH = 10;

/** Délai entre deux appels Gemini (ms) — respecte 15 req/min */
const CALL_DELAY_MS = 4_000;

/** Nombre max de messages analysés par session (pour éviter une attente trop longue) */
const MAX_MESSAGES_PER_SESSION = 40;

/** Résultat brut de l'analyse d'un seul message */
interface SingleMessageFlagResult {
  type: 'red' | 'green' | 'none';
  category: string;
  severity: FlagSeverity;
  citation: string;
  explanation: string;
  confidence: number;
}

export class GeminiFlagAnalysisModule {
  isAvailable(): boolean {
    // La clé vit désormais côté serveur — la seule condition côté client
    // est d'avoir une session active (l'Edge Function exige un JWT valide).
    return useAuthStore.getState().isAuthenticated;
  }

  /**
   * Analyse les red/green flags d'une conversation, message par message.
   *
   * @param messages      tous les messages de la conversation
   * @param currentUserId id de l'utilisateur courant (pour étiqueter "Vous")
   * @param partnerName   nom affiché du partenaire
   * @returns             résultat structuré, ou null si Gemini indisponible
   */
  async analyzeFlags(
    messages: Message[],
    currentUserId: string,
    partnerName: string
  ): Promise<FlagAnalysisResult | null> {
    if (!this.isAvailable()) {
      console.warn('[GeminiFlags] Clé API non configurée. Module désactivé.');
      return null;
    }

    // Filtrer les messages exploitables et limiter à MAX_MESSAGES_PER_SESSION
    const usable = messages
      .filter((m) => !m.isDeleted && this.extractText(m).trim().length >= MIN_TEXT_LENGTH)
      .slice(-MAX_MESSAGES_PER_SESSION);

    if (usable.length < 2) {
      return null; // Trop peu de messages pour une analyse pertinente
    }

    const redFlags: RelationshipFlag[] = [];
    const greenFlags: RelationshipFlag[] = [];

    console.log(`[GeminiFlags] Analyse de ${usable.length} messages un par un...`);

    // ── Analyse message par message ──────────────────────────────────────────
    for (let i = 0; i < usable.length; i++) {
      const msg = usable[i];
      const text = this.extractText(msg).trim();
      const sender = msg.senderId === currentUserId ? 'Vous' : partnerName;

      try {
        const result = await this.analyzeOneMessage(text, sender, partnerName);

        if (result && result.type !== 'none') {
          const flag: RelationshipFlag = {
            type: result.type,
            category: result.category,
            severity: result.severity,
            citation: result.citation || text.substring(0, 120),
            sender,
            explanation: result.explanation,
            confidence: Math.min(1, Math.max(0, result.confidence)),
          };

          if (result.type === 'red') {
            redFlags.push(flag);
          } else {
            greenFlags.push(flag);
          }
        }
      } catch (error) {
        console.warn(`[GeminiFlags] Erreur message ${i + 1}/${usable.length} :`, error);
        // On continue avec le message suivant
      }

      // Respecter le débit Gemini (sauf pour le dernier message)
      if (i < usable.length - 1) {
        await this.sleep(CALL_DELAY_MS);
      }
    }

    return this.buildResult(redFlags, greenFlags, usable.length);
  }

  // ── Analyse d'un seul message ────────────────────────────────────────────────

  private async analyzeOneMessage(
    text: string,
    sender: string,
    partnerName: string
  ): Promise<SingleMessageFlagResult | null> {
    const prompt = `Tu es un système d'analyse de la santé relationnelle. Analyse CE SEUL MESSAGE et détecte s'il contient un RED FLAG, un GREEN FLAG, ou aucun signal clair.

Expéditeur : ${sender}
Message : "${text}"

DÉFINITIONS :
- RED FLAG : manque de respect, dévalorisation, contrôle, jalousie excessive, manipulation, culpabilisation, pression, mensonge apparent, mépris, isolement, non-respect des limites.
- GREEN FLAG : respect, écoute, soutien, encouragement, honnêteté, excuses sincères, réciprocité, respect des limites, communication ouverte, gestion saine des désaccords.

RÈGLES STRICTES :
1. Un signal doit être CLAIREMENT présent dans le message — pas implicite ni supposé.
2. Cite l'extrait EXACT du message qui constitue le signal.
3. Langage neutre et probabiliste — jamais d'accusation.
4. Si aucun signal clair → type: "none".
5. Réponds UNIQUEMENT en JSON valide, sans texte autour.

JSON attendu :
{
  "type": "red" | "green" | "none",
  "category": "Respect|Soutien|Écoute|Honnêteté|Encouragement|Réciprocité|Manipulation|Contrôle|Dévalorisation|Culpabilisation|Pression|Mépris|Autre",
  "severity": "faible" | "modéré" | "élevé",
  "citation": "extrait exact du message (vide si type=none)",
  "explanation": "explication en 1 phrase (vide si type=none)",
  "confidence": 0.0
}`;

    const data = await geminiProxyService.generateContent({
      prompt,
      generationConfig: {
        temperature: 0.1,
        topK: 20,
        topP: 0.9,
        maxOutputTokens: 256,
      },
      safetySettings: [
        { category: 'HARM_CATEGORY_HARASSMENT',  threshold: 'BLOCK_MEDIUM_AND_ABOVE' },
        { category: 'HARM_CATEGORY_HATE_SPEECH', threshold: 'BLOCK_MEDIUM_AND_ABOVE' },
      ],
    });

    if (!data) return null;

    const raw = data?.candidates?.[0]?.content?.parts?.[0]?.text ?? '';

    const jsonMatch =
      raw.match(/```json\n?([\s\S]*?)\n?```/) ?? raw.match(/\{[\s\S]*\}/);
    if (!jsonMatch) return null;

    try {
      const parsed = JSON.parse(jsonMatch[1] ?? jsonMatch[0]);
      const allowedSeverities: FlagSeverity[] = ['faible', 'modéré', 'élevé'];
      return {
        type:        ['red', 'green', 'none'].includes(parsed.type) ? parsed.type : 'none',
        category:    String(parsed.category ?? 'Autre'),
        severity:    allowedSeverities.includes(parsed.severity) ? parsed.severity : 'modéré',
        citation:    String(parsed.citation ?? '').trim(),
        explanation: String(parsed.explanation ?? ''),
        confidence:  Math.min(1, Math.max(0, Number(parsed.confidence ?? 0.5))),
      };
    } catch {
      return null;
    }
  }

  // ── Construction du résultat final ──────────────────────────────────────────

  private buildResult(
    redFlags: RelationshipFlag[],
    greenFlags: RelationshipFlag[],
    messageCount: number
  ): FlagAnalysisResult {
    const totalFlags = redFlags.length + greenFlags.length;

    // Score d'équilibre : 50 = neutre, > 50 = plutôt positif, < 50 = préoccupant
    let balanceScore = 50;
    if (totalFlags > 0) {
      balanceScore = Math.round((greenFlags.length / totalFlags) * 100);
    }

    // Calcul de la pondération par gravité
    const redWeight = this.computeWeight(redFlags);
    const greenWeight = this.computeWeight(greenFlags);
    if (redWeight + greenWeight > 0) {
      balanceScore = Math.round((greenWeight / (redWeight + greenWeight)) * 100);
    }

    const climateLabel = this.computeClimateLabel(balanceScore, redFlags);

    const facts = [
      ...redFlags.map((f) => `[RED] ${f.sender ?? ''} : "${f.citation}"`),
      ...greenFlags.map((f) => `[GREEN] ${f.sender ?? ''} : "${f.citation}"`),
    ].filter(Boolean);

    const summary = this.buildSummary(redFlags, greenFlags, climateLabel);

    return {
      redFlags,
      greenFlags,
      balanceScore,
      climateLabel,
      summary,
      facts,
      interpretations: [
        'Chaque signal est analysé indépendamment — le contexte global peut nuancer ces observations.',
        'Ces résultats sont probabilistes et ne constituent pas une certitude.',
      ],
      messageCount,
      analyzedAt: new Date(),
    };
  }

  /** Pondère les flags par gravité (élevé=3, modéré=2, faible=1). */
  private computeWeight(flags: RelationshipFlag[]): number {
    return flags.reduce((sum, f) => {
      const w = f.severity === 'élevé' ? 3 : f.severity === 'modéré' ? 2 : 1;
      return sum + w * f.confidence;
    }, 0);
  }

  private computeClimateLabel(
    score: number,
    redFlags: RelationshipFlag[]
  ): FlagAnalysisResult['climateLabel'] {
    const hasHighRed = redFlags.some((f) => f.severity === 'élevé' && f.confidence > 0.7);
    if (hasHighRed || score < 30) return 'préoccupant';
    if (score < 45) return 'à surveiller';
    if (score < 65) return 'globalement sain';
    return 'sain';
  }

  private buildSummary(
    redFlags: RelationshipFlag[],
    greenFlags: RelationshipFlag[],
    label: string
  ): string {
    if (redFlags.length === 0 && greenFlags.length === 0) {
      return 'Aucun signal relationnel notable détecté dans les messages analysés.';
    }
    const parts: string[] = [];
    if (greenFlags.length > 0) {
      parts.push(`${greenFlags.length} signal${greenFlags.length > 1 ? 'aux' : ''} positif${greenFlags.length > 1 ? 's' : ''} détecté${greenFlags.length > 1 ? 's' : ''}`);
    }
    if (redFlags.length > 0) {
      parts.push(`${redFlags.length} signal${redFlags.length > 1 ? 'aux' : ''} d'alerte détecté${redFlags.length > 1 ? 's' : ''}`);
    }
    return `${parts.join(', ')}. Climat relationnel : ${label}.`;
  }

  private extractText(m: Message): string {
    if (m.type === 'voice') return m.aiAnalysis?.transcription ?? '';
    return m.content ?? '';
  }

  private sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}

export const geminiFlagModule = new GeminiFlagAnalysisModule();
