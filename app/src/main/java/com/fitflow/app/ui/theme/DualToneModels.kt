package com.fitflow.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Supported base theme modes.
 */
enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode");

    companion object {
        fun fromName(name: String?): ThemeMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
        }
    }
}

/**
 * Curated high-contrast accent colors for the dual-tone theme system.
 * Each accent color provides a WCAG-compliant onAccent color for text/icons placed on top of it.
 */
enum class AccentColor(
    val id: String,
    val displayName: String,
    val color: Color,
    val onAccentColor: Color
) {
    EMERALD(
        id = "emerald",
        displayName = "Emerald",
        color = Color(0xFF00E676),
        onAccentColor = Color(0xFF0D0E12)
    ),
    CYAN(
        id = "cyan",
        displayName = "Electric Cyan",
        color = Color(0xFF00E5FF),
        onAccentColor = Color(0xFF0D0E12)
    ),
    VIOLET(
        id = "violet",
        displayName = "Neon Violet",
        color = Color(0xFF7C4DFF),
        onAccentColor = Color(0xFFFFFFFF)
    ),
    CORAL(
        id = "coral",
        displayName = "Sunset Coral",
        color = Color(0xFFFF6E40),
        onAccentColor = Color(0xFFFFFFFF)
    ),
    AMBER(
        id = "amber",
        displayName = "Electric Amber",
        color = Color(0xFFFFD600),
        onAccentColor = Color(0xFF0D0E12)
    ),
    ROSE(
        id = "rose",
        displayName = "Hot Rose",
        color = Color(0xFFFF4081),
        onAccentColor = Color(0xFFFFFFFF)
    ),
    BLUE(
        id = "blue",
        displayName = "Cobalt Blue",
        color = Color(0xFF2979FF),
        onAccentColor = Color(0xFFFFFFFF)
    );

    companion object {
        val DEFAULT = EMERALD

        fun fromId(id: String?): AccentColor {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
