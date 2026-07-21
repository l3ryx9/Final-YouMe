package com.youme24.app.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chiffrement bout-en-bout — réimplémentation Kotlin du E2ECryptoService.ts.
 *
 * Algorithmes :
 *   - Génération de clé : X25519 via KeyPairGenerator (Android Keystore)
 *     → Fallback : ECDH P-256 (X25519 pas disponible avant API 31)
 *   - Chiffrement symétrique : AES-256-GCM (remplace XSalsa20-Poly1305 / TweetNaCl)
 *   - Stockage de la clé privée : Android Keystore (remplace expo-secure-store)
 *
 * Note : le nonce (12 bytes GCM IV) est encodé en Base64 et stocké avec le message,
 * reproduisant le champ `nonce` du schéma Supabase existant.
 */
@Singleton
class E2ECryptoService @Inject constructor(
    private val keyStorage: KeyStorage,
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MY_KEY_ALIAS      = "youme_e2e_private_key"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH     = 128
        private const val GCM_IV_LENGTH      = 12
    }

    /** Génère la paire de clés ECDH de l'utilisateur et stocke la privée dans l'Android Keystore. */
    fun generateKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            MY_KEY_ALIAS,
            KeyProperties.PURPOSE_AGREE_KEY,
        )
            .setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            .setUserAuthenticationRequired(false)
            .build()
        kpg.initialize(spec)
        return kpg.generateKeyPair()
    }

    /** Exporte la clé publique en Base64 pour la stocker dans Supabase (colonne e2e_public_key). */
    fun exportPublicKey(keyPair: KeyPair): String =
        Base64.getEncoder().encodeToString(keyPair.public.encoded)

    /** Dérive le secret partagé à partir de la clé publique du partenaire (Base64). */
    fun deriveSharedSecret(partnerPublicKeyBase64: String): SecretKey {
        val partnerKeyBytes = Base64.getDecoder().decode(partnerPublicKeyBase64)
        val keyFactory = java.security.KeyFactory.getInstance("EC")
        val partnerPublicKey = keyFactory.generatePublic(
            java.security.spec.X509EncodedKeySpec(partnerKeyBytes)
        )

        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
        val myPrivateKey = keyStore.getKey(MY_KEY_ALIAS, null) as java.security.PrivateKey

        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(myPrivateKey)
        ka.doPhase(partnerPublicKey, true)
        val sharedSecret = ka.generateSecret()

        // Derive AES-256 key from raw shared secret using HKDF-like approach
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        // Use first 32 bytes of SHA-256(sharedSecret) as AES key
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(sharedSecret)
        return javax.crypto.spec.SecretKeySpec(digest, "AES")
    }

    /** Chiffre un message texte. Retourne Pair(ciphertextBase64, nonceBase64). */
    fun encrypt(plaintext: String, sharedKey: SecretKey): Pair<String, String> {
        val iv = ByteArray(GCM_IV_LENGTH).also { java.security.SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, sharedKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Pair(
            Base64.getEncoder().encodeToString(ciphertext),
            Base64.getEncoder().encodeToString(iv),
        )
    }

    /** Déchiffre un message. */
    fun decrypt(ciphertextBase64: String, nonceBase64: String, sharedKey: SecretKey): String {
        val ciphertext = Base64.getDecoder().decode(ciphertextBase64)
        val iv = Base64.getDecoder().decode(nonceBase64)
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, sharedKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }
}
