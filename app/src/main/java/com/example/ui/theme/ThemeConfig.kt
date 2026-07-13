package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class LoveThemeColors(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val textDark: Color,
    val bgLight: Color,
    val cardBg: Color,
    val alertBg: Color,
    val alertBorder: Color,
    val darkAccent: Color = Color(0xFF1D1B1B)
)

object ThemePresets {
    val RoseRomance = LoveThemeColors(
        name = "Rose Romance 🌹",
        primary = Color(0xFFE11D48),
        secondary = Color(0xFFFB7185),
        tertiary = Color(0xFFFBCFE8),
        textDark = Color(0xFF4C0519),
        bgLight = Color(0xFFFFF1F2),
        cardBg = Color(0xFFFFF8F8),
        alertBg = Color(0xFFFEE2E2),
        alertBorder = Color(0xFFFECACA)
    )

    val LavenderLove = LoveThemeColors(
        name = "Lavender Love 💜",
        primary = Color(0xFF7C3AED),
        secondary = Color(0xFFA78BFA),
        tertiary = Color(0xFFDDD6FE),
        textDark = Color(0xFF1E1B4B),
        bgLight = Color(0xFFF5F3FF),
        cardBg = Color(0xFFFAF5FF),
        alertBg = Color(0xFFEDE9FE),
        alertBorder = Color(0xFFDDD6FE)
    )

    val SunsetRomance = LoveThemeColors(
        name = "Sunset Romance 🌅",
        primary = Color(0xFFEA580C),
        secondary = Color(0xFFFDBA74),
        tertiary = Color(0xFFFFEDD5),
        textDark = Color(0xFF431407),
        bgLight = Color(0xFFFFF7ED),
        cardBg = Color(0xFFFFFDFA),
        alertBg = Color(0xFFFFEDD5),
        alertBorder = Color(0xFFFED7AA)
    )

    val OceanCrush = LoveThemeColors(
        name = "Ocean Crush 🌊",
        primary = Color(0xFF0D9488),
        secondary = Color(0xFF2DD4BF),
        tertiary = Color(0xFF99F6E4),
        textDark = Color(0xFF115E59),
        bgLight = Color(0xFFF0FDFA),
        cardBg = Color(0xFFF5FFFD),
        alertBg = Color(0xFFCCFBF1),
        alertBorder = Color(0xFF99F6E4)
    )

    val GothicValentine = LoveThemeColors(
        name = "Gothic Valentine 🖤",
        primary = Color(0xFFE11D48),
        secondary = Color(0xFFFB7185),
        tertiary = Color(0xFF4C0519),
        textDark = Color(0xFFFFF1F2),
        bgLight = Color(0xFF0F0509),
        cardBg = Color(0xFF1E0A14),
        alertBg = Color(0xFF2E0F1E),
        alertBorder = Color(0xFF4C0519)
    )

    val list = listOf(RoseRomance, LavenderLove, SunsetRomance, OceanCrush, GothicValentine)

    fun getThemeByName(name: String): LoveThemeColors {
        return list.find { it.name == name } ?: RoseRomance
    }

    /**
     * Fallback semantic customizer in case Gemini is offline or API key isn't set.
     */
    fun generateLocalSemanticTheme(prompt: String): LoveThemeColors {
        val p = prompt.lowercase()
        return when {
            p.contains("green") || p.contains("mint") || p.contains("forest") || p.contains("leaf") -> LoveThemeColors(
                name = "AI: Mint Forest 🌿",
                primary = Color(0xFF059669),
                secondary = Color(0xFF34D399),
                tertiary = Color(0xFFA7F3D0),
                textDark = Color(0xFF064E3B),
                bgLight = Color(0xFFECFDF5),
                cardBg = Color(0xFFF6FFF9),
                alertBg = Color(0xFFD1FAE5),
                alertBorder = Color(0xFFA7F3D0)
            )
            p.contains("gold") || p.contains("yellow") || p.contains("honey") || p.contains("sun") || p.contains("lemon") -> LoveThemeColors(
                name = "AI: Golden Honey 🍯",
                primary = Color(0xFFD97706),
                secondary = Color(0xFFFBBF24),
                tertiary = Color(0xFFFEF3C7),
                textDark = Color(0xFF78350F),
                bgLight = Color(0xFFFFFBEB),
                cardBg = Color(0xFFFFFFFD),
                alertBg = Color(0xFFFEF3C7),
                alertBorder = Color(0xFFFDE68A)
            )
            p.contains("blue") || p.contains("sky") || p.contains("bubble") || p.contains("aqua") || p.contains("ice") -> LoveThemeColors(
                name = "AI: Bubblegum Sky 💎",
                primary = Color(0xFF0284C7),
                secondary = Color(0xFF38BDF8),
                tertiary = Color(0xFFBAE6FD),
                textDark = Color(0xFF0C4A6E),
                bgLight = Color(0xFFF0F9FF),
                cardBg = Color(0xFFF7FCFF),
                alertBg = Color(0xFFE0F2FE),
                alertBorder = Color(0xFFBAE6FD)
            )
            p.contains("dark") || p.contains("goth") || p.contains("black") || p.contains("night") || p.contains("vampire") -> LoveThemeColors(
                name = "AI: Gothic Night 🧛",
                primary = Color(0xFFE11D48),
                secondary = Color(0xFFF43F5E),
                tertiary = Color(0xFF881337),
                textDark = Color(0xFFFFF1F2),
                bgLight = Color(0xFF0C0206),
                cardBg = Color(0xFF1A050E),
                alertBg = Color(0xFF2D0715),
                alertBorder = Color(0xFF4C0519)
            )
            p.contains("orange") || p.contains("peach") || p.contains("fire") || p.contains("coral") -> LoveThemeColors(
                name = "AI: Coral Sunset 🍑",
                primary = Color(0xFFEA580C),
                secondary = Color(0xFFFDBA74),
                tertiary = Color(0xFFFFEDD5),
                textDark = Color(0xFF431407),
                bgLight = Color(0xFFFFF7ED),
                cardBg = Color(0xFFFFFDFA),
                alertBg = Color(0xFFFFEDD5),
                alertBorder = Color(0xFFFED7AA)
            )
            p.contains("purple") || p.contains("grape") || p.contains("orchid") || p.contains("violet") -> LoveThemeColors(
                name = "AI: Royal Lavender 🍇",
                primary = Color(0xFF7C3AED),
                secondary = Color(0xFFA78BFA),
                tertiary = Color(0xFFDDD6FE),
                textDark = Color(0xFF1E1B4B),
                bgLight = Color(0xFFF5F3FF),
                cardBg = Color(0xFFFAF5FF),
                alertBg = Color(0xFFEDE9FE),
                alertBorder = Color(0xFFDDD6FE)
            )
            else -> {
                // Generate a unique dynamic pink shade using prompt's length or characteristics
                val hueShift = (prompt.length * 12) % 360
                LoveThemeColors(
                    name = "AI: Customized Pink 💖",
                    primary = Color(0xFFBE123C),
                    secondary = Color(0xFFFB7185),
                    tertiary = Color(0xFFFBCFE8),
                    textDark = Color(0xFF4C0519),
                    bgLight = Color(0xFFFFF1F2),
                    cardBg = Color(0xFFFFF8F8),
                    alertBg = Color(0xFFFEE2E2),
                    alertBorder = Color(0xFFFECACA)
                )
            }
        }
    }
}
