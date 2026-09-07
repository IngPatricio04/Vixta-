package com.vixta.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Tema de Vixta.
 *
 * Dos cambios respecto a la plantilla que genera Android Studio:
 *
 * 1. Los colores son los del mockup del equipo, no los morados por defecto.
 * 2. dynamicColor esta APAGADO. Con esa opcion encendida, Android 12 en adelante
 *    toma los colores del fondo de pantalla del usuario e ignora la paleta. La app
 *    se veria distinta en cada telefono y nunca como el mockup.
 *
 * El mockup esta disenado en claro. El esquema oscuro de abajo es una derivacion
 * conservadora hecha con los mismos colores, no un diseno aparte: si el equipo
 * decide un modo oscuro propio, se rehace aqui.
 */

private val EsquemaClaro = lightColorScheme(
    primary = VixtaAzulMarino,
    onPrimary = VixtaBlanco,
    primaryContainer = VixtaSuperficie,
    onPrimaryContainer = VixtaAzulProfundo,

    secondary = VixtaTurquesa,
    onSecondary = VixtaBlanco,
    secondaryContainer = VixtaOkFondo,
    onSecondaryContainer = VixtaAzulMarino,

    tertiary = VixtaOk,
    onTertiary = VixtaBlanco,
    tertiaryContainer = VixtaAvisoFondo,
    onTertiaryContainer = VixtaAzulMarino,

    background = VixtaFondo,
    onBackground = VixtaAzulMarino,

    surface = VixtaBlanco,
    onSurface = VixtaAzulMarino,
    surfaceVariant = VixtaSuperficie,
    onSurfaceVariant = VixtaTextoSuave,

    outline = VixtaTextoTenue,
    outlineVariant = VixtaBorde,

    error = VixtaError,
    onError = VixtaBlanco,
    errorContainer = VixtaErrorFondo,
    onErrorContainer = VixtaError
)

private val EsquemaOscuro = darkColorScheme(
    primary = VixtaTurquesaClaro,
    onPrimary = VixtaAzulProfundo,
    primaryContainer = VixtaAzulMarino,
    onPrimaryContainer = VixtaSuperficie,

    secondary = VixtaTurquesa,
    onSecondary = VixtaAzulProfundo,
    secondaryContainer = VixtaAzulMarino,
    onSecondaryContainer = VixtaSuperficie,

    tertiary = VixtaOk,
    onTertiary = VixtaAzulProfundo,

    background = VixtaAzulProfundo,
    onBackground = VixtaSuperficie,

    surface = VixtaAzulMarino,
    onSurface = VixtaSuperficie,
    surfaceVariant = VixtaAzulMarino,
    onSurfaceVariant = VixtaTextoTenue,

    outline = VixtaTextoSuave,

    error = VixtaError,
    onError = VixtaBlanco
)

@Composable
fun VixtaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
