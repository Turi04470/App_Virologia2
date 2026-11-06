package com.virologia.app.data

import com.virologia.app.crypto.CryptoAlgorithm
import com.virologia.app.crypto.CryptoEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ViralRepository {
    const val DEFAULT_LAB_KEY = "VIRO-2026"

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val _records = MutableStateFlow<List<ViralRecord>>(emptyList())
    val records: StateFlow<List<ViralRecord>> = _records.asStateFlow()

    init {
        loadOrSeed()
    }

    fun loadOrSeed() {
        val rawJson = PlatformDatabase.loadRecordsJson()
        if (!rawJson.isNullOrBlank()) {
            val parsed = runCatching { json.decodeFromString<List<ViralRecord>>(rawJson) }.getOrNull()
            if (parsed != null) {
                _records.value = parsed
                return
            }
        }
        val initial = buildSeedRecords()
        _records.value = initial
        persist(initial)
    }

    fun clearAllRecords() {
        _records.value = emptyList()
        persist(emptyList())
    }

    fun addRecord(
        sampleCode: String,
        patientName: String,
        patientAge: Int,
        pathogen: String,
        biosafetyLevel: BiosafetyLevel,
        viralLoadCopiesMl: String,
        symptoms: String,
        plainDiagnosis: String,
        secretKey: String,
        algorithm: CryptoAlgorithm,
        epidemiologist: String
    ): ViralRecord {
        val effectiveKey = secretKey.ifBlank { DEFAULT_LAB_KEY }
        val envelope = CryptoEngine.encryptDiagnosis(
            plainDiagnosis = plainDiagnosis,
            secretKey = effectiveKey,
            algorithm = algorithm,
            epidemiologist = epidemiologist
        )
        val record = ViralRecord(
            id = "EXP-${(_records.value.size + 101)}",
            sampleCode = sampleCode.trim().uppercase(),
            patientName = patientName.trim(),
            patientAge = patientAge,
            pathogen = pathogen.trim(),
            biosafetyLevel = biosafetyLevel,
            viralLoadCopiesMl = viralLoadCopiesMl.trim(),
            symptoms = symptoms.trim(),
            cipherTextHex = envelope.cipherTextHex,
            cipherTextBase64 = envelope.cipherTextBase64,
            rsaSignatureHex = envelope.signatureHex,
            algorithm = algorithm,
            keyHint = "Llave usada: ${effectiveKey.take(4)}*** (${effectiveKey.length} chars)",
            epidemiologist = epidemiologist.trim(),
            createdAt = "2026-10-25"
        )
        val updated = listOf(record) + _records.value
        _records.value = updated
        persist(updated)
        return record
    }

    fun deleteRecord(id: String) {
        val updated = _records.value.filterNot { it.id == id }
        _records.value = updated
        persist(updated)
    }

    fun resetToDefaultSeed() {
        val seed = buildSeedRecords()
        _records.value = seed
        persist(seed)
    }

    private fun persist(list: List<ViralRecord>) {
        val serialized = json.encodeToString(list)
        PlatformDatabase.saveRecordsJson(serialized)
    }

    private fun buildSeedRecords(): List<ViralRecord> {
        val envMatrix = CryptoEngine.encryptDiagnosis(
            plainDiagnosis = "RT-PCR Positivo para Filovirus Zaire. Aislamiento estricto en cámara de presión negativa.",
            secretKey = DEFAULT_LAB_KEY,
            algorithm = CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX,
            epidemiologist = "Dra. Elena Vázquez"
        )
        val envAes = CryptoEngine.encryptDiagnosis(
            plainDiagnosis = "Secuenciación genómica confirma linaje JN.1 con mutación en proteína Spike S:L455S.",
            secretKey = DEFAULT_LAB_KEY,
            algorithm = CryptoAlgorithm.AES_128,
            epidemiologist = "Dr. Marco Mendoza"
        )
        val envRsa = CryptoEngine.encryptDiagnosis(
            plainDiagnosis = "Antígeno NS1 reactivo y serotipo DENV-2 confirmado. Monitoreo plaquetario cada 6h.",
            secretKey = DEFAULT_LAB_KEY,
            algorithm = CryptoAlgorithm.RSA_ASYMMETRIC,
            epidemiologist = "Dr. Carlos Herrera"
        )
        val envMatrix2 = CryptoEngine.encryptDiagnosis(
            plainDiagnosis = "Cepa neurotrópica H5N1 detectada en lavado broncoalveolar. Iniciar Oseltamivir.",
            secretKey = DEFAULT_LAB_KEY,
            algorithm = CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX,
            epidemiologist = "Dra. Sofía Pineda"
        )

        return listOf(
            ViralRecord(
                id = "EXP-101",
                sampleCode = "VIR-2026-001",
                patientName = "Alejandro Morales R.",
                patientAge = 42,
                pathogen = "Ébola Zaire (EBOV)",
                biosafetyLevel = BiosafetyLevel.BSL_4,
                viralLoadCopiesMl = "8.4 x 10^7 copias/mL",
                symptoms = "Fiebre hemorrágica, mialgia severa, trombocitopenia",
                cipherTextHex = envMatrix.cipherTextHex,
                cipherTextBase64 = envMatrix.cipherTextBase64,
                rsaSignatureHex = envMatrix.signatureHex,
                algorithm = CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX,
                keyHint = "Llave de laboratorio por defecto: VIRO-2026",
                epidemiologist = "Dra. Elena Vázquez",
                createdAt = "2026-10-25"
            ),
            ViralRecord(
                id = "EXP-102",
                sampleCode = "VIR-2026-002",
                patientName = "Valeria Gómez S.",
                patientAge = 29,
                pathogen = "SARS-CoV-2 (Variante JN.1)",
                biosafetyLevel = BiosafetyLevel.BSL_3,
                viralLoadCopiesMl = "3.1 x 10^6 copias/mL",
                symptoms = "Disnea moderada, anosmia, saturación SpO2 91%",
                cipherTextHex = envAes.cipherTextHex,
                cipherTextBase64 = envAes.cipherTextBase64,
                rsaSignatureHex = envAes.signatureHex,
                algorithm = CryptoAlgorithm.AES_128,
                keyHint = "Llave de laboratorio por defecto: VIRO-2026",
                epidemiologist = "Dr. Marco Mendoza",
                createdAt = "2026-10-25"
            ),
            ViralRecord(
                id = "EXP-103",
                sampleCode = "VIR-2026-003",
                patientName = "Roberto Domínguez L.",
                patientAge = 35,
                pathogen = "Dengue Virus (DENV-2)",
                biosafetyLevel = BiosafetyLevel.BSL_2,
                viralLoadCopiesMl = "1.5 x 10^5 copias/mL",
                symptoms = "Fiebre retroocular, exantema maculopapular, artralgia",
                cipherTextHex = envRsa.cipherTextHex,
                cipherTextBase64 = envRsa.cipherTextBase64,
                rsaSignatureHex = envRsa.signatureHex,
                algorithm = CryptoAlgorithm.RSA_ASYMMETRIC,
                keyHint = "Llave de laboratorio por defecto: VIRO-2026",
                epidemiologist = "Dr. Carlos Herrera",
                createdAt = "2026-10-25"
            ),
            ViralRecord(
                id = "EXP-104",
                sampleCode = "VIR-2026-004",
                patientName = "Lucía Fernández M.",
                patientAge = 51,
                pathogen = "Influenza Aviar (H5N1)",
                biosafetyLevel = BiosafetyLevel.BSL_3,
                viralLoadCopiesMl = "6.2 x 10^6 copias/mL",
                symptoms = "Neumonía atípica bilateral, fiebre 39.8 °C",
                cipherTextHex = envMatrix2.cipherTextHex,
                cipherTextBase64 = envMatrix2.cipherTextBase64,
                rsaSignatureHex = envMatrix2.signatureHex,
                algorithm = CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX,
                keyHint = "Llave de laboratorio por defecto: VIRO-2026",
                epidemiologist = "Dra. Sofía Pineda",
                createdAt = "2026-10-25"
            )
        )
    }
}
