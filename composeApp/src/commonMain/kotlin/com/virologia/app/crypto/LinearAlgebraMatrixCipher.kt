package com.virologia.app.crypto

/**
 * Algoritmo Inventado respaldado por Álgebra Lineal:
 * Cifrado Matricial Modular Dinámico (CMMD-Z256) sobre el anillo Z_256.
 *
 * Fundamento Matemático:
 * 1. Sea K una matriz de 3x3 en Z_256 derivada de la clave secreta.
 * 2. K es invertible en Z_256 si y solo si mcd(det(K), 256) = 1 (es decir, det(K) es impar).
 * 3. La matriz inversa modular se calcula mediante la matriz adjunta:
 *    K^(-1) = (det(K))^(-1) * Adj(K) (mod 256), donde Adj(K) = Cof(K)^T.
 * 4. Para romper la linealidad estática del cifrado de Hill clásico, cada bloque vectorial p_i en Z_256^3
 *    se transforma mediante una rotación afín dependiente del índice de bloque i y la traza tr(K):
 *    Cifrado:    c_i = (K * p_i + r_i) mod 256
 *    Descifrado: p_i = K^(-1) * (c_i - r_i) mod 256
 */
object LinearAlgebraMatrixCipher {

    private const val MOD = 256

    fun mod256(value: Int): Int {
        val r = value % MOD
        return if (r < 0) r + MOD else r
    }

    fun gcd(aInput: Int, bInput: Int): Int {
        var a = kotlin.math.abs(aInput)
        var b = kotlin.math.abs(bInput)
        while (b != 0) {
            val t = a % b
            a = b
            b = t
        }
        return a
    }

    fun modInverse256(aInput: Int): Int {
        val a = mod256(aInput)
        var t = 0
        var newT = 1
        var r = MOD
        var newR = a

        while (newR != 0) {
            val q = r / newR
            val tempT = t - q * newT
            t = newT
            newT = tempT

            val tempR = r - q * newR
            r = newR
            newR = tempR
        }
        require(r == 1) { "El determinante $aInput no tiene inverso multiplicativo módulo 256." }
        return mod256(t)
    }

    /**
     * Deriva una matriz 3x3 garantizada invertible en Z_256 a partir de una llave de texto.
     */
    fun deriveInvertibleMatrix(secretKey: String): Array<IntArray> {
        val seed = if (secretKey.isBlank()) "VIRO-MATRIX-KEY" else secretKey.trim()
        val seedBytes = seed.encodeToByteArray()
        val m = Array(3) { IntArray(3) }

        var acc = 73
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val b = seedBytes[(r * 3 + c) % seedBytes.size].toInt() and 0xFF
                acc = mod256(acc * 29 + b + (r * 13) + (c * 7) + seedBytes.size)
                m[r][c] = if (acc == 0) (r * 3 + c + 3) else acc
            }
        }

        // Asegurar que mcd(det(K), 256) == 1 (determinante impar en Z_256)
        var attempts = 0
        while (gcd(mod256(determinant3x3(m)), MOD) != 1) {
            attempts++
            // Ajuste algebraico sobre la diagonal principal y subdiagonal para garantizar invertibilidad
            m[0][0] = mod256(m[0][0] + 1) or 1
            m[1][1] = mod256(m[1][1] + attempts) or 1
            m[2][2] = mod256(m[2][2] + 3) or 1
            m[0][1] = mod256(m[0][1] and 0xFE)
            m[1][2] = mod256(m[1][2] and 0xFE)
            if (attempts > 16) {
                // Matriz triangular superior con diagonal impar tiene determinante = prod(diag) = impar
                m[1][0] = 0
                m[2][0] = 0
                m[2][1] = 0
                m[0][0] = m[0][0] or 1
                m[1][1] = m[1][1] or 1
                m[2][2] = m[2][2] or 1
            }
        }
        return m
    }

    fun determinant3x3(k: Array<IntArray>): Int {
        val term0 = k[0][0] * (k[1][1] * k[2][2] - k[1][2] * k[2][1])
        val term1 = k[0][1] * (k[1][0] * k[2][2] - k[1][2] * k[2][0])
        val term2 = k[0][2] * (k[1][0] * k[2][1] - k[1][1] * k[2][0])
        return term0 - term1 + term2
    }

    /**
     * Calcula la matriz adjunta Adj(K) = Cof(K)^T módulo 256.
     */
    fun adjugate3x3Mod256(k: Array<IntArray>): Array<IntArray> {
        val cof = Array(3) { IntArray(3) }
        cof[0][0] = +(k[1][1] * k[2][2] - k[1][2] * k[2][1])
        cof[0][1] = -(k[1][0] * k[2][2] - k[1][2] * k[2][0])
        cof[0][2] = +(k[1][0] * k[2][1] - k[1][1] * k[2][0])

        cof[1][0] = -(k[0][1] * k[2][2] - k[0][2] * k[2][1])
        cof[1][1] = +(k[0][0] * k[2][2] - k[0][2] * k[2][0])
        cof[1][2] = -(k[0][0] * k[2][1] - k[0][1] * k[2][0])

        cof[2][0] = +(k[0][1] * k[1][2] - k[0][2] * k[1][1])
        cof[2][1] = -(k[0][0] * k[1][2] - k[0][2] * k[1][0])
        cof[2][2] = +(k[0][0] * k[1][1] - k[0][1] * k[1][0])

        // Transponer Cof(K) y aplicar módulo 256
        val adj = Array(3) { IntArray(3) }
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                adj[r][c] = mod256(cof[c][r])
            }
        }
        return adj
    }

    /**
     * Calcula la matriz inversa K^(-1) = det(K)^(-1) * Adj(K) mod 256.
     */
    fun inverseMatrix3x3Mod256(k: Array<IntArray>): Array<IntArray> {
        val detMod = mod256(determinant3x3(k))
        val detInv = modInverse256(detMod)
        val adj = adjugate3x3Mod256(k)
        val inv = Array(3) { IntArray(3) }
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                inv[r][c] = mod256(detInv * adj[r][c])
            }
        }
        return inv
    }

    fun rotationVector(k: Array<IntArray>, blockIndex: Int): IntArray {
        val trace = k[0][0] + k[1][1] + k[2][2]
        val det = mod256(determinant3x3(k))
        return intArrayOf(
            mod256(trace + 19 * (blockIndex + 1) + k[0][2]),
            mod256(det + 37 * (blockIndex + 1) + k[1][0]),
            mod256((trace xor det) + 53 * (blockIndex + 1) + k[2][1])
        )
    }

    fun multiplyMatrixVectorMod256(m: Array<IntArray>, v: IntArray): IntArray {
        return IntArray(3) { r ->
            mod256(m[r][0] * v[0] + m[r][1] * v[1] + m[r][2] * v[2])
        }
    }

    fun encrypt(plainText: String, secretKey: String): ByteArray {
        val k = deriveInvertibleMatrix(secretKey)
        val rawBytes = plainText.encodeToByteArray()

        // Cabecera de 3 bytes: [0x56 ('V'), lenHi, lenLo] para verificación de llave e integridad
        val totalLen = rawBytes.size
        val payloadWithHeader = ByteArray(3 + totalLen)
        payloadWithHeader[0] = 0x56.toByte()
        payloadWithHeader[1] = ((totalLen ushr 8) and 0xFF).toByte()
        payloadWithHeader[2] = (totalLen and 0xFF).toByte()
        rawBytes.copyInto(payloadWithHeader, 3)

        // Padding a múltiplo de 3 para formar vectores en Z_256^3
        val remainder = payloadWithHeader.size % 3
        val padCount = if (remainder == 0) 0 else (3 - remainder)
        val padded = ByteArray(payloadWithHeader.size + padCount)
        payloadWithHeader.copyInto(padded)

        val out = ByteArray(padded.size)
        val numBlocks = padded.size / 3
        for (bIdx in 0 until numBlocks) {
            val offset = bIdx * 3
            val pVec = intArrayOf(
                padded[offset].toInt() and 0xFF,
                padded[offset + 1].toInt() and 0xFF,
                padded[offset + 2].toInt() and 0xFF
            )
            val multVec = multiplyMatrixVectorMod256(k, pVec)
            val rotVec = rotationVector(k, bIdx)
            for (j in 0 until 3) {
                out[offset + j] = mod256(multVec[j] + rotVec[j]).toByte()
            }
        }
        return out
    }

    fun decrypt(cipherBytes: ByteArray, secretKey: String): String {
        require(cipherBytes.size >= 3 && cipherBytes.size % 3 == 0) {
            "El criptograma matricial debe ser múltiplo de 3 bytes (vectores en Z_256^3)."
        }
        val k = deriveInvertibleMatrix(secretKey)
        val kInv = inverseMatrix3x3Mod256(k)

        val recovered = ByteArray(cipherBytes.size)
        val numBlocks = cipherBytes.size / 3
        for (bIdx in 0 until numBlocks) {
            val offset = bIdx * 3
            val rotVec = rotationVector(k, bIdx)
            val shifted = intArrayOf(
                mod256((cipherBytes[offset].toInt() and 0xFF) - rotVec[0]),
                mod256((cipherBytes[offset + 1].toInt() and 0xFF) - rotVec[1]),
                mod256((cipherBytes[offset + 2].toInt() and 0xFF) - rotVec[2])
            )
            val pVec = multiplyMatrixVectorMod256(kInv, shifted)
            for (j in 0 until 3) {
                recovered[offset + j] = pVec[j].toByte()
            }
        }

        val magic = recovered[0].toInt() and 0xFF
        val expectedLen = ((recovered[1].toInt() and 0xFF) shl 8) or (recovered[2].toInt() and 0xFF)
        require(magic == 0x56 && expectedLen >= 0 && (3 + expectedLen) <= recovered.size && (recovered.size - (3 + expectedLen)) < 3) {
            "Llave matricial incorrecta: la matriz inversa K^(-1) no reconstruye el vector de verificación."
        }
        return recovered.copyOfRange(3, 3 + expectedLen).decodeToString()
    }

    fun inspect(plainText: String, secretKey: String): MatrixInspectionDetails {
        val k = deriveInvertibleMatrix(secretKey)
        val detRaw = determinant3x3(k)
        val detMod = mod256(detRaw)
        val gcdVal = gcd(detMod, MOD)
        val detInv = modInverse256(detMod)
        val adj = adjugate3x3Mod256(k)
        val kInv = inverseMatrix3x3Mod256(k)

        val bytes = plainText.encodeToByteArray()
        val paddedLen = if (bytes.isEmpty()) 3 else ((bytes.size + 2) / 3) * 3
        val padded = ByteArray(paddedLen) { idx -> if (idx < bytes.size) bytes[idx] else 0 }

        val maxBlocksToPreview = minOf(padded.size / 3, 5)
        val traces = (0 until maxBlocksToPreview).map { bIdx ->
            val offset = bIdx * 3
            val pVec = intArrayOf(
                padded[offset].toInt() and 0xFF,
                padded[offset + 1].toInt() and 0xFF,
                padded[offset + 2].toInt() and 0xFF
            )
            val multVec = multiplyMatrixVectorMod256(k, pVec)
            val rotVec = rotationVector(k, bIdx)
            val cVec = IntArray(3) { j -> mod256(multVec[j] + rotVec[j]) }
            val chars = (0 until 3).map { j ->
                val code = pVec[j]
                if (code in 32..126) code.toChar() else '·'
            }.joinToString("")

            BlockTransformTrace(
                blockIndex = bIdx + 1,
                plainChars = chars,
                plainVector = pVec.toList(),
                multipliedVector = multVec.toList(),
                rotationVector = rotVec.toList(),
                cipherVector = cVec.toList()
            )
        }

        return MatrixInspectionDetails(
            matrixK = k.map { it.toList() },
            determinantRaw = detRaw,
            determinantMod256 = detMod,
            gcdWith256 = gcdVal,
            detModularInverse = detInv,
            adjugateMatrix = adj.map { it.toList() },
            inverseMatrixK = kInv.map { it.toList() },
            blockTraces = traces
        )
    }
}
