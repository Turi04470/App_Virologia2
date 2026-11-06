package com.virologia.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virologia.app.crypto.CryptoAlgorithm
import com.virologia.app.crypto.CryptoEngine
import com.virologia.app.data.BiosafetyLevel
import com.virologia.app.data.ViralRecord
import com.virologia.app.data.ViralRepository

@Composable
fun NewSampleScreen(
    nextSampleIndex: Int,
    isMobile: Boolean = false,
    onSaveSample: (
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
    ) -> ViralRecord,
    onOpenInCryptoLab: (ViralRecord) -> Unit
) {
    var sampleCode by remember(nextSampleIndex) {
        mutableStateOf("VIR-2026-00$nextSampleIndex")
    }
    var patientName by remember { mutableStateOf("Mariana Torres G.") }
    var patientAgeText by remember { mutableStateOf("31") }
    var pathogen by remember { mutableStateOf("Virus Nipah (Henipavirus)") }
    var biosafetyLevel by remember { mutableStateOf(BiosafetyLevel.BSL_4) }
    var viralLoad by remember { mutableStateOf("4.9 x 10^7 copias/mL") }
    var symptoms by remember { mutableStateOf("Encefalitis aguda febril, letargia, dificultad respiratoria") }
    var plainDiagnosis by remember {
        mutableStateOf("ARN viral de Henipavirus confirmado por RT-qPCR en líquido cefalorraquídeo. Contención BSL-4 obligatoria.")
    }
    var epidemiologist by remember { mutableStateOf("Dr. Sebastián Ríos") }
    var secretKey by remember { mutableStateOf(ViralRepository.DEFAULT_LAB_KEY) }
    var selectedAlgorithm by remember { mutableStateOf(CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX) }
    var savedBannerRecord by remember { mutableStateOf<ViralRecord?>(null) }

    val livePreview = remember(plainDiagnosis, secretKey, selectedAlgorithm, epidemiologist) {
        if (plainDiagnosis.isBlank()) null
        else runCatching {
            CryptoEngine.encryptDiagnosis(
                plainDiagnosis = plainDiagnosis,
                secretKey = secretKey.ifBlank { ViralRepository.DEFAULT_LAB_KEY },
                algorithm = selectedAlgorithm,
                epidemiologist = epidemiologist.ifBlank { "Laboratorio Central" }
            )
        }.getOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Plantillas rápidas para demostración en clase
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Plantillas Rápidas para Demostración en Clase:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                patientName = "Mariana Torres G."
                                patientAgeText = "31"
                                pathogen = "Virus Nipah (Henipavirus)"
                                biosafetyLevel = BiosafetyLevel.BSL_4
                                viralLoad = "4.9 x 10^7 copias/mL"
                                symptoms = "Encefalitis aguda febril, desorientación"
                                plainDiagnosis = "ARN viral de Henipavirus confirmado en LCR. Aislamiento BSL-4 inmediato."
                                selectedAlgorithm = CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cargar Caso Nipah (BSL-4 + Álgebra Lineal)", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                patientName = "Fernando Ruiz P."
                                patientAgeText = "47"
                                pathogen = "Fiebre de Lassa (Arenavirus)"
                                biosafetyLevel = BiosafetyLevel.BSL_4
                                viralLoad = "9.2 x 10^6 copias/mL"
                                symptoms = "Hemorragia gingival, hipoacusia neurosensorial"
                                plainDiagnosis = "Serología IgM positiva para Arenavirus Lassa. Administrar Ribavirina IV."
                                selectedAlgorithm = CryptoAlgorithm.AES_128
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cargar Caso Lassa (BSL-4 + AES-128)", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                patientName = "Camila Vega D."
                                patientAgeText = "24"
                                pathogen = "Rabia Clásica (Lyssavirus)"
                                biosafetyLevel = BiosafetyLevel.BSL_3
                                viralLoad = "2.1 x 10^5 copias/mL"
                                symptoms = "Hidrofobia, parestesia en sitio de mordedura, hiperactividad"
                                plainDiagnosis = "Inmunofluorescencia directa positiva a corpúsculos de Negri. Inmunoglobulina antirrábica."
                                selectedAlgorithm = CryptoAlgorithm.RSA_ASYMMETRIC
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cargar Caso Rabia (BSL-3 + RSA)", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                patientName = ""
                                patientAgeText = ""
                                pathogen = ""
                                biosafetyLevel = BiosafetyLevel.BSL_2
                                viralLoad = ""
                                symptoms = ""
                                plainDiagnosis = ""
                                epidemiologist = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Limpiar Campos (Llenado Manual en Blanco)", fontSize = 12.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                patientName = "Mariana Torres G."
                                patientAgeText = "31"
                                pathogen = "Virus Nipah (Henipavirus)"
                                biosafetyLevel = BiosafetyLevel.BSL_4
                                viralLoad = "4.9 x 10^7 copias/mL"
                                symptoms = "Encefalitis aguda febril, desorientación"
                                plainDiagnosis = "ARN viral de Henipavirus confirmado en LCR. Aislamiento BSL-4 inmediato."
                                selectedAlgorithm = CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX
                            },
                            label = { Text("Caso Nipah (BSL-4 + Álgebra Lineal)") }
                        )
                        AssistChip(
                            onClick = {
                                patientName = "Fernando Ruiz P."
                                patientAgeText = "47"
                                pathogen = "Fiebre de Lassa (Arenavirus)"
                                biosafetyLevel = BiosafetyLevel.BSL_4
                                viralLoad = "9.2 x 10^6 copias/mL"
                                symptoms = "Hemorragia gingival, hipoacusia neurosensorial"
                                plainDiagnosis = "Serología IgM positiva para Arenavirus Lassa. Administrar Ribavirina IV."
                                selectedAlgorithm = CryptoAlgorithm.AES_128
                            },
                            label = { Text("Caso Lassa (BSL-4 + AES-128)") }
                        )
                        AssistChip(
                            onClick = {
                                patientName = "Camila Vega D."
                                patientAgeText = "24"
                                pathogen = "Rabia Clásica (Lyssavirus)"
                                biosafetyLevel = BiosafetyLevel.BSL_3
                                viralLoad = "2.1 x 10^5 copias/mL"
                                symptoms = "Hidrofobia, parestesia en sitio de mordedura, hiperactividad"
                                plainDiagnosis = "Inmunofluorescencia directa positiva a corpúsculos de Negri. Inmunoglobulina antirrábica."
                                selectedAlgorithm = CryptoAlgorithm.RSA_ASYMMETRIC
                            },
                            label = { Text("Caso Rabia (BSL-3 + RSA)") }
                        )
                        AssistChip(
                            onClick = {
                                patientName = ""
                                patientAgeText = ""
                                pathogen = ""
                                biosafetyLevel = BiosafetyLevel.BSL_2
                                viralLoad = ""
                                symptoms = ""
                                plainDiagnosis = ""
                                epidemiologist = ""
                            },
                            label = { Text("Limpiar Campos (Manual)") }
                        )
                    }
                }
            }
        }

        // Datos del Paciente y Muestra
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "1. Identificación de Muestra y Paciente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isMobile) {
                    // En móvil todos los campos de texto se apilan verticalmente a ancho completo
                    OutlinedTextField(
                        value = sampleCode,
                        onValueChange = { sampleCode = it },
                        label = { Text("Código de Muestra") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("Nombre del Paciente") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = patientAgeText,
                        onValueChange = { patientAgeText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Edad del Paciente (años)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = epidemiologist,
                        onValueChange = { epidemiologist = it },
                        label = { Text("Epidemiólogo Firmante (RSA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pathogen,
                        onValueChange = { pathogen = it },
                        label = { Text("Agente Patógeno / Virus") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viralLoad,
                        onValueChange = { viralLoad = it },
                        label = { Text("Carga Viral (copias/mL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = sampleCode,
                            onValueChange = { sampleCode = it },
                            label = { Text("Código de Muestra") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = patientAgeText,
                            onValueChange = { patientAgeText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Edad") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Nombre del Paciente") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = epidemiologist,
                            onValueChange = { epidemiologist = it },
                            label = { Text("Epidemiólogo Firmante (RSA)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = pathogen,
                            onValueChange = { pathogen = it },
                            label = { Text("Agente Patógeno / Virus") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = viralLoad,
                            onValueChange = { viralLoad = it },
                            label = { Text("Carga Viral") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    "Nivel de Bioseguridad (BSL):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BiosafetyLevel.entries.forEach { level ->
                            val color = bslBadgeColor(level)
                            val isSelected = biosafetyLevel == level
                            Surface(
                                color = if (isSelected) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) color else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { biosafetyLevel = level }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { biosafetyLevel = level }
                                    )
                                    Column {
                                        Text(
                                            text = "${level.code} • ${level.riskLabel}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = level.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BiosafetyLevel.entries.forEach { level ->
                            val color = bslBadgeColor(level)
                            FilterChip(
                                selected = biosafetyLevel == level,
                                onClick = { biosafetyLevel = level },
                                label = { Text("${level.code}: ${level.riskLabel}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.25f),
                                    selectedLabelColor = color
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Sintomatología Clínica Observada") },
                    minLines = if (isMobile) 2 else 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Selección de Algoritmo Criptográfico y Diagnóstico Confidencial
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "2. Diagnóstico Confidencial y Motor de Encriptación",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = plainDiagnosis,
                    onValueChange = { plainDiagnosis = it },
                    label = { Text("Diagnóstico Clínico Confidencial (Texto Plano a Cifrar)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = secretKey,
                    onValueChange = { secretKey = it },
                    label = { Text("Llave Secreta / Semilla Criptográfica") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Selecciona el Algoritmo de Cifrado:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                CryptoAlgorithm.entries.forEach { alg ->
                    val isSelected = selectedAlgorithm == alg
                    val algColor = algorithmBadgeColor(alg)
                    Surface(
                        color = if (isSelected) algColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) algColor else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedAlgorithm = alg }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedAlgorithm = alg }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alg.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) algColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${alg.category} • ${alg.mathematicalBasis}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Vista Previa del Criptograma en Tiempo Real
                if (livePreview != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070C15), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "VISTA PREVIA DEL CRIPTOGRAMA EN TIEMPO REAL (${selectedAlgorithm.shortTag}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = algorithmBadgeColor(selectedAlgorithm)
                        )
                        Text(
                            text = "HEX: ${livePreview.cipherTextHex}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "BASE64: ${livePreview.cipherTextBase64}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "FIRMA DIGITAL RSA DEL EPIDEMIÓLOGO: ${livePreview.signatureHex}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (plainDiagnosis.isNotBlank() && patientName.isNotBlank()) {
                            val saved = onSaveSample(
                                sampleCode,
                                patientName,
                                patientAgeText.toIntOrNull() ?: 30,
                                pathogen,
                                biosafetyLevel,
                                viralLoad,
                                symptoms,
                                plainDiagnosis,
                                secretKey,
                                selectedAlgorithm,
                                epidemiologist
                            )
                            savedBannerRecord = saved
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Default.EnhancedEncryption, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isMobile) "Cifrar (${selectedAlgorithm.shortTag}) y Guardar"
                        else "Cifrar con ${selectedAlgorithm.shortTag} y Guardar en Base de Datos",
                        fontWeight = FontWeight.Bold
                    )
                }

                savedBannerRecord?.let { rec ->
                    Surface(
                        color = Color(0xFF052E24),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isMobile) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "✓ Expediente ${rec.sampleCode} cifrado y almacenado en la BD.",
                                    fontWeight = FontWeight.Bold,
                                    color = BioPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Puedes inspeccionar las matrices y llaves en el Crypto Lab.",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Button(
                                    onClick = { onOpenInCryptoLab(rec) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Abrir en Crypto Lab", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "✓ Expediente ${rec.sampleCode} cifrado y almacenado en la BD.",
                                        fontWeight = FontWeight.Bold,
                                        color = BioPrimary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Puedes inspeccionar las matrices y llaves en el Crypto Lab.",
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                                Button(onClick = { onOpenInCryptoLab(rec) }) {
                                    Text("Ver en Crypto Lab", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
