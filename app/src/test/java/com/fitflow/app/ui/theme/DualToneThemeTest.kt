package com.fitflow.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.fitflow.app.ui.settings.SettingsUiState
import org.junit.Assert.assertEquals
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
        assertEquals("System Default", ThemeMode.SYSTEM.displayName)
        assertEquals("Light Mode", ThemeMode.LIGHT.displayName)
        assertEquals("Dark Mode", ThemeMode.DARK.displayName)
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
        assertEquals(Color(0xFF0D0E12), AccentColor.EMERALD.onAccentColor)
        assertEquals(Color(0xFF0D0E12), AccentColor.CYAN.onAccentColor)
        assertEquals(Color(0xFFFFFFFF), AccentColor.VIOLET.onAccentColor)
        assertEquals(Color(0xFFFFFFFF), AccentColor.CORAL.onAccentColor)
        assertEquals(Color(0xFF0D0E12), AccentColor.AMBER.onAccentColor)
        assertEquals(Color(0xFFFFFFFF), AccentColor.ROSE.onAccentColor)
        assertEquals(Color(0xFFFFFFFF), AccentColor.BLUE.onAccentColor)
    }

    @Test
    fun testBuildDualToneColorSchemeDark() {
        val colorScheme = buildDualToneColorScheme(isDark = true, accent = AccentColor.CYAN)
        assertNotNull(colorScheme)
        assertEquals(AccentColor.CYAN.color, colorScheme.primary)
        assertEquals(AccentColor.CYAN.onAccentColor, colorScheme.onPrimary)
        assertEquals(DarkBackground, colorScheme.background)
        assertEquals(DarkSurface, colorScheme.surface)
    }

    @Test
    fun testBuildDualToneColorSchemeLight() {
        val colorScheme = buildDualToneColorScheme(isDark = false, accent = AccentColor.CORAL)
        assertNotNull(colorScheme)
        assertEquals(AccentColor.CORAL.color, colorScheme.primary)
        assertEquals(AccentColor.CORAL.onAccentColor, colorScheme.onPrimary)
        assertEquals(LightBackground, colorScheme.background)
        assertEquals(LightSurface, colorScheme.surface)
    }

    @Test
    fun testSettingsUiStateDefaults() {
        val defaultState = SettingsUiState()
        assertEquals(ThemeMode.SYSTEM, defaultState.themeMode)
        assertEquals(AccentColor.EMERALD, defaultState.selectedAccent)
    }
}
