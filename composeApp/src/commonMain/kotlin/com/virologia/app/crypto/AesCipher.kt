package com.virologia.app.crypto

/**
 * Implementación pura en Kotlin Multiplatform del estándar NIST FIPS-197 AES-128
 * (Advanced Encryption Standard) en modo CBC con relleno PKCS#7.
 */
object AesCipher {

    private val S_BOX = intArrayOf(
        0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
        0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
        0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
        0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
        0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
        0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
        0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
        0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
        0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
        0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
        0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
        0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
        0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
        0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
        0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
        0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    )

    private val INV_S_BOX = IntArray(256).apply {
        for (i in 0 until 256) {
            this[S_BOX[i]] = i
        }
    }

    private val RCON = intArrayOf(
        0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1B, 0x36
    )

    fun derive128BitKey(secretKey: String): ByteArray {
        val raw = secretKey.encodeToByteArray()
        val key = ByteArray(16)
        for (i in 0 until 16) {
            val base = if (raw.isNotEmpty()) raw[i % raw.size].toInt() and 0xFF else 0x5A
            key[i] = ((base xor ((i * 31 + secretKey.length * 17) and 0xFF)) and 0xFF).toByte()
        }
        return key
    }

    fun deriveIv(secretKey: String): ByteArray {
        val key = derive128BitKey(secretKey)
        return ByteArray(16) { i ->
            (S_BOX[(key[15 - i].toInt() and 0xFF) xor (i * 19 and 0xFF)]).toByte()
        }
    }

    fun encrypt(plainText: String, secretKey: String): ByteArray {
        val key = derive128BitKey(secretKey)
        val iv = deriveIv(secretKey)
        val expandedKey = expandKey(key)
        val padded = pkcs7Pad(plainText.encodeToByteArray(), 16)
        val out = ByteArray(padded.size)
        var prevBlock = iv.copyOf()

        for (offset in padded.indices step 16) {
            val block = ByteArray(16) { i ->
                (padded[offset + i].toInt() xor prevBlock[i].toInt()).toByte()
            }
            val encryptedBlock = encryptBlock(block, expandedKey)
            encryptedBlock.copyInto(out, offset)
            prevBlock = encryptedBlock
        }
        return out
    }

    fun decrypt(cipherBytes: ByteArray, secretKey: String): String {
        require(cipherBytes.isNotEmpty() && cipherBytes.size % 16 == 0) {
            "El bloque cifrado AES debe ser múltiplo de 16 bytes."
        }
        val key = derive128BitKey(secretKey)
        val iv = deriveIv(secretKey)
        val expandedKey = expandKey(key)
        val decryptedPadded = ByteArray(cipherBytes.size)
        var prevBlock = iv.copyOf()

        for (offset in cipherBytes.indices step 16) {
            val cipherBlock = cipherBytes.copyOfRange(offset, offset + 16)
            val decryptedBlock = decryptBlock(cipherBlock, expandedKey)
            for (i in 0 until 16) {
                decryptedPadded[offset + i] = (decryptedBlock[i].toInt() xor prevBlock[i].toInt()).toByte()
            }
            prevBlock = cipherBlock
        }
        val unpadded = pkcs7Unpad(decryptedPadded)
        return unpadded.decodeToString()
    }

    fun inspect(plainText: String, secretKey: String): AesInspectionDetails {
        val key = derive128BitKey(secretKey)
        val iv = deriveIv(secretKey)
        val expanded = expandKey(key)
        val roundKeys = (0..10).map { round ->
            val slice = ByteArray(16) { idx -> expanded[round * 16 + idx].toByte() }
            "Ronda $round: ${CryptoEncodingUtils.bytesToHex(slice)}"
        }
        val paddedSize = pkcs7Pad(plainText.encodeToByteArray(), 16).size
        return AesInspectionDetails(
            keyHex = CryptoEncodingUtils.bytesToHex(key),
            ivHex = CryptoEncodingUtils.bytesToHex(iv),
            totalBlocks = paddedSize / 16,
            roundKeysPreview = roundKeys
        )
    }

    private fun pkcs7Pad(data: ByteArray, blockSize: Int): ByteArray {
        val padLen = blockSize - (data.size % blockSize)
        val out = ByteArray(data.size + padLen)
        data.copyInto(out)
        for (i in data.size until out.size) {
            out[i] = padLen.toByte()
        }
        return out
    }

    private fun pkcs7Unpad(data: ByteArray): ByteArray {
        if (data.isEmpty()) return data
        val padLen = data.last().toInt() and 0xFF
        require(padLen in 1..16 && padLen <= data.size) {
            "Llave AES incorrecta o relleno PKCS#7 inválido."
        }
        for (i in data.size - padLen until data.size) {
            require((data[i].toInt() and 0xFF) == padLen) {
                "Llave AES incorrecta: verificación de padding fallida."
            }
        }
        return data.copyOfRange(0, data.size - padLen)
    }

    private fun expandKey(key: ByteArray): IntArray {
        val w = IntArray(176)
        for (i in 0 until 16) {
            w[i] = key[i].toInt() and 0xFF
        }
        var bytesGenerated = 16
        var rconIteration = 1
        val temp = IntArray(4)

        while (bytesGenerated < 176) {
            for (i in 0 until 4) {
                temp[i] = w[bytesGenerated - 4 + i]
            }
            if (bytesGenerated % 16 == 0) {
                // RotWord
                val k = temp[0]
                temp[0] = temp[1]
                temp[1] = temp[2]
                temp[2] = temp[3]
                temp[3] = k
                // SubWord
                for (i in 0 until 4) {
                    temp[i] = S_BOX[temp[i]]
                }
                temp[0] = temp[0] xor RCON[rconIteration++]
            }
            for (i in 0 until 4) {
                w[bytesGenerated] = w[bytesGenerated - 16] xor temp[i]
                bytesGenerated++
            }
        }
        return w
    }

    private fun encryptBlock(input: ByteArray, expandedKey: IntArray): ByteArray {
        val state = IntArray(16) { input[it].toInt() and 0xFF }
        addRoundKey(state, expandedKey, 0)

        for (round in 1..9) {
            subBytes(state)
            shiftRows(state)
            mixColumns(state)
            addRoundKey(state, expandedKey, round)
        }

        subBytes(state)
        shiftRows(state)
        addRoundKey(state, expandedKey, 10)

        return ByteArray(16) { state[it].toByte() }
    }

    private fun decryptBlock(input: ByteArray, expandedKey: IntArray): ByteArray {
        val state = IntArray(16) { input[it].toInt() and 0xFF }
        addRoundKey(state, expandedKey, 10)

        for (round in 9 downTo 1) {
            invShiftRows(state)
            invSubBytes(state)
            addRoundKey(state, expandedKey, round)
            invMixColumns(state)
        }

        invShiftRows(state)
        invSubBytes(state)
        addRoundKey(state, expandedKey, 0)

        return ByteArray(16) { state[it].toByte() }
    }

    private fun subBytes(state: IntArray) {
        for (i in 0 until 16) state[i] = S_BOX[state[i]]
    }

    private fun invSubBytes(state: IntArray) {
        for (i in 0 until 16) state[i] = INV_S_BOX[state[i]]
    }

    private fun shiftRows(s: IntArray) {
        // Row 1 shift left 1
        var t = s[1]; s[1] = s[5]; s[5] = s[9]; s[9] = s[13]; s[13] = t
        // Row 2 shift left 2
        t = s[2]; s[2] = s[10]; s[10] = t
        t = s[6]; s[6] = s[14]; s[14] = t
        // Row 3 shift left 3
        t = s[15]; s[15] = s[11]; s[11] = s[7]; s[7] = s[3]; s[3] = t
    }

    private fun invShiftRows(s: IntArray) {
        // Row 1 shift right 1
        var t = s[13]; s[13] = s[9]; s[9] = s[5]; s[5] = s[1]; s[1] = t
        // Row 2 shift right 2
        t = s[2]; s[2] = s[10]; s[10] = t
        t = s[6]; s[6] = s[14]; s[14] = t
        // Row 3 shift right 3
        t = s[3]; s[3] = s[7]; s[7] = s[11]; s[11] = s[15]; s[15] = t
    }

    private fun gmul(aInput: Int, bInput: Int): Int {
        var a = aInput
        var b = bInput
        var p = 0
        for (i in 0 until 8) {
            if ((b and 1) != 0) p = p xor a
            val hiBitSet = (a and 0x80) != 0
            a = (a shl 1) and 0xFF
            if (hiBitSet) a = a xor 0x1B
            b = b ushr 1
        }
        return p
    }

    private fun mixColumns(s: IntArray) {
        for (c in 0 until 4) {
            val i = c * 4
            val s0 = s[i]; val s1 = s[i + 1]; val s2 = s[i + 2]; val s3 = s[i + 3]
            s[i] = gmul(0x02, s0) xor gmul(0x03, s1) xor s2 xor s3
            s[i + 1] = s0 xor gmul(0x02, s1) xor gmul(0x03, s2) xor s3
            s[i + 2] = s0 xor s1 xor gmul(0x02, s2) xor gmul(0x03, s3)
            s[i + 3] = gmul(0x03, s0) xor s1 xor s2 xor gmul(0x02, s3)
        }
    }

    private fun invMixColumns(s: IntArray) {
        for (c in 0 until 4) {
            val i = c * 4
            val s0 = s[i]; val s1 = s[i + 1]; val s2 = s[i + 2]; val s3 = s[i + 3]
            s[i] = gmul(0x0e, s0) xor gmul(0x0b, s1) xor gmul(0x0d, s2) xor gmul(0x09, s3)
            s[i + 1] = gmul(0x09, s0) xor gmul(0x0e, s1) xor gmul(0x0b, s2) xor gmul(0x0d, s3)
            s[i + 2] = gmul(0x0d, s0) xor gmul(0x09, s1) xor gmul(0x0e, s2) xor gmul(0x0b, s3)
            s[i + 3] = gmul(0x0b, s0) xor gmul(0x0d, s1) xor gmul(0x09, s2) xor gmul(0x0e, s3)
        }
    }

    private fun addRoundKey(state: IntArray, expandedKey: IntArray, round: Int) {
        val offset = round * 16
        for (i in 0 until 16) {
            state[i] = state[i] xor expandedKey[offset + i]
        }
    }
}
