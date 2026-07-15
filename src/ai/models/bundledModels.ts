/**
 * bundledModels — Manifeste des modèles IA embarqués dans le bundle natif
 *
 * Les modèles sont trop volumineux pour être embarqués directement dans le
 * binaire de l'app (~1.5 Go au total). Ils sont téléchargés au premier
 * lancement via `ModelDownloadManager` depuis HuggingFace / GitHub Releases,
 * puis stockés localement sur l'appareil.
 *
 * Ce fichier reste en place pour compatibilité avec `LocalModelInstaller` et
 * `isModelBundled()`. BUNDLED_MODELS est vide → tous les modèles passent par
 * le téléchargement réseau.
 */
import type { ModelId } from '@ai/models/ModelDownloadManager';

export type BundledAssetFile =
  | { kind: 'binary'; filename: string; module: number }
  | { kind: 'json'; filename: string; content: unknown };

export const BUNDLED_MODELS: Partial<Record<ModelId, BundledAssetFile[]>> = {};
