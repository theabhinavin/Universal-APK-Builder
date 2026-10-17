package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AbmRadarAnimation
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1400)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbmDarkNavyBg)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            AbmRadarAnimation(size = 180.dp)

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AB.M. Scan",
                color = AbmTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Personal Privacy Intelligence",
                color = AbmCyanPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ABM | ABHINAV MISHRA",
                color = AbmTextTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}
