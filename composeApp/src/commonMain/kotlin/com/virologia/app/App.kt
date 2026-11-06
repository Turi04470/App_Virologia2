package com.virologia.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virologia.app.data.ViralRecord
import com.virologia.app.data.ViralRepository
import com.virologia.app.ui.CatalogScreen
import com.virologia.app.ui.CryptoLabScreen
import com.virologia.app.ui.NewSampleScreen
import com.virologia.app.ui.VirologiaTheme

enum class AppSection(val webTitle: String, val mobileTitle: String, val icon: ImageVector) {
    CATALOG("Expedientes Virales", "Expedientes", Icons.Default.FolderSpecial),
    NEW_SAMPLE("Nueva Muestra", "Nueva Muestra", Icons.Default.AddCircleOutline),
    CRYPTO_LAB("Crypto Lab (Álgebra/AES/RSA)", "Crypto Lab", Icons.Default.Science)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(isMobile: Boolean = false) {
    VirologiaTheme {
        val records by ViralRepository.records.collectAsState()
        var currentSection by remember { mutableStateOf(AppSection.CATALOG) }
        var inspectedRecord by remember { mutableStateOf<ViralRecord?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "App_Virologia • BioCrypto Lab",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isMobile) 15.sp else 17.sp
                                )
                                Text(
                                    text = if (isMobile) "AES-128 • RSA • Álgebra Lineal Z_256"
                                    else "Sistema Híbrido (Android + Web Wasm) • AES-128 | RSA | Cifrado Matricial Z_256",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AppSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = currentSection == section,
                            onClick = { currentSection = section },
                            icon = { Icon(section.icon, contentDescription = section.webTitle) },
                            label = {
                                Text(
                                    text = if (isMobile) section.mobileTitle else section.webTitle,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.widthIn(max = 1080.dp).fillMaxSize()) {
                    when (currentSection) {
                        AppSection.CATALOG -> {
                            CatalogScreen(
                                records = records,
                                isMobile = isMobile,
                                onInspectRecord = { record ->
                                    inspectedRecord = record
                                    currentSection = AppSection.CRYPTO_LAB
                                },
                                onDeleteRecord = { id ->
                                    ViralRepository.deleteRecord(id)
                                },
                                onResetSeed = {
                                    ViralRepository.resetToDefaultSeed()
                                },
                                onClearAll = {
                                    inspectedRecord = null
                                    ViralRepository.clearAllRecords()
                                }
                            )
                        }

                        AppSection.NEW_SAMPLE -> {
                            NewSampleScreen(
                                nextSampleIndex = records.size + 1,
                                isMobile = isMobile,
                                onSaveSample = { code, patient, age, pathogen, bsl, load, symptoms, diagnosis, key, alg, epi ->
                                    ViralRepository.addRecord(
                                        sampleCode = code,
                                        patientName = patient,
                                        patientAge = age,
                                        pathogen = pathogen,
                                        biosafetyLevel = bsl,
                                        viralLoadCopiesMl = load,
                                        symptoms = symptoms,
                                        plainDiagnosis = diagnosis,
                                        secretKey = key,
                                        algorithm = alg,
                                        epidemiologist = epi
                                    )
                                },
                                onOpenInCryptoLab = { saved ->
                                    inspectedRecord = saved
                                    currentSection = AppSection.CRYPTO_LAB
                                }
                            )
                        }

                        AppSection.CRYPTO_LAB -> {
                            CryptoLabScreen(
                                records = records,
                                selectedRecord = inspectedRecord ?: records.firstOrNull(),
                                isMobile = isMobile,
                                onSelectRecord = { rec -> inspectedRecord = rec }
                            )
                        }
                    }
                }
            }
        }
    }
}
