package com.omargannoune.smartbudget.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.omargannoune.smartbudget.R

private val spaceGroteskProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val spaceGrotesk = FontFamily(
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = spaceGroteskProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = spaceGroteskProvider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = spaceGroteskProvider,
        weight = FontWeight.SemiBold
    )
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = spaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = spaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = spaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = spaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = spaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    labelSmall = TextStyle(
        fontFamily = spaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp
    )
)
