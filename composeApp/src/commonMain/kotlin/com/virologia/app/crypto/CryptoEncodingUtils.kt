package com.virologia.app.crypto

object CryptoEncodingUtils {
    private const val HEX_CHARS = "0123456789ABCDEF"
    private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            sb.append(HEX_CHARS[v ushr 4])
            sb.append(HEX_CHARS[v and 0x0F])
        }
        return sb.toString()
    }

    fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim().replace(" ", "").uppercase()
        require(clean.length % 2 == 0) { "Longitud hexadecimal inválida" }
        val out = ByteArray(clean.length / 2)
        for (i in out.indices) {
            val hi = HEX_CHARS.indexOf(clean[i * 2])
            val lo = HEX_CHARS.indexOf(clean[i * 2 + 1])
            require(hi >= 0 && lo >= 0) { "Carácter hexadecimal inválido en posición ${i * 2}" }
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }

    fun bytesToBase64(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val sb = StringBuilder(((bytes.size + 2) / 3) * 4)
        var i = 0
        while (i < bytes.size) {
            val b0 = bytes[i++].toInt() and 0xFF
            val b1 = if (i < bytes.size) bytes[i++].toInt() and 0xFF else -1
            val b2 = if (i < bytes.size) bytes[i++].toInt() and 0xFF else -1

            val c0 = b0 ushr 2
            val c1 = ((b0 and 0x03) shl 4) or (if (b1 >= 0) (b1 ushr 4) else 0)
            val c2 = if (b1 >= 0) (((b1 and 0x0F) shl 2) or (if (b2 >= 0) (b2 ushr 6) else 0)) else -1
            val c3 = if (b2 >= 0) (b2 and 0x3F) else -1

            sb.append(BASE64_ALPHABET[c0])
            sb.append(BASE64_ALPHABET[c1])
            sb.append(if (c2 >= 0) BASE64_ALPHABET[c2] else '=')
            sb.append(if (c3 >= 0) BASE64_ALPHABET[c3] else '=')
        }
        return sb.toString()
    }
}
