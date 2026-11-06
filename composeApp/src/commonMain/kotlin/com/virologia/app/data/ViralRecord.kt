package com.virologia.app.data

import com.virologia.app.crypto.CryptoAlgorithm
import kotlinx.serialization.Serializable

@Serializable
enum class BiosafetyLevel(
    val code: String,
    val riskLabel: String,
    val description: String
) {
    BSL_1("BSL-1", "Riesgo Bajo", "Agentes bien caracterizados sin riesgo letal en adultos sanos"),
    BSL_2("BSL-2", "Riesgo Moderado", "Agentes patógenos humanos con transmisión percutánea o mucosa"),
    BSL_3("BSL-3", "Riesgo Alto (Aerosol)", "Agentes exóticos con transmisión respiratoria y consecuencias graves"),
    BSL_4("BSL-4", "Riesgo Extremo / Letal", "Patógenos hemorrágicos de alta letalidad sin vacuna disponible")
}

@Serializable
data class ViralRecord(
    val id: String,
    val sampleCode: String,
    val patientName: String,
    val patientAge: Int,
    val pathogen: String,
    val biosafetyLevel: BiosafetyLevel,
    val viralLoadCopiesMl: String,
    val symptoms: String,
    val cipherTextHex: String,
    val cipherTextBase64: String,
    val rsaSignatureHex: String,
    val algorithm: CryptoAlgorithm,
    val keyHint: String,
    val epidemiologist: String,
    val createdAt: String
)
