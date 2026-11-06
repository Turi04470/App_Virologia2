package com.virologia.app.crypto

import kotlinx.serialization.Serializable

@Serializable
enum class CryptoAlgorithm(
    val id: String,
    val displayName: String,
    val shortTag: String,
    val category: String,
    val mathematicalBasis: String
) {
    AES_128(
        id = "AES_128",
        displayName = "AES-128 (Advanced Encryption Standard)",
        shortTag = "AES-128",
        category = "Estándar Simétrico por Bloques",
        mathematicalBasis = "Campos Finitos de Galois GF(2^8), Red de Sustitución-Permutación (10 rondas)"
    ),
    RSA_ASYMMETRIC(
        id = "RSA_ASYMMETRIC",
        displayName = "RSA (Rivest-Shamir-Adleman)",
        shortTag = "RSA-Asimétrico",
        category = "Estándar Asimétrico de Llave Pública",
        mathematicalBasis = "Aritmética Modular, Teorema de Euler φ(n) = (p-1)(q-1) y Factorización de Primos"
    ),
    LINEAR_ALGEBRA_MATRIX(
        id = "LINEAR_ALGEBRA_MATRIX",
        displayName = "Cifrado Matricial Dinámico Z_256 (Inventado)",
        shortTag = "Álgebra Lineal 3x3",
        category = "Algoritmo Propio - Álgebra Lineal",
        mathematicalBasis = "Anillo Modular Z_256, Matrices Invertibles 3x3, Matriz Adjunta y Rotación Vectorial"
    )
}

@Serializable
data class BlockTransformTrace(
    val blockIndex: Int,
    val plainChars: String,
    val plainVector: List<Int>,
    val multipliedVector: List<Int>,
    val rotationVector: List<Int>,
    val cipherVector: List<Int>
)

@Serializable
data class MatrixInspectionDetails(
    val matrixK: List<List<Int>>,
    val determinantRaw: Int,
    val determinantMod256: Int,
    val gcdWith256: Int,
    val detModularInverse: Int,
    val adjugateMatrix: List<List<Int>>,
    val inverseMatrixK: List<List<Int>>,
    val blockTraces: List<BlockTransformTrace>
)

@Serializable
data class RsaInspectionDetails(
    val primeP: Long,
    val primeQ: Long,
    val modulusN: Long,
    val totientPhi: Long,
    val publicExponentE: Long,
    val privateExponentD: Long,
    val sampleBlocks: List<String>
)

@Serializable
data class AesInspectionDetails(
    val keyHex: String,
    val ivHex: String,
    val totalBlocks: Int,
    val roundKeysPreview: List<String>,
    val gfPolynomial: String = "x^8 + x^4 + x^3 + x + 1 (0x11B)"
)

@Serializable
data class EncryptionEnvelope(
    val cipherTextHex: String,
    val cipherTextBase64: String,
    val signatureHex: String,
    val algorithm: CryptoAlgorithm
)
