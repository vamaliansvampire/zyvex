package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Royal Black Palette
val RoyalBlack900 = Color(0xFF060608) // Deepest OLED black
val RoyalBlack800 = Color(0xFF0C0D11) // Main background
val RoyalBlack700 = Color(0xFF13151C) // Surface elevated
val RoyalBlack600 = Color(0xFF1A1D27) // Card / Surface container
val RoyalBlack500 = Color(0xFF242836) // Stroke / Divider
val RoyalBlack400 = Color(0xFF33384A) // Active outline

// Metallic Gold Palette (Iron Man inspired)
val MetallicGold50 = Color(0xFFFFFBE8)  // Core flare / text highlight
val MetallicGold100 = Color(0xFFFFF3B8) // Bright gold glow
val MetallicGold200 = Color(0xFFFFE47A) // Highlight
val MetallicGold300 = Color(0xFFFFD700) // Pure Gold
val MetallicGold400 = Color(0xFFF3C326) // Classic Amber Gold
val MetallicGold500 = Color(0xFFDAA520) // Deep Metallic Gold
val MetallicGold600 = Color(0xFFB8860B) // Dark Gold accent
val MetallicGold700 = Color(0xFF8B6508) // Shadow gold
val MetallicGold800 = Color(0xFF5C4300) // Deep amber border

// HUD Cyan / Teal Telemetry Accents (Iron Man HUD secondary)
val HudCyan = Color(0xFF00E5FF)
val HudCyanGlow = Color(0x6600E5FF)
val HudRedAlert = Color(0xFFFF3344)
val HudGreenNormal = Color(0xFF00E676)

// Gold Gradients
val GoldLinearGradient = Brush.linearGradient(
    colors = listOf(MetallicGold200, MetallicGold400, MetallicGold600)
)
val GoldGlowRadial = Brush.radialGradient(
    colors = listOf(MetallicGold100.copy(alpha = 0.8f), MetallicGold400.copy(alpha = 0.3f), Color.Transparent)
)
val ArcReactorCoreBrush = Brush.radialGradient(
    colors = listOf(Color.White, MetallicGold200, MetallicGold500, RoyalBlack800)
)
