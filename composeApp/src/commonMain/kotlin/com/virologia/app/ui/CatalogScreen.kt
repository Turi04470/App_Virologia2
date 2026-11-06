package com.virologia.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.virologia.app.data.PlatformDatabase
import com.virologia.app.data.ViralRecord
import com.virologia.app.data.ViralRepository

@Composable
fun CatalogScreen(
    records: List<ViralRecord>,
    isMobile: Boolean = false,
    onInspectRecord: (ViralRecord) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onResetSeed: () -> Unit,
    onClearAll: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf<CryptoAlgorithm?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var globalUnlockKey by remember { mutableStateOf(ViralRepository.DEFAULT_LAB_KEY) }

    val filteredRecords = remember(records, selectedFilter, searchQuery) {
        records.filter { rec ->
            val matchesFilter = selectedFilter == null || rec.algorithm == selectedFilter
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                    rec.sampleCode.lowercase().contains(q) ||
                    rec.pathogen.lowercase().contains(q) ||
                    rec.patientName.lowercase().contains(q)
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DatabaseBannerCard(
                recordCount = records.size,
                isMobile = isMobile,
                onResetSeed = onResetSeed,
                onClearAll = onClearAll
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isMobile) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar muestra, patógeno o paciente") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = globalUnlockKey,
                            onValueChange = { globalUnlockKey = it },
                            label = { Text("Llave de Descifrado Activa") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar muestra, patógeno o paciente") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = globalUnlockKey,
                                onValueChange = { globalUnlockKey = it },
                                label = { Text("Llave de Descifrado") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.widthIn(min = 160.dp, max = 220.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("Todos (${records.size})") }
                        )
                        CryptoAlgorithm.entries.forEach { alg ->
                            val count = records.count { it.algorithm == alg }
                            FilterChip(
                                selected = selectedFilter == alg,
                                onClick = { selectedFilter = if (selectedFilter == alg) null else alg },
                                label = { Text("${alg.shortTag} ($count)") }
                            )
                        }
                    }
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se encontraron expedientes virales con el filtro actual.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                ViralRecordCard(
                    record = record,
                    unlockKey = globalUnlockKey,
                    isMobile = isMobile,
                    onInspect = { onInspectRecord(record) },
                    onDelete = { onDeleteRecord(record.id) }
                )
            }
        }
    }
}

@Composable
private fun DatabaseBannerCard(
    recordCount: Int,
    isMobile: Boolean,
    onResetSeed: () -> Unit,
    onClearAll: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isMobile) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = PlatformDatabase.getDatabaseStatusLabel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "$recordCount expedientes almacenados con cifrado activo\nLlave por defecto: ${ViralRepository.DEFAULT_LAB_KEY}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onResetSeed,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restaurar Expedientes Demo", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limpiar Todo (Sin Demo / Llenado Manual)", fontSize = 12.sp)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = PlatformDatabase.getDatabaseStatusLabel(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$recordCount expedientes almacenados con cifrado activo • Llave por defecto: ${ViralRepository.DEFAULT_LAB_KEY}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    OutlinedButton(onClick = onResetSeed) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restaurar Demo", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Limpiar Todo (Sin Demo)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ViralRecordCard(
    record: ViralRecord,
    unlockKey: String,
    isMobile: Boolean,
    onInspect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDecrypted by remember { mutableStateOf(false) }
    val decryptResult = remember(record.cipherTextHex, unlockKey, record.algorithm) {
        CryptoEngine.decryptDiagnosis(record.cipherTextHex, unlockKey, record.algorithm)
    }
    val signatureValid = remember(record.cipherTextHex, record.rsaSignatureHex, record.epidemiologist, unlockKey) {
        CryptoEngine.verifyRecordSignature(
            cipherTextHex = record.cipherTextHex,
            signatureHex = record.rsaSignatureHex,
            epidemiologist = record.epidemiologist,
            secretKey = unlockKey
        )
    }

    val bslColor = bslBadgeColor(record.biosafetyLevel)
    val algColor = algorithmBadgeColor(record.algorithm)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, algColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.sampleCode,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = algColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = record.algorithm.shortTag,
                                color = algColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Surface(
                        color = bslColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, bslColor, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "${record.biosafetyLevel.code} • ${record.biosafetyLevel.riskLabel}",
                            color = bslColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = bslColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, bslColor, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "${record.biosafetyLevel.code} • ${record.biosafetyLevel.riskLabel}",
                                color = bslColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = algColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = record.algorithm.shortTag,
                                color = algColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = record.sampleCode,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = record.pathogen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isMobile) {
                    Text(
                        text = "Paciente: ${record.patientName} (${record.patientAge} años)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Carga Viral: ${record.viralLoadCopiesMl}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Paciente: ${record.patientName} (${record.patientAge} años) • Carga Viral: ${record.viralLoadCopiesMl}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Cuadro clínico: ${record.symptoms}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bloque Cifrado en Base de Datos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070C15), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isMobile) {
                    Text(
                        text = "CIPHERTEXT EN BD (${record.algorithm.id}):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = algColor
                    )
                    Text(
                        text = if (signatureValid) "✓ Firma RSA Válida (${record.rsaSignatureHex})"
                        else "✗ Firma RSA No Coincide (${record.rsaSignatureHex})",
                        fontSize = 10.sp,
                        color = if (signatureValid) Color(0xFF4ADE80) else MaterialTheme.colorScheme.error,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CIPHERTEXT EN BD (${record.algorithm.id}):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = algColor
                        )
                        Text(
                            text = if (signatureValid) "✓ Firma RSA Válida (${record.rsaSignatureHex})"
                            else "✗ Firma RSA No Coincide (${record.rsaSignatureHex})",
                            fontSize = 10.sp,
                            color = if (signatureValid) Color(0xFF4ADE80) else MaterialTheme.colorScheme.error,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = "HEX: ${record.cipherTextHex.take(72)}${if (record.cipherTextHex.length > 72) "..." else ""}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "B64: ${record.cipherTextBase64.take(72)}${if (record.cipherTextBase64.length > 72) "..." else ""}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (showDecrypted) {
                Surface(
                    color = if (decryptResult.isSuccess) Color(0xFF052E24) else Color(0xFF3B0D18),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (decryptResult.isSuccess) "DIAGNÓSTICO CONFIDENCIAL DESCIFRADO:"
                            else "ERROR DE DESCIFRADO CON LLAVE '$unlockKey':",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (decryptResult.isSuccess) BioPrimary else BioError
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = decryptResult.getOrElse { it.message ?: "Llave inválida" },
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (isMobile) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Autorizó: ${record.epidemiologist} • ${record.createdAt}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onInspect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inspeccionar en Crypto Lab", fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDecrypted = !showDecrypted },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (showDecrypted) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showDecrypted) "Ocultar Diagnóstico" else "Descifrar Diagnóstico", fontSize = 12.sp)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Autorizó: ${record.epidemiologist} • ${record.createdAt}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showDecrypted = !showDecrypted },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                if (showDecrypted) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showDecrypted) "Ocultar" else "Descifrar", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onInspect,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crypto Lab", fontSize = 12.sp)
                        }

                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
