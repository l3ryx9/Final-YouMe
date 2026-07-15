/**
 * LocalModelInstaller — Installe les modèles IA embarqués (bundle natif)
 * vers le même dossier que celui utilisé par `ModelDownloadManager`
 * (`${FileSystem.documentDirectory}ai-models/<modelId>/`), sans passer par
 * le réseau.
 *
 * Les fichiers binaires (.onnx) sont packagés par Metro/Expo comme des
 * "assets" natifs : on les résout via `expo-asset` puis on les copie du
 * cache d'assets vers le dossier attendu. Les fichiers JSON sont déjà
 * disponibles en mémoire (require() les a parsés) : on les réécrit tels
 * quels sur disque.
 */
import * as FileSystem from 'expo-file-system';
import { Asset } from 'expo-asset';
import { BUNDLED_MODELS, type BundledAssetFile } from '@ai/models/bundledModels';
import type { ModelId } from '@ai/models/ModelDownloadManager';

export function isModelBundled(modelId: ModelId): boolean {
  return Boolean(BUNDLED_MODELS[modelId]?.length);
}

async function installFile(modelDir: string, file: BundledAssetFile): Promise<void> {
  const destPath = `${modelDir}${file.filename}`;
  const existing = await FileSystem.getInfoAsync(destPath);
  if (existing.exists) return;

  if (file.kind === 'json') {
    await FileSystem.writeAsStringAsync(destPath, JSON.stringify(file.content));
    return;
  }

  const asset = Asset.fromModule(file.module);
  await asset.downloadAsync();
  const localUri = asset.localUri ?? asset.uri;
  if (!localUri) {
    throw new Error(`[LocalModelInstaller] Asset embarqué introuvable pour ${file.filename}`);
  }
  await FileSystem.copyAsync({ from: localUri, to: destPath });
}

/**
 * Copie tous les fichiers embarqués d'un modèle vers son dossier de
 * destination. Idempotent : les fichiers déjà présents ne sont pas
 * re-copiés. Ne fait rien si le modèle n'est pas dans `BUNDLED_MODELS`.
 */
export async function installBundledModel(
  modelId: ModelId,
  getModelDir: (modelId: ModelId) => string
): Promise<boolean> {
  const files = BUNDLED_MODELS[modelId];
  if (!files || files.length === 0) return false;

  const modelDir = getModelDir(modelId);
  await FileSystem.makeDirectoryAsync(modelDir, { intermediates: true }).catch(() => {});

  for (const file of files) {
    await installFile(modelDir, file);
  }
  return true;
}
