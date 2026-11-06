package com.virologia.app

import com.virologia.app.crypto.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CryptoAlgorithmsTest {

    @Test
    fun testAes128RoundTrip() {
        val original = "Diagnóstico SARS-CoV-2 variante JN.1 con carga viral 3.1x10^6 copias/mL"
        val key = "VIRO-2026"
        val encrypted = AesCipher.encrypt(original, key)
        val decrypted = AesCipher.decrypt(encrypted, key)
        assertEquals(original, decrypted)
    }

    @Test
    fun testRsaAsymmetricRoundTripAndSignature() {
        val original = "Confirmación de Filovirus Ébola Zaire en muestra hemorrágica BSL-4"
        val key = "CLAVE-MEDICA-RSA"
        val encrypted = RsaCipher.encrypt(original, key)
        val decrypted = RsaCipher.decrypt(encrypted, key)
        assertEquals(original, decrypted)

        val sig = RsaCipher.signRecord(original, key)
        assertTrue(RsaCipher.verifySignature(original, sig, key))
        assertFalse(RsaCipher.verifySignature(original, sig, "LLAVE-FALSA"))
    }

    @Test
    fun testLinearAlgebraMatrixInvertibilityAndRoundTrip() {
        val keysToTest = listOf("VIRO-2026", "ALGEBRA-LINEAL", "A", "123456789", " ÉBOLA-BSL4 ")
        for (key in keysToTest) {
            val k = LinearAlgebraMatrixCipher.deriveInvertibleMatrix(key)
            val detMod = LinearAlgebraMatrixCipher.mod256(LinearAlgebraMatrixCipher.determinant3x3(k))
            assertEquals(1, LinearAlgebraMatrixCipher.gcd(detMod, 256), "El determinante debe ser coprimo con 256")

            val kInv = LinearAlgebraMatrixCipher.inverseMatrix3x3Mod256(k)
            // Verificar que K * K^(-1) == Identidad I_3 (mod 256)
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    var sum = 0
                    for (m in 0 until 3) {
                        sum += k[r][m] * kInv[m][c]
                    }
                    val expected = if (r == c) 1 else 0
                    assertEquals(expected, LinearAlgebraMatrixCipher.mod256(sum))
                }
            }

            val sampleText = "Muestra positiva H5N1 con requerimiento de aislamiento inmediato [$key]"
            val cipherBytes = LinearAlgebraMatrixCipher.encrypt(sampleText, key)
            val recovered = LinearAlgebraMatrixCipher.decrypt(cipherBytes, key)
            assertEquals(sampleText, recovered)
        }
    }

    @Test
    fun testCryptoEngineUnifiedRoundTrip() {
        val text = "Paciente con fiebre hemorrágica y trombocitopenia severa."
        val key = "VIRO-2026"
        CryptoAlgorithm.entries.forEach { algorithm ->
            val env = CryptoEngine.encryptDiagnosis(text, key, algorithm, "Dra. Elena Vázquez")
            val dec = CryptoEngine.decryptDiagnosis(env.cipherTextHex, key, algorithm)
            assertTrue(dec.isSuccess, "Fallo al descifrar con ${algorithm.name}")
            assertEquals(text, dec.getOrThrow())
        }
    }
}
