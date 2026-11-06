package com.virologia.app.crypto

object CryptoEngine {

    fun encryptDiagnosis(
        plainDiagnosis: String,
        secretKey: String,
        algorithm: CryptoAlgorithm,
        epidemiologist: String
    ): EncryptionEnvelope {
        val cipherBytes = when (algorithm) {
            CryptoAlgorithm.AES_128 -> AesCipher.encrypt(plainDiagnosis, secretKey)
            CryptoAlgorithm.RSA_ASYMMETRIC -> RsaCipher.encrypt(plainDiagnosis, secretKey)
            CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX -> LinearAlgebraMatrixCipher.encrypt(plainDiagnosis, secretKey)
        }
        val hex = CryptoEncodingUtils.bytesToHex(cipherBytes)
        val b64 = CryptoEncodingUtils.bytesToBase64(cipherBytes)
        // Firma digital RSA de autenticidad sobre el criptograma y el epidemiólogo autorizante
        val signatureHex = RsaCipher.signRecord("$epidemiologist|$hex", secretKey)

        return EncryptionEnvelope(
            cipherTextHex = hex,
            cipherTextBase64 = b64,
            signatureHex = signatureHex,
            algorithm = algorithm
        )
    }

    fun decryptDiagnosis(
        cipherTextHex: String,
        secretKey: String,
        algorithm: CryptoAlgorithm
    ): Result<String> {
        return runCatching {
            val cipherBytes = CryptoEncodingUtils.hexToBytes(cipherTextHex)
            when (algorithm) {
                CryptoAlgorithm.AES_128 -> AesCipher.decrypt(cipherBytes, secretKey)
                CryptoAlgorithm.RSA_ASYMMETRIC -> RsaCipher.decrypt(cipherBytes, secretKey)
                CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX -> LinearAlgebraMatrixCipher.decrypt(cipherBytes, secretKey)
            }
        }
    }

    fun verifyRecordSignature(
        cipherTextHex: String,
        signatureHex: String,
        epidemiologist: String,
        secretKey: String
    ): Boolean {
        return RsaCipher.verifySignature("$epidemiologist|$cipherTextHex", signatureHex, secretKey)
    }
}
