package com.virologia.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.virologia.app.crypto.CryptoAlgorithm
import com.virologia.app.data.BiosafetyLevel

val BioPrimary = Color(0xFF00E5B0)
val BioSecondary = Color(0xFF38BDF8)
val BioTertiary = Color(0xFFA78BFA)
val BioBackground = Color(0xFF0B111E)
val BioSurface = Color(0xFF131C2E)
val BioSurfaceVariant = Color(0xFF1C2942)
val BioError = Color(0xFFFB7185)

private val VirologyColorScheme = darkColorScheme(
    primary = BioPrimary,
    onPrimary = Color(0xFF002B20),
    primaryContainer = Color(0xFF004D3B),
    onPrimaryContainer = Color(0xFF9FFCE1),
    secondary = BioSecondary,
    onSecondary = Color(0xFF002A40),
    secondaryContainer = Color(0xFF0B3D59),
    onSecondaryContainer = Color(0xFFBFE9FF),
    tertiary = BioTertiary,
    background = BioBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = BioSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = BioSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = BioError
)

@Composable
fun VirologiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VirologyColorScheme,
        content = content
    )
}

fun bslBadgeColor(level: BiosafetyLevel): Color = when (level) {
    BiosafetyLevel.BSL_1 -> Color(0xFF22C55E)
    BiosafetyLevel.BSL_2 -> Color(0xFFEAB308)
    BiosafetyLevel.BSL_3 -> Color(0xFFF97316)
    BiosafetyLevel.BSL_4 -> Color(0xFFEF4444)
}

fun algorithmBadgeColor(algorithm: CryptoAlgorithm): Color = when (algorithm) {
    CryptoAlgorithm.LINEAR_ALGEBRA_MATRIX -> Color(0xFF00E5B0)
    CryptoAlgorithm.AES_128 -> Color(0xFF38BDF8)
    CryptoAlgorithm.RSA_ASYMMETRIC -> Color(0xFFA78BFA)
}
