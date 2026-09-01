package com.fitflow.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.fitflow.app.ui.settings.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DualToneThemeTest {

    @Test
    fun testThemeModeEnumValues() {
        assertEquals("SYSTEM", ThemeMode.SYSTEM.name)
        assertEquals("LIGHT", ThemeMode.LIGHT.name)
        assertEquals("DARK", ThemeMode.DARK.name)
        assertEquals(3, ThemeMode.entries.size)
    }

    @Test
    fun testThemeModeLabelFormatting() {
        assertEquals("System", ThemeMode.SYSTEM.displayName)
        assertEquals("Light", ThemeMode.LIGHT.displayName)
        assertEquals("Dark", ThemeMode.DARK.displayName)
    }

    @Test
    fun testAccentColorsDefinition() {
        assertEquals(7, AccentColor.entries.size)
        assertTrue(AccentColor.entries.contains(AccentColor.EMERALD))
        assertTrue(AccentColor.entries.contains(AccentColor.CYAN))
        assertTrue(AccentColor.entries.contains(AccentColor.VIOLET))
        assertTrue(AccentColor.entries.contains(AccentColor.CORAL))
        assertTrue(AccentColor.entries.contains(AccentColor.AMBER))
        assertTrue(AccentColor.entries.contains(AccentColor.ROSE))
        assertTrue(AccentColor.entries.contains(AccentColor.BLUE))
    }

    @Test
    fun testAccentColorHighContrastOnColor() {
        // Emerald is a bright accent, onAccent should be dark (0xFF000000 or close)
        assertEquals(Color(0xFF003822), AccentColor.EMERALD.onColor)
        assertEquals(Color(0xFF00363D), AccentColor.CYAN.onColor)
        assertEquals(Color(0xFFFFFFFF), AccentColor.VIOLET.onColor)
        assertEquals(Color(0xFF3B0900), AccentColor.CORAL.onColor)
        assertEquals(Color(0xFF3D2E00), AccentColor.AMBER.onColor)
        assertEquals(Color(0xFFFFFFFF), AccentColor.ROSE.onColor)
        assertEquals(Color(0xFF00325B), AccentColor.BLUE.onColor)
    }

    @Test
    fun testBuildDualToneColorSchemeDark() {
        val colorScheme = buildDualToneColorScheme(isDark = true, accent = AccentColor.CYAN)
        assertNotNull(colorScheme)
        assertEquals(AccentColor.CYAN.color, colorScheme.primary)
        assertEquals(AccentColor.CYAN.onColor, colorScheme.onPrimary)
        assertEquals(BaseDarkBackground, colorScheme.background)
        assertEquals(BaseDarkSurface, colorScheme.surface)
    }

    @Test
    fun testBuildDualToneColorSchemeLight() {
        val colorScheme = buildDualToneColorScheme(isDark = false, accent = AccentColor.CORAL)
        assertNotNull(colorScheme)
        assertEquals(AccentColor.CORAL.color, colorScheme.primary)
        assertEquals(AccentColor.CORAL.onColor, colorScheme.onPrimary)
        assertEquals(BaseLightBackground, colorScheme.background)
        assertEquals(BaseLightSurface, colorScheme.surface)
    }

    @Test
    fun testSettingsUiStateDefaults() {
        val defaultState = SettingsUiState()
        assertEquals(ThemeMode.SYSTEM, defaultState.themeMode)
        assertEquals(AccentColor.EMERALD, defaultState.accentColor)
    }
}
