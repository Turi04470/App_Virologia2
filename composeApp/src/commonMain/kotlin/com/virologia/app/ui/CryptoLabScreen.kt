package com.virologia.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virologia.app.crypto.*
import com.virologia.app.data.ViralRecord
import com.virologia.app.data.ViralRepository

@Composable
fun CryptoLabScreen(
    records: List<ViralRecord>,
    selectedRecord: ViralRecord?,
    isMobile: Boolean = false,
    onSelectRecord: (ViralRecord) -> Unit
) {
    val activeRecord = selectedRecord ?: records.firstOrNull()
    var activeAlgorithm by remember(activeRecord) {
        mutableStateOf(activeRecord?.algorithm ?: CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX)
    }
    var testKey by remember(activeRecord) {
        mutableStateOf(ViralRepository.DEFAULT_LAB_KEY)
    }

    val decryptedFromRecord = remember(activeRecord, testKey) {
        if (activeRecord == null) Result.failure(Exception("Sin expediente seleccionado"))
        else CryptoEngine.decryptDiagnosis(activeRecord.cipherTextHex, testKey, activeRecord.algorithm)
    }

    var customPlainText by remember(activeRecord, decryptedFromRecord) {
        mutableStateOf(
            decryptedFromRecord.getOrNull()
                ?: "RT-PCR Positivo para Filovirus Zaire. Aislamiento estricto."
        )
    }

    val signatureOk = remember(activeRecord, testKey) {
        if (activeRecord == null) false
        else CryptoEngine.verifyRecordSignature(
            cipherTextHex = activeRecord.cipherTextHex,
            signatureHex = activeRecord.rsaSignatureHex,
            epidemiologist = activeRecord.epidemiologist,
            secretKey = testKey
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Selector de Expediente Almacenado en BD
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "1. Seleccionar Expediente de la Base de Datos para Inspección",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        records.forEach { rec ->
                            val selected = activeRecord?.id == rec.id
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onSelectRecord(rec)
                                    activeAlgorithm = rec.algorithm
                                },
                                label = {
                                    Text("${rec.sampleCode} • ${rec.pathogen} (${rec.algorithm.shortTag})")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        records.forEach { rec ->
                            val selected = activeRecord?.id == rec.id
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onSelectRecord(rec)
                                    activeAlgorithm = rec.algorithm
                                },
                                label = {
                                    Text("${rec.sampleCode} • ${rec.pathogen.take(18)} (${rec.algorithm.shortTag})")
                                }
                            )
                        }
                    }
                }

                if (isMobile) {
                    OutlinedTextField(
                        value = testKey,
                        onValueChange = { testKey = it },
                        label = { Text("Llave Criptográfica Activa") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { testKey = ViralRepository.DEFAULT_LAB_KEY },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restaurar Llave de Laboratorio (${ViralRepository.DEFAULT_LAB_KEY})", fontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testKey,
                            onValueChange = { testKey = it },
                            label = { Text("Llave Criptográfica Activa (Prueba cambiarla para verificar fallo)") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = { testKey = ViralRepository.DEFAULT_LAB_KEY }) {
                            Text("Llave Lab (${ViralRepository.DEFAULT_LAB_KEY})", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Inspector del Criptograma Crudo en Base de Datos y Descifrado en Tiempo Real
        if (activeRecord != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isMobile) {
                        Text(
                            "2. Registro en Crudo en BD (${activeRecord.sampleCode})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = if (signatureOk) Color(0xFF052E24) else Color(0xFF3B0D18),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (signatureOk) "✓ Firma RSA Verificada (${activeRecord.rsaSignatureHex})"
                                else "✗ Firma RSA Inválida para esta llave",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (signatureOk) Color(0xFF4ADE80) else BioError,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "2. Registro en Crudo en Base de Datos (${activeRecord.sampleCode})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = if (signatureOk) Color(0xFF052E24) else Color(0xFF3B0D18),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (signatureOk) "✓ Firma RSA Verificada (${activeRecord.rsaSignatureHex})"
                                    else "✗ Firma RSA Inválida para esta llave",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (signatureOk) Color(0xFF4ADE80) else BioError,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070C15), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "ALGORITMO EN BD: ${activeRecord.algorithm.displayName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = algorithmBadgeColor(activeRecord.algorithm)
                        )
                        Text(
                            "HEXADECIMAL (${activeRecord.cipherTextHex.length / 2} bytes):\n${activeRecord.cipherTextHex}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            "BASE64:\n${activeRecord.cipherTextBase64}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Surface(
                        color = if (decryptedFromRecord.isSuccess) Color(0xFF052E24) else Color(0xFF3B0D18),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (decryptedFromRecord.isSuccess)
                                    "✓ RESULTADO DEL DESCIFRADO EN TIEMPO REAL:"
                                else "✗ DESCIFRADO RECHAZADO (LLAVE INCORRECTA):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (decryptedFromRecord.isSuccess) BioPrimary else BioError
                            )
                            Text(
                                text = decryptedFromRecord.getOrElse { it.message ?: "Error al descifrar" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Demostrador Matemático Paso a Paso
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "3. Demostración Matemática y Criptográfica Paso a Paso",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CryptoAlgorithm.entries.forEach { alg ->
                            FilterChip(
                                selected = activeAlgorithm == alg,
                                onClick = { activeAlgorithm = alg },
                                label = { Text(alg.displayName) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CryptoAlgorithm.entries.forEach { alg ->
                            FilterChip(
                                selected = activeAlgorithm == alg,
                                onClick = { activeAlgorithm = alg },
                                label = { Text(alg.displayName) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customPlainText,
                    onValueChange = { customPlainText = it },
                    label = { Text("Texto de Prueba para Inspección Matemática Paso a Paso") },
                    minLines = if (isMobile) 2 else 1,
                    modifier = Modifier.fillMaxWidth()
                )

                when (activeAlgorithm) {
                    CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX -> {
                        val details = remember(customPlainText, testKey) {
                            LinearAlgebraMatrixCipher.inspect(customPlainText, testKey)
                        }
                        LinearAlgebraInspectorSection(details = details, isMobile = isMobile)
                    }
                    CryptoAlgorithm.AES_128 -> {
                        val details = remember(customPlainText, testKey) {
                            AesCipher.inspect(customPlainText, testKey)
                        }
                        AesInspectorSection(details = details)
                    }
                    CryptoAlgorithm.RSA_ASYMMETRIC -> {
                        val details = remember(customPlainText, testKey) {
                            RsaCipher.inspect(customPlainText, testKey)
                        }
                        RsaInspectorSection(details = details, isMobile = isMobile)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinearAlgebraInspectorSection(
    details: MatrixInspectionDetails,
    isMobile: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Fundamento de Álgebra Lineal sobre el Anillo Modular Z_256:",
                    fontWeight = FontWeight.Bold,
                    color = BioPrimary,
                    fontSize = 13.sp
                )
                Text(
                    "• Cifrado por bloques de dimensión 3: c_i = (K · p_i + r_i) mod 256\n" +
                            "• Descifrado con matriz inversa: p_i = K⁻¹ · (c_i - r_i) mod 256\n" +
                            "• Condición de invertibilidad: mcd(det(K), 256) = ${details.gcdWith256} (Coprimos ✓)",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatPill("det(K) entero", "${details.determinantRaw}", Modifier.weight(1f))
                            StatPill("det(K) mod 256", "${details.determinantMod256}", Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatPill("mcd(det(K), 256)", "${details.gcdWith256}", Modifier.weight(1f))
                            StatPill("det(K)⁻¹ mod 256", "${details.detModularInverse}", Modifier.weight(1f))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatPill("det(K) entero", "${details.determinantRaw}")
                        StatPill("det(K) mod 256", "${details.determinantMod256}")
                        StatPill("mcd(det(K), 256)", "${details.gcdWith256}")
                        StatPill("det(K)⁻¹ mod 256", "${details.detModularInverse}")
                    }
                }
            }
        }

        // Visualización de las 3 Matrices 3x3: Vertical en móvil, Horizontal en web
        if (isMobile) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Matrix3x3Box(
                    title = "Matriz Clave K (mod 256)",
                    subtitle = "Generada de la llave secreta",
                    matrix = details.matrixK,
                    accent = BioPrimary,
                    fillWidth = true
                )
                Matrix3x3Box(
                    title = "Matriz Adjunta Adj(K)",
                    subtitle = "Cof(K)^T mod 256",
                    matrix = details.adjugateMatrix,
                    accent = BioSecondary,
                    fillWidth = true
                )
                Matrix3x3Box(
                    title = "Matriz Inversa K⁻¹",
                    subtitle = "${details.detModularInverse} · Adj(K) mod 256",
                    matrix = details.inverseMatrixK,
                    accent = BioTertiary,
                    fillWidth = true
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Matrix3x3Box(
                    title = "Matriz Clave K (mod 256)",
                    subtitle = "Generada de la llave",
                    matrix = details.matrixK,
                    accent = BioPrimary,
                    fillWidth = false
                )
                Matrix3x3Box(
                    title = "Matriz Adjunta Adj(K)",
                    subtitle = "Cof(K)^T mod 256",
                    matrix = details.adjugateMatrix,
                    accent = BioSecondary,
                    fillWidth = false
                )
                Matrix3x3Box(
                    title = "Matriz Inversa K⁻¹",
                    subtitle = "${details.detModularInverse} · Adj(K) mod 256",
                    matrix = details.inverseMatrixK,
                    accent = BioTertiary,
                    fillWidth = false
                )
            }
        }

        Text(
            "Traza de Multiplicación Matricial por Bloques (Primeros bloques de 3 bytes):",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary
        )

        details.blockTraces.forEach { trace ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070C15), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Bloque #${trace.blockIndex} • Caracteres ASCII: \"${trace.plainChars}\"",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BioPrimary
                )
                if (isMobile) {
                    Text(
                        "• Vector Plano p_${trace.blockIndex} = ${trace.plainVector}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        "• Producto K·p_${trace.blockIndex} mod 256 = ${trace.multipliedVector}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        "• Rotación r_${trace.blockIndex} = ${trace.rotationVector}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        "• Cifrado c_${trace.blockIndex} = (K·p + r) mod 256 = ${trace.cipherVector}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    )
                } else {
                    Text(
                        "Vector Plano p_${trace.blockIndex} = ${trace.plainVector}  -->  " +
                                "K · p_${trace.blockIndex} mod 256 = ${trace.multipliedVector}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        "Rotación r_${trace.blockIndex} = ${trace.rotationVector}  -->  " +
                                "Vector Cifrado c_${trace.blockIndex} = (K·p + r) mod 256 = ${trace.cipherVector}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}

@Composable
private fun Matrix3x3Box(
    title: String,
    subtitle: String,
    matrix: List<List<Int>>,
    accent: Color,
    fillWidth: Boolean
) {
    Surface(
        color = Color(0xFF070C15),
        shape = RoundedCornerShape(12.dp),
        modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(235.dp))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = accent)
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            matrix.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { value ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF131C2E), RoundedCornerShape(6.dp))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value.toString().padStart(3, ' '),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AesInspectorSection(details: AesInspectionDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF070C15), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Parámetros Internos AES-128 (Modo CBC + PKCS#7):",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = BioSecondary
        )
        Text("Polinomio Irreducible GF(2^8): ${details.gfPolynomial}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Text("Llave Derivada 128-bit (Hex):\n${details.keyHex}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Text("Vector de Inicialización IV:\n${details.ivHex}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Text("Bloques de 128 bits procesados: ${details.totalBlocks}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text("Expansión de Sub-llaves de Ronda (Key Schedule 44 palabras):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BioPrimary)
        details.roundKeysPreview.forEach { rk ->
            Text(rk, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun RsaInspectorSection(
    details: RsaInspectionDetails,
    isMobile: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF070C15), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Parámetros Aritméticos RSA (Par de Llaves Asimétricas):",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = BioTertiary
        )
        if (isMobile) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatPill("Primo p", "${details.primeP}", Modifier.weight(1f))
                    StatPill("Primo q", "${details.primeQ}", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatPill("Módulo n = p·q", "${details.modulusN}", Modifier.weight(1f))
                    StatPill("Totiente φ(n)", "${details.totientPhi}", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatPill("Pública e", "${details.publicExponentE}", Modifier.weight(1f))
                    StatPill("Privada d", "${details.privateExponentD}", Modifier.weight(1f))
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill("Primo p", "${details.primeP}")
                StatPill("Primo q", "${details.primeQ}")
                StatPill("Módulo n = p·q", "${details.modulusN}")
                StatPill("Totiente φ(n)", "${details.totientPhi}")
                StatPill("Pública e", "${details.publicExponentE}")
                StatPill("Privada d", "${details.privateExponentD}")
            }
        }
        Text(
            "Verificación de Identidad de Euler: (e · d) mod φ(n) = ${(details.publicExponentE * details.privateExponentD) % details.totientPhi}",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFF4ADE80)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("Exponenciación Modular por Bloques (C = M^e mod n  |  M = C^d mod n):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        details.sampleBlocks.forEach { line ->
            Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFCBD5E1))
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF131C2E),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        }
    }
}
