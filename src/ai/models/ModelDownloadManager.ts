/**
 * ModelDownloadManager — Mise à disposition des 3 modèles IA locaux
 *
 *   - "llm"     : Qwen2.5-1.5B-Instruct, quantifié q4f16 (~1,17 Go)
 *   - "emotion" : CamemBERT français d'émotions            (~109 Mo)
 *   - "whisper" : Whisper Small multilingue                (~320 Mo)
 *
 * Les 3 modèles sont téléchargés au premier lancement (voir `runDownload`),
 * soit directement depuis HuggingFace (llm, whisper — fichiers publics déjà
 * au format ONNX), soit depuis la release GitHub `ai-models-v1` du dépôt
 * `l3ryx9/youme-ai` (emotion — reconversion PyTorch → ONNX maison, il
 * n'existe aucune version ONNX publique de ce modèle).
 *
 * FIX (voir diagnostic du 15/07) : le code pointait auparavant vers
 * `l3ryx9/new-youme/releases/download/ai-models-v1`, un dépôt introuvable
 * publiquement (404) — chaque tentative de téléchargement échouait donc
 * instantanément, avant même de toucher au réseau lent. Le workflow GitHub
 * Actions « 📦 Download & Upload AI Models » qui construit cette release
 * tourne en réalité dans `l3ryx9/youme-ai`, pas `new-youme`.
 *
 * Historique modèle LLM : utilisait auparavant Llama-3.2-1B-Instruct. L'asset
 * `llm-model_q4.onnx` de la release était incomplet (il ne contenait que le
 * graphe ONNX, ~149 Ko ; le fichier de poids externe `model_q4.onnx_data`,
 * ~1,69 Go, n'avait jamais été généré — le workflow ne le téléchargeait pas).
 * Remplacé par Qwen2.5-1.5B-Instruct en quantification q4f16 : contrairement
 * aux quantifications q4/fp16/fp32 de ce modèle, la variante q4f16 est un
 * fichier `.onnx` unique et autonome (pas de `.onnx_data` externe), donc
 * aucun risque d'upload partiel de ce type. Architecture attendue par
 * `LLMService.ts` (num_hidden_layers=28, num_key_value_heads=2, head_dim=128)
 * — c'est bien celle de Qwen2.5-1.5B, pas de Phi-4-mini (3.8B, incompatible).
 *
 * FIX PERF (voir runDownload) : les 3 modèles sont téléchargés en parallèle,
 * pas séquentiellement.
 *
 * Synchronisation barre de progression :
 *   Le manager émet des événements status='retrying' pendant les délais de
 *   backoff afin que l'UI puisse afficher un compte à rebours en temps réel.
 */
import * as FileSystem from 'expo-file-system';
import { installBundledModel, isModelBundled } from '@ai/models/LocalModelInstaller';

export type ModelId = 'llm' | 'emotion' | 'whisper';

interface ModelFileSpec {
  url: string;
  filename: string;
  approxBytes: number;
}

interface ModelSpec {
  files: ModelFileSpec[];
}

const MODELS_DIR = `${FileSystem.documentDirectory}ai-models/`;
const STATE_FILE = `${MODELS_DIR}download-state.json`;

/** Release GitHub hébergeant l'asset Emotion (reconversion maison) */
const R  = 'https://github.com/l3ryx9/youme-ai/releases/download/ai-models-v1';
const HF = 'https://huggingface.co';

export const MODEL_MANIFEST: Record<ModelId, ModelSpec> = {
  llm: {
    files: [
      // Qwen2.5-1.5B-Instruct q4f16 — fichier ONNX unique, sans .onnx_data
      // externe (contrairement aux variantes q4/fp16/fp32 de ce même repo),
      // donc aucun risque de poids manquants au moment de l'upload/download.
      { url: `${HF}/onnx-community/Qwen2.5-1.5B-Instruct/resolve/main/onnx/model_q4f16.onnx`,           filename: 'model.onnx',             approxBytes: 1_220_000_000 },
      { url: `${HF}/onnx-community/Qwen2.5-1.5B-Instruct/resolve/main/tokenizer.json`,                  filename: 'tokenizer.json',          approxBytes: 7_030_000     },
      { url: `${HF}/onnx-community/Qwen2.5-1.5B-Instruct/resolve/main/tokenizer_config.json`,           filename: 'tokenizer_config.json',   approxBytes: 7_310         },
      { url: `${HF}/onnx-community/Qwen2.5-1.5B-Instruct/resolve/main/special_tokens_map.json`,         filename: 'special_tokens_map.json', approxBytes: 613           },
      { url: `${HF}/onnx-community/Qwen2.5-1.5B-Instruct/resolve/main/config.json`,                     filename: 'config.json',             approxBytes: 809           },
      { url: `${HF}/onnx-community/Qwen2.5-1.5B-Instruct/resolve/main/generation_config.json`,          filename: 'generation_config.json',  approxBytes: 242           },
    ],
  },
  emotion: {
    files: [
      // CamemBERT français — ~107 Mo (reconverti + quantifié int8 par le
      // workflow GitHub Actions, aucune version ONNX publique n'existe)
      { url: `${R}/emotion-model.onnx`,            filename: 'model.onnx',             approxBytes: 111_445_232 },
      { url: `${R}/emotion-tokenizer.json`,         filename: 'tokenizer.json',          approxBytes: 2_421_253  },
      { url: `${R}/emotion-tokenizer_config.json`,  filename: 'tokenizer_config.json',   approxBytes: 1_813      },
      { url: `${R}/emotion-special_tokens_map.json`,filename: 'special_tokens_map.json', approxBytes: 1_058      },
      { url: `${R}/emotion-config.json`,            filename: 'config.json',             approxBytes: 965        },
    ],
  },
  whisper: {
    files: [
      // Whisper Small — encoder fp16 (meilleure précision de transcription
      // que la variante quantifiée int8) + decoder quantifié int8
      { url: `${R}/whisper-encoder.onnx`,                                                                filename: 'encoder_model.onnx',          approxBytes: 176_607_756 },
      { url: `${R}/whisper-decoder.onnx`,                                                                filename: 'decoder_model_merged.onnx',   approxBytes: 156_750_845 },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/tokenizer.json`,                          filename: 'tokenizer.json',              approxBytes: 2_400_000   },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/tokenizer_config.json`,                   filename: 'tokenizer_config.json',       approxBytes: 5_000       },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/config.json`,                             filename: 'config.json',                 approxBytes: 2_000       },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/generation_config.json`,                  filename: 'generation_config.json',      approxBytes: 4_000       },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/preprocessor_config.json`,                filename: 'preprocessor_config.json',    approxBytes: 339         },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/special_tokens_map.json`,                 filename: 'special_tokens_map.json',     approxBytes: 2_000       },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/normalizer.json`,                         filename: 'normalizer.json',             approxBytes: 52_000      },
      { url: `${HF}/onnx-community/whisper-small/resolve/main/added_tokens.json`,                       filename: 'added_tokens.json',           approxBytes: 2_000       },
    ],
  },
};

const TOTAL_APPROX_BYTES = Object.values(MODEL_MANIFEST).reduce(
  (sum, model) => sum + model.files.reduce((s, f) => s + f.approxBytes, 0),
  0
);

export interface ModelDownloadProgress {
  modelId: ModelId;
  filename: string;
  fileIndex: number;
  fileCountForModel: number;
  overallProgress: number;
  status: 'downloading' | 'retrying';
  retryAttempt?: number;
  retryMaxAttempts?: number;
  retryDelayMs?: number;
}

type ProgressListener = (progress: ModelDownloadProgress) => void;

interface DownloadState {
  completedModels: ModelId[];
}

const MAX_ATTEMPTS = 7;
const RETRY_DELAYS = [2000, 4000, 8000, 15000, 30000, 60000];
const STALL_TIMEOUT_MS = 20000;
const STALL_CHECK_INTERVAL_MS = 5000;

export class ModelDownloadManager {
  private progressListeners: ProgressListener[] = [];
  private downloadPromise: Promise<void> | null = null;

  onProgress(listener: ProgressListener): () => void {
    this.progressListeners.push(listener);
    return () => {
      this.progressListeners = this.progressListeners.filter((l) => l !== listener);
    };
  }

  private emit(progress: ModelDownloadProgress): void {
    this.progressListeners.forEach((l) => l(progress));
  }

  private getModelDir(modelId: ModelId): string {
    return `${MODELS_DIR}${modelId}/`;
  }

  getFilePath(modelId: ModelId, filename: string): string {
    return `${this.getModelDir(modelId)}${filename}`;
  }

  private async readState(): Promise<DownloadState> {
    try {
      const info = await FileSystem.getInfoAsync(STATE_FILE);
      if (!info.exists) return { completedModels: [] };
      const raw = await FileSystem.readAsStringAsync(STATE_FILE);
      return JSON.parse(raw) as DownloadState;
    } catch {
      return { completedModels: [] };
    }
  }

  private async writeState(state: DownloadState): Promise<void> {
    await FileSystem.makeDirectoryAsync(MODELS_DIR, { intermediates: true }).catch(() => {});
    await FileSystem.writeAsStringAsync(STATE_FILE, JSON.stringify(state));
  }

  async isModelReady(modelId: ModelId): Promise<boolean> {
    const state = await this.readState();
    if (!state.completedModels.includes(modelId)) return false;
    const spec = MODEL_MANIFEST[modelId];
    for (const file of spec.files) {
      const info = await FileSystem.getInfoAsync(this.getFilePath(modelId, file.filename));
      if (!info.exists) return false;
    }
    return true;
  }

  async areAllModelsReady(): Promise<boolean> {
    const results = await Promise.all(
      (Object.keys(MODEL_MANIFEST) as ModelId[]).map((id) => this.isModelReady(id))
    );
    return results.every(Boolean);
  }

  async downloadAllModels(onProgress?: ProgressListener): Promise<void> {
    if (this.downloadPromise) return this.downloadPromise;
    const unsubscribe = onProgress ? this.onProgress(onProgress) : null;
    this.downloadPromise = this.runDownload().finally(() => {
      this.downloadPromise = null;
      unsubscribe?.();
    });
    return this.downloadPromise;
  }

  /**
   * FIX PERF : les 3 modèles sont désormais téléchargés EN PARALLÈLE (au
   * lieu d'un par un, fichier par fichier). Sur une connexion correcte, ça
   * réduit significativement l'attente au premier lancement — la bande
   * passante restait sous-utilisée par le mode séquentiel précédent, et
   * plusieurs connexions concurrentes vers des hôtes différents (HuggingFace
   * pour llm/whisper, GitHub pour emotion) atteignent en pratique un débit
   * agrégé supérieur à une seule connexion à la fois.
   *
   * La progression globale n'a donc plus de notion "avant/après" entre
   * modèles : elle est recalculée à chaque évènement à partir d'une carte
   * partagée `bytesDoneByModel`, mise à jour par chaque téléchargement de
   * modèle indépendamment.
   */
  private async runDownload(): Promise<void> {
    await FileSystem.makeDirectoryAsync(MODELS_DIR, { intermediates: true }).catch(() => {});
    const state = await this.readState();

    const bytesDoneByModel: Record<ModelId, number> = { llm: 0, emotion: 0, whisper: 0 };

    const emitOverall = (
      modelId: ModelId,
      filename: string,
      fileIndex: number,
      fileCountForModel: number,
      status: 'downloading' | 'retrying',
      extra?: { retryAttempt?: number; retryMaxAttempts?: number; retryDelayMs?: number }
    ) => {
      const totalDone = (Object.values(bytesDoneByModel) as number[]).reduce((a, b) => a + b, 0);
      this.emit({
        modelId,
        filename,
        fileIndex,
        fileCountForModel,
        overallProgress: Math.min(totalDone / TOTAL_APPROX_BYTES, status === 'downloading' ? 1 : 0.999),
        status,
        ...extra,
      });
    };

    // Écritures d'état sérialisées : deux modèles peuvent finir quasi en
    // même temps, `writeState` est un read-modify-write sur un fichier JSON
    // partagé — sans sérialisation, l'écriture la plus lente pourrait
    // écraser celle d'un autre modèle terminé entre-temps. `state` reste un
    // objet partagé muté de façon synchrone avant chaque écriture, donc même
    // dans le pire cas la version finalement persistée reflète toujours les
    // deux modèles (voir markModelComplete) ; ce verrou évite simplement les
    // écritures concurrentes inutiles.
    let stateWriteQueue: Promise<void> = Promise.resolve();
    const markModelComplete = (modelId: ModelId): Promise<void> => {
      state.completedModels = Array.from(new Set([...state.completedModels, modelId]));
      stateWriteQueue = stateWriteQueue.then(() => this.writeState(state));
      return stateWriteQueue;
    };

    const downloadOneModel = async (modelId: ModelId): Promise<void> => {
      const spec = MODEL_MANIFEST[modelId];
      const modelTotalBytes = spec.files.reduce((s, f) => s + f.approxBytes, 0);

      if (await this.isModelReady(modelId)) {
        bytesDoneByModel[modelId] = modelTotalBytes;
        emitOverall(modelId, '', spec.files.length, spec.files.length, 'downloading');
        return;
      }

      const modelDir = this.getModelDir(modelId);
      await FileSystem.makeDirectoryAsync(modelDir, { intermediates: true }).catch(() => {});

      if (isModelBundled(modelId)) {
        // Modèle livré dans le bundle natif (assets/ai-models/) : simple
        // copie locale, aucun accès réseau. Si l'installation échoue
        // (assets absents ou corrompus dans le build), on bascule sur le
        // téléchargement réseau ci-dessous.
        try {
          await installBundledModel(modelId, (id) => this.getModelDir(id));
          bytesDoneByModel[modelId] = modelTotalBytes;
          await markModelComplete(modelId);
          emitOverall(modelId, '', spec.files.length, spec.files.length, 'downloading');
          return;
        } catch (bundleErr) {
          console.warn(
            `[ModelDownloadManager] Installation embarquée échouée pour "${modelId}", ` +
            `bascule sur téléchargement réseau :`,
            bundleErr
          );
          // Nettoyage du dossier partiellement copié avant de retenter en réseau.
          await FileSystem.deleteAsync(this.getModelDir(modelId), { idempotent: true });
          await FileSystem.makeDirectoryAsync(this.getModelDir(modelId), { intermediates: true }).catch(() => {});
        }
      }

      let bytesDoneInModel = 0;
      for (let i = 0; i < spec.files.length; i++) {
        const file = spec.files[i];
        const destPath = this.getFilePath(modelId, file.filename);

        const existing = await FileSystem.getInfoAsync(destPath);
        if (!existing.exists) {
          await this.downloadFileWithRetry(
            file.url,
            destPath,
            (bytesWritten, expectedBytes) => {
              const fileSize = (expectedBytes && expectedBytes > 0) ? expectedBytes : file.approxBytes;
              const normalizedBytes = Math.min(bytesWritten, fileSize) * (file.approxBytes / fileSize);
              bytesDoneByModel[modelId] = bytesDoneInModel + normalizedBytes;
              emitOverall(modelId, file.filename, i, spec.files.length, 'downloading');
            },
            (attempt, maxAttempts, delayMs) => {
              emitOverall(modelId, file.filename, i, spec.files.length, 'retrying', {
                retryAttempt: attempt,
                retryMaxAttempts: maxAttempts,
                retryDelayMs: delayMs,
              });
            }
          );
        }

        bytesDoneInModel += file.approxBytes;
        bytesDoneByModel[modelId] = bytesDoneInModel;
      }

      await markModelComplete(modelId);
      emitOverall(modelId, '', spec.files.length, spec.files.length, 'downloading');
    };

    const modelIds = Object.keys(MODEL_MANIFEST) as ModelId[];
    const results = await Promise.allSettled(modelIds.map(downloadOneModel));

    const firstFailure = results.find(
      (r): r is PromiseRejectedResult => r.status === 'rejected'
    );
    if (firstFailure) {
      // Les modèles qui ont réussi restent téléchargés sur disque (fichiers
      // déjà écrits + state.json à jour pour eux) — un retry ne les
      // re-télécharge pas, seul celui qui a échoué sera retenté.
      throw firstFailure.reason;
    }
  }

  private async downloadFileWithRetry(
    url: string,
    destPath: string,
    onBytes: (bytesWritten: number, expectedBytes?: number) => void,
    onRetry: (attempt: number, maxAttempts: number, delayMs: number) => void
  ): Promise<void> {
    const tmpPath = `${destPath}.part`;
    let lastError: unknown;
    let resumeData: string | undefined;

    for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      let resumable: FileSystem.DownloadResumable | undefined;
      let stallIntervalId: ReturnType<typeof setInterval> | undefined;

      try {
        let lastProgressAt = Date.now();

        const progressCallback = (data: {
          totalBytesWritten: number;
          totalBytesExpectedToWrite: number;
        }) => {
          lastProgressAt = Date.now();
          onBytes(data.totalBytesWritten, data.totalBytesExpectedToWrite);
        };

        if (resumeData) {
          resumable = FileSystem.createDownloadResumable(
            url, tmpPath, {}, progressCallback, resumeData
          );
        } else {
          await FileSystem.deleteAsync(tmpPath, { idempotent: true });
          resumable = FileSystem.createDownloadResumable(
            url, tmpPath, {}, progressCallback
          );
        }

        const stallWatchdog = new Promise<never>((_, reject) => {
          stallIntervalId = setInterval(() => {
            if (Date.now() - lastProgressAt > STALL_TIMEOUT_MS) {
              reject(new Error(
                `Téléchargement bloqué depuis ${STALL_TIMEOUT_MS / 1000}s pour ${url}`
              ));
            }
          }, STALL_CHECK_INTERVAL_MS);
        });

        const result = await Promise.race([resumable.downloadAsync(), stallWatchdog]);

        if (!result || result.status !== 200) {
          throw new Error(`Status ${result?.status ?? 'inconnu'} pour ${url}`);
        }

        await FileSystem.moveAsync({ from: tmpPath, to: destPath });
        return;

      } catch (error) {
        lastError = error;
        console.warn(`[ModelDownloadManager] Tentative ${attempt}/${MAX_ATTEMPTS} échouée:`, error);

        if (resumable) {
          try {
            const pauseState = await resumable.pauseAsync();
            resumeData = pauseState.resumeData;
          } catch {
            try { resumeData = resumable.savable().resumeData; } catch { resumeData = undefined; }
          }
        }

        if (attempt < MAX_ATTEMPTS) {
          const delay = RETRY_DELAYS[attempt - 1] ?? 60000;
          console.log(`[ModelDownloadManager] Reconnexion dans ${delay / 1000}s…`);

          // Émettre une mise à jour par seconde pour le compte à rebours UI
          const tickMs = 1000;
          let elapsed = 0;
          while (elapsed < delay) {
            onRetry(attempt, MAX_ATTEMPTS, delay - elapsed);
            await new Promise((r) => setTimeout(r, Math.min(tickMs, delay - elapsed)));
            elapsed += tickMs;
          }
        }

      } finally {
        if (stallIntervalId) clearInterval(stallIntervalId);
      }
    }

    await FileSystem.deleteAsync(tmpPath, { idempotent: true });
    throw lastError instanceof Error
      ? lastError
      : new Error(`Échec du téléchargement de ${url}`);
  }

  async deleteAllModels(): Promise<void> {
    await FileSystem.deleteAsync(MODELS_DIR, { idempotent: true });
  }

  getTotalApproxBytes(): number {
    return TOTAL_APPROX_BYTES;
  }
}

export const modelDownloadManager = new ModelDownloadManager();
