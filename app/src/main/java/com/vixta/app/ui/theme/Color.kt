package com.vixta.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de Vixta.
 *
 * Los colores salen del mockup del equipo: docs/mockups/Vixta - Mockups e Mapa.pdf
 * Antes de esto el archivo tenia la plantilla morada que genera Android Studio
 * (Purple80, Purple40...), que no corresponde al diseno aprobado.
 *
 * Si un color no esta aqui, no se usa. No se escriben hex sueltos en las pantallas.
 */

// --- Marca ---
/** Azul marino. Es el color dominante del mockup: barras, encabezados, texto principal. */
val VixtaAzulMarino = Color(0xFF0E1A45)

/** Azul marino mas oscuro. Barra de estado y superficies elevadas en modo oscuro. */
val VixtaAzulProfundo = Color(0xFF0A1230)

/** Turquesa. El acento de la marca: botones principales, enlaces, elementos activos. */
val VixtaTurquesa = Color(0xFF14B8A6)

/** Turquesa claro. Estados presionados y acento sobre fondo oscuro. */
val VixtaTurquesaClaro = Color(0xFF1FD1BE)

// --- Superficies ---
/** Fondo general de pantalla. */
val VixtaFondo = Color(0xFFF6F8FC)

/** Tarjetas, campos de formulario y separadores suaves. */
val VixtaSuperficie = Color(0xFFE2E7F1)

/** Borde de tarjeta y lineas divisorias. */
val VixtaBorde = Color(0xFFDFE4EE)

// --- Texto ---
/** Texto secundario: subtitulos, etiquetas, descripciones. */
val VixtaTextoSuave = Color(0xFF4A5568)

/** Texto terciario: pistas, marcas de agua, texto deshabilitado. */
val VixtaTextoTenue = Color(0xFF9AA5B8)

// --- Estados ---
/** Verde de estado correcto: inspeccion aprobada, sincronizacion completada. */
val VixtaOk = Color(0xFF22B07D)

/** Fondo del estado correcto. */
val VixtaOkFondo = Color(0xFFE4F8EF)

/** Fondo de advertencia: pendiente de sincronizar, temperatura al limite. */
val VixtaAvisoFondo = Color(0xFFFEF3DD)

/**
 * Rojo de error. No aparece en el mockup, asi que se toma el rojo estandar de
 * Material 3 en lugar de inventar uno. Si el equipo define uno propio, se cambia aqui.
 */
val VixtaError = Color(0xFFB3261E)
val VixtaErrorFondo = Color(0xFFF9DEDC)

val VixtaBlanco = Color(0xFFFFFFFF)
