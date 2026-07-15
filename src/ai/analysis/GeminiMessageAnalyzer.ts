/**
 * Module Gemini — Analyse par message pour le compteur de green flags
 *
 * Appelé pour CHAQUE message (texte ou vocal transcrit) entrant dans
 * le pipeline IA. Détecte si le message contient un green flag relationnel
 * et incrémente le compteur journalier dans SQLite.
 *
 * Limite de débit : 1 appel toutes les 3 secondes max (plan gratuit Gemini
 * = 15 req/min). Les messages trop courts ou trop proches du précédent
 * sont ignorés silencieusement.
 *
 * FIX SÉCURITÉ : l'appel Gemini passe désormais par l'Edge Function
 * `gemini-proxy` (clé API et modèle définis côté serveur) au lieu d'un
 * fetch direct avec une clé embarquée dans le bundle client. Voir
 * GeminiProxyService.ts et supabase/functions/gemini-proxy/index.ts.
 */
import type { Message } from '@domain/entities/Message';
import type { MessageGreenFlag } from '@domain/entities/Memory';
import { memoryRepository } from '@infrastructure/storage/LocalMemoryRepository';
import { geminiProxyService } from '@infrastructure/supabase/GeminiProxyService';
import { useAuthStore } from '@presentation/stores/authStore';

/** Longueur minimale du texte pour déclencher l'analyse (évite les "ok", "👍"…) */
const MIN_TEXT_LENGTH = 15;

/** Délai minimum entre deux appels Gemini (ms) — respecte la limite 15 req/min */
const MIN_CALL_INTERVAL_MS = 4_000;

export class GeminiMessageAnalyzer {
  private lastCallAt = 0;

  isAvailable(): boolean {
    // La clé vit désormais côté serveur — la seule condition côté client
    // est d'avoir une session active (l'Edge Function exige
