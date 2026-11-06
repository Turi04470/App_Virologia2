package com.virologia.app.crypto

/**
 * Implementación didáctica y funcional de RSA (Rivest-Shamir-Adleman) en Kotlin Multiplatform.
 * Soporta generación determinista de pares de llaves (pública/privada) a partir de semilla/clave,
 * cifrado asimétrico (C = M^e mod n), descifrado (M = C^d mod n) y sellado digital.
 */
object RsaCipher {

    // Tabla de primos de 16 bits para garantizar que n = p * q quepa en aritmética de 64 bits sin desbordamiento en modMul
    private val PRIME_POOL = longArrayOf(
        10007L, 10009L, 10037L, 10039L, 10061L, 10067L, 10069L, 10079L,
        10091L, 10093L, 10099L, 10103L, 10111L, 10133L, 10139L, 10141L,
        10151L, 10159L, 10163L, 10169L, 10177L, 10181L, 10193L, 10211L,
        10223L, 10243L, 10247L, 10253L, 10259L, 10267L, 10271L, 10273L
    )

    data class RsaKeyPair(
        val p: Long,
        val q: Long,
        val n: Long,
        val phi: Long,
        val e: Long,
        val d: Long
    )

    fun deriveKeyPair(secretSeed: String): RsaKeyPair {
        val clean = if (secretSeed.isBlank()) "LAB-VIRO-DEFAULT" else secretSeed.trim()
        var hash1 = 17L
        var hash2 = 31L
        for ((idx, ch) in clean.withIndex()) {
            hash1 = (hash1 * 37L + ch.code + idx) and 0x7FFFFFFFL
            hash2 = (hash2 * 53L + ch.code * (idx + 3)) and 0x7FFFFFFFL
        }
        val idxP = (hash1 % PRIME_POOL.size).toInt()
        var idxQ = (hash2 % PRIME_POOL.size).toInt()
        if (idxQ == idxP) {
            idxQ = (idxP + 7) % PRIME_POOL.size
        }

        val p = PRIME_POOL[idxP]
        val q = PRIME_POOL[idxQ]
        val n = p * q
        val phi = (p - 1L) * (q - 1L)

        var e = 65537L
        while (gcd(e, phi) != 1L) {
            e += 2L
        }
        val d = modInverse(e, phi)
        return RsaKeyPair(p = p, q = q, n = n, phi = phi, e = e, d = d)
    }

    fun encrypt(plainText: String, secretSeed: String): ByteArray {
        val keys = deriveKeyPair(secretSeed)
        val inputBytes = plainText.encodeToByteArray()
        // Cada byte M (con sal de posición reversible pequeña para evitar bloques idénticos visibles, o M puro < n)
        // Aquí ciframos bloques de 16 bits (2 bytes) ya que n > 100,000,000 > 65,535.
        // Guardamos primero la longitud original en 2 bytes cifrados, luego pares de bytes.
        val blocks = mutableListOf<Int>()
        blocks.add(inputBytes.size and 0xFFFF)
        var i = 0
        while (i < inputBytes.size) {
            val b0 = inputBytes[i].toInt() and 0xFF
            val b1 = if (i + 1 < inputBytes.size) inputBytes[i + 1].toInt() and 0xFF else 0
            val m = (b0 shl 8) or b1
            blocks.add(m)
            i += 2
        }

        val out = ByteArray(blocks.size * 4)
        for ((idx, m) in blocks.withIndex()) {
            val c = modPow(m.toLong(), keys.e, keys.n).toInt()
            val offset = idx * 4
            out[offset] = ((c ushr 24) and 0xFF).toByte()
            out[offset + 1] = ((c ushr 16) and 0xFF).toByte()
            out[offset + 2] = ((c ushr 8) and 0xFF).toByte()
            out[offset + 3] = (c and 0xFF).toByte()
        }
        return out
    }

    fun decrypt(cipherBytes: ByteArray, secretSeed: String): String {
        require(cipherBytes.size >= 4 && cipherBytes.size % 4 == 0) {
            "Formato de criptograma RSA inválido."
        }
        val keys = deriveKeyPair(secretSeed)
        val totalBlocks = cipherBytes.size / 4
        val decodedBlocks = IntArray(totalBlocks)

        for (idx in 0 until totalBlocks) {
            val offset = idx * 4
            val c = ((cipherBytes[offset].toInt() and 0xFF) shl 24) or
                    ((cipherBytes[offset + 1].toInt() and 0xFF) shl 16) or
                    ((cipherBytes[offset + 2].toInt() and 0xFF) shl 8) or
                    (cipherBytes[offset + 3].toInt() and 0xFF)
            val cUnsigned = c.toLong() and 0xFFFFFFFFL
            require(cUnsigned < keys.n) {
                "Llave privada RSA no corresponde al módulo n del criptograma."
            }
            val m = modPow(cUnsigned, keys.d, keys.n).toInt()
            decodedBlocks[idx] = m
        }

        val expectedLen = decodedBlocks[0]
        val maxPossibleBytes = (totalBlocks - 1) * 2
        require(expectedLen in 0..maxPossibleBytes && (maxPossibleBytes - expectedLen) <= 1) {
            "Llave privada RSA incorrecta: longitud de bloque inválida al descifrar."
        }

        val resultBytes = ByteArray(expectedLen)
        var bytePos = 0
        for (blockIdx in 1 until totalBlocks) {
            val m = decodedBlocks[blockIdx]
            require(m in 0..0xFFFF) {
                "Llave privada RSA incorrecta: desbordamiento de bloque."
            }
            if (bytePos < expectedLen) {
                resultBytes[bytePos++] = ((m ushr 8) and 0xFF).toByte()
            }
            if (bytePos < expectedLen) {
                resultBytes[bytePos++] = (m and 0xFF).toByte()
            }
        }
        return resultBytes.decodeToString()
    }

    /**
     * Genera una firma digital RSA sobre el resumen del expediente usando la llave privada d:
     * S = H(m)^d mod n
     */
    fun signRecord(payload: String, secretSeed: String): String {
        val keys = deriveKeyPair(secretSeed)
        val digest = computeDigestModN(payload, keys.n)
        val signature = modPow(digest, keys.d, keys.n)
        return signature.toString(16).uppercase().padStart(8, '0')
    }

    /**
     * Verifica la firma digital RSA usando la llave pública (e, n):
     * H'(m) = S^e mod n
     */
    fun verifySignature(payload: String, signatureHex: String, secretSeed: String): Boolean {
        return try {
            val keys = deriveKeyPair(secretSeed)
            val sigLong = signatureHex.trim().toLong(16)
            val recoveredDigest = modPow(sigLong, keys.e, keys.n)
            val expectedDigest = computeDigestModN(payload, keys.n)
            recoveredDigest == expectedDigest
        } catch (_: Exception) {
            false
        }
    }

    fun inspect(plainText: String, secretSeed: String): RsaInspectionDetails {
        val keys = deriveKeyPair(secretSeed)
        val bytes = plainText.encodeToByteArray()
        val samples = mutableListOf<String>()
        val limit = minOf(bytes.size, 6)
        var i = 0
        var blockNum = 1
        while (i < limit) {
            val b0 = bytes[i].toInt() and 0xFF
            val b1 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0
            val m = ((b0 shl 8) or b1).toLong()
            val c = modPow(m, keys.e, keys.n)
            val mDec = modPow(c, keys.d, keys.n)
            samples.add("Bloque #$blockNum: M=$m -> C = $m^${keys.e} mod ${keys.n} = $c -> Descifrado C^d mod n = $mDec")
            i += 2
            blockNum++
        }
        return RsaInspectionDetails(
            primeP = keys.p,
            primeQ = keys.q,
            modulusN = keys.n,
            totientPhi = keys.phi,
            publicExponentE = keys.e,
            privateExponentD = keys.d,
            sampleBlocks = samples
        )
    }

    private fun computeDigestModN(text: String, n: Long): Long {
        var h = 5381L
        for (ch in text) {
            h = ((h * 33L) xor ch.code.toLong()) % (n - 2L)
        }
        if (h <= 1L) h += 2L
        return h
    }

    fun gcd(aInput: Long, bInput: Long): Long {
        var a = kotlin.math.abs(aInput)
        var b = kotlin.math.abs(bInput)
        while (b != 0L) {
            val t = a % b
            a = b
            b = t
        }
        return a
    }

    fun modInverse(a: Long, m: Long): Long {
        var t = 0L
        var newT = 1L
        var r = m
        var newR = a % m

        while (newR != 0L) {
            val quotient = r / newR
            val tempT = t - quotient * newT
            t = newT
            newT = tempT

            val tempR = r - quotient * newR
            r = newR
            newR = tempR
        }
        require(r == 1L) { "El elemento $a no es invertible módulo $m" }
        if (t < 0L) t += m
        return t
    }

    fun modPow(baseInput: Long, expInput: Long, mod: Long): Long {
        var result = 1L
        var base = ((baseInput % mod) + mod) % mod
        var exp = expInput
        while (exp > 0L) {
            if ((exp and 1L) == 1L) {
                result = (result * base) % mod
            }
            base = (base * base) % mod
            exp = exp ushr 1
        }
        return result
    }
}
