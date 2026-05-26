package com.beszel.android.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AccentColor { Teal, Blue, Purple, Rose, Amber }

fun buildColorScheme(dark: Boolean, accent: AccentColor): ColorScheme {
    val primary = when (accent) {
        AccentColor.Teal   -> if (dark) TealPrimaryDark else TealPrimary
        AccentColor.Blue   -> if (dark) BluePrimaryDark else BluePrimary
        AccentColor.Purple -> if (dark) PurplePrimaryDark else PurplePrimary
        AccentColor.Rose   -> if (dark) RosePrimaryDark else RosePrimary
        AccentColor.Amber  -> if (dark) AmberPrimaryDark else AmberPrimary
    }
    val primaryContainer = when (accent) {
        AccentColor.Teal   -> if (dark) TealPrimaryContainerDark else TealPrimaryContainer
        AccentColor.Blue   -> if (dark) BluePrimaryContainerDark else BluePrimaryContainer
        AccentColor.Purple -> if (dark) PurplePrimaryContainerDark else PurplePrimaryContainer
        AccentColor.Rose   -> if (dark) RosePrimaryContainerDark else RosePrimaryContainer
        AccentColor.Amber  -> if (dark) AmberPrimaryContainerDark else AmberPrimaryContainer
    }
    val onPrimary = if (dark) Color.Black.copy(alpha = 0f).let {
        when (accent) {
            AccentColor.Teal   -> TealOnPrimaryDark
            AccentColor.Blue   -> Color(0xFF003258)
            AccentColor.Purple -> Color(0xFF3B0091)
            AccentColor.Rose   -> Color(0xFF680003)
            AccentColor.Amber  -> Color(0xFF412D00)
        }
    } else Color.White
    val onPrimaryContainer = when (accent) {
        AccentColor.Teal   -> if (dark) TealOnPrimaryContainerDark else TealOnPrimaryContainer
        AccentColor.Blue   -> if (dark) BlueOnPrimaryContainerDark else BlueOnPrimaryContainer
        AccentColor.Purple -> if (dark) PurpleOnPrimaryContainerDark else PurpleOnPrimaryContainer
        AccentColor.Rose   -> if (dark) RoseOnPrimaryContainerDark else RoseOnPrimaryContainer
        AccentColor.Amber  -> if (dark) AmberOnPrimaryContainerDark else AmberOnPrimaryContainer
    }

    return if (dark) darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerDark,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceContainerDark2,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark,
        inverseSurface = InverseSurfaceDark,
        inverseOnSurface = InverseOnSurfaceDark,
        inversePrimary = InversePrimaryDark,
        surfaceDim = SurfaceDimDark,
        surfaceContainerLowest = SurfaceContainerLowestDark,
        surfaceContainerLow = SurfaceContainerLowDark,
        surfaceContainer = SurfaceContainerDark2,
        surfaceContainerHigh = SurfaceContainerHighDark,
        surfaceContainerHighest = SurfaceContainerHighestDark,
    ) else lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = Secondary,
        onSecondary = OnSecondary,
        secondaryContainer = SecondaryContainer,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = Tertiary,
        onTertiary = OnTertiary,
        tertiaryContainer = TertiaryContainer,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceContainerLight2,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        inverseSurface = InverseSurfaceLight,
        inverseOnSurface = InverseOnSurfaceLight,
        inversePrimary = InversePrimaryLight,
        surfaceDim = SurfaceDimLight,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerLight2,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
    )
}

// Extension helpers for semantic colors not in standard M3 scheme
val ColorScheme.warning: Color get() = if (this.background == BackgroundDark) WarningDark else WarningLight
val ColorScheme.warningContainer: Color get() = if (this.background == BackgroundDark) WarningContainerDark else WarningContainerLight
val ColorScheme.onWarningContainer: Color get() = if (this.background == BackgroundDark) OnWarningContainerDark else OnWarningContainerLight
val ColorScheme.success: Color get() = if (this.background == BackgroundDark) SuccessDark else SuccessLight
val ColorScheme.successContainer: Color get() = if (this.background == BackgroundDark) SuccessContainerDark else SuccessContainerLight
val ColorScheme.onSuccessContainer: Color get() = if (this.background == BackgroundDark) OnSuccessContainerDark else OnSuccessContainerLight

@Composable
fun BeszelTheme(
    dark: Boolean = false,
    accent: AccentColor = AccentColor.Teal,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = buildColorScheme(dark, accent),
        typography = BeszelTypography,
        content = content,
    )
}
