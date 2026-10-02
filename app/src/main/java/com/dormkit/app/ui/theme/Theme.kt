package com.dormkit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dormkit.app.model.StorageLocation

private val LightColors = lightColorScheme(
    primary = Color(0xFF397A58),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F2E1),
    onPrimaryContainer = Color(0xFF0B3B25),
    secondary = Color(0xFF3266B0),
    secondaryContainer = Color(0xFFDCE8FF),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    background = Color(0xFFF9FAF7),
    surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFE9EEE9)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BD5AF),
    onPrimary = Color(0xFF08391F),
    primaryContainer = Color(0xFF20513A),
    secondary = Color(0xFFA9C7FF),
    secondaryContainer = Color(0xFF234B80),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF73000A),
    background = Color(0xFF101512),
    surface = Color(0xFF171D19),
    surfaceVariant = Color(0xFF29312C)
)

private val RoomLightColors = lightColorScheme(
    primary = Color(0xFF4266A6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E3FF),
    onPrimaryContainer = Color(0xFF102A56),
    secondary = Color(0xFF74558F),
    secondaryContainer = Color(0xFFF0DBFF),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    background = Color(0xFFF7F8FF),
    surface = Color(0xFFFBF8FF),
    surfaceVariant = Color(0xFFE4E8F3)
)

private val RoomDarkColors = darkColorScheme(
    primary = Color(0xFFADC6FF),
    onPrimary = Color(0xFF0D3264),
    primaryContainer = Color(0xFF294D82),
    secondary = Color(0xFFDDB8F8),
    secondaryContainer = Color(0xFF593E70),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF73000A),
    background = Color(0xFF111318),
    surface = Color(0xFF191B22),
    surfaceVariant = Color(0xFF2B303B)
)

@Composable
fun DormKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    storageLocation: StorageLocation = StorageLocation.DORM,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when (storageLocation) {
            StorageLocation.DORM -> if (darkTheme) DarkColors else LightColors
            StorageLocation.ROOM -> if (darkTheme) RoomDarkColors else RoomLightColors
        },
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
