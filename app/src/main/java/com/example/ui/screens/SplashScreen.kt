package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    
    // Pulse animation for glow
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteSpec(1500),
        label = "GlowPulse"
    )

    // Rotation animation for the loading gear/ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteSpec(2000),
        label = "LogoRotation"
    )

    var loadingText by remember { mutableStateOf("Initializing secure protocols...") }

    LaunchedEffect(Unit) {
        delay(800)
        loadingText = "Checking cellular & Wi-Fi gateways..."
        delay(800)
        loadingText = "Establishing local tunnel configurations..."
        delay(600)
        loadingText = "Ready to connect!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkVoid, Color(0xFF0A142D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Infinity Logo
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Drawing the glowing infinity path
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val halfWidth = width / 2
                    val halfHeight = height / 2
                    
                    val path = Path().apply {
                        // Drawing an elegant infinity loop
                        val controlX1 = halfWidth / 2
                        val controlY1 = halfHeight / 2
                        
                        moveTo(halfWidth, halfHeight)
                        // Left Loop
                        cubicTo(
                            halfWidth - controlX1, halfHeight - controlY1 * 2f,
                            0f, halfHeight + controlY1 * 2f,
                            0f, halfHeight
                        )
                        cubicTo(
                            0f, halfHeight - controlY1 * 2f,
                            halfWidth - controlX1, halfHeight + controlY1 * 2f,
                            halfWidth, halfHeight
                        )
                        // Right Loop
                        cubicTo(
                            halfWidth + controlX1, halfHeight - controlY1 * 2f,
                            width, halfHeight + controlY1 * 2f,
                            width, halfHeight
                        )
                        cubicTo(
                            width, halfHeight - controlY1 * 2f,
                            halfWidth + controlX1, halfHeight + controlY1 * 2f,
                            halfWidth, halfHeight
                        )
                    }

                    // Shadow/Glow effect
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(CyberCyan.copy(alpha = 0.15f * glowPulse), ElectricBlue.copy(alpha = 0.05f))
                        ),
                        style = Stroke(width = 16.dp.toPx())
                    )

                    // Core path
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(CyberCyan, ElectricBlue)
                        ),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "INFINITY SECURE",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "VPN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(PremiumOrange, shape = MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO+",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Modern circular progress loading
            CircularProgressIndicator(
                color = CyberCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = loadingText,
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// Infinite animation helper
private fun infiniteSpec(duration: Int): InfiniteRepeatableSpec<Float> {
    return infiniteRepeatable(
        animation = tween(duration, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    )
}
