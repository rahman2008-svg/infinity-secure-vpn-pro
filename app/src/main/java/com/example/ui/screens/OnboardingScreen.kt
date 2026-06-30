package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class OnboardingPage(
    val title: String,
    val description: String,
    val iconColor: Color,
    val illustrationType: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPageIndex by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Faster Internet",
            description = "Unleash extreme speeds. By optimizing DNS lookups, we route your inquiries through Cloudflare's ultra-responsive global edge network.",
            iconColor = CyberCyan,
            illustrationType = "SPEED"
        ),
        OnboardingPage(
            title = "Private Secure DNS",
            description = "Encrypt your digital footprint. Our DNS-over-HTTPS (DoH) and DNS-over-TLS (DoT) protocols shield your browsing queries from ISP monitoring and local spoofing.",
            iconColor = ElectricBlue,
            illustrationType = "SECURITY"
        ),
        OnboardingPage(
            title = "Local WARP Tunnel",
            description = "A real, system-level local VPN. Encrypts all DNS lookup parameters, establishing an active secure interface shown directly in your system status bar.",
            iconColor = PremiumOrange,
            illustrationType = "TUNNEL"
        ),
        OnboardingPage(
            title = "WARP+ Smart Routing",
            description = "Premium congestion bypass. Tap into Argo smart routing to direct data through optimal internet backbones and maximize transfer speeds.",
            iconColor = Color(0xFFFFD600),
            illustrationType = "SMART_ROUTING"
        ),
        OnboardingPage(
            title = "Family Protection DNS",
            description = "Automated web defense. Instantly filter out known malware domains, phishing scripts, or adult content automatically at the gateway level.",
            iconColor = Color(0xFF00E676),
            illustrationType = "FAMILY"
        )
    )

    val currentPage = pages[currentPageIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Skip", color = TextSecondary, fontSize = 14.sp)
                }
            }

            // Illustration / Graphics Section (Canvas Based for Premium feel)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "OnboardingGraphics"
                ) { targetPage ->
                    IllustrationCanvas(
                        type = targetPage.illustrationType,
                        accentColor = targetPage.iconColor,
                        modifier = Modifier
                            .size(240.dp)
                            .padding(16.dp)
                    )
                }
            }

            // Texts and descriptions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                    },
                    label = "OnboardingText"
                ) { targetPage ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = targetPage.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = targetPage.description,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Indicator Dots & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                if (currentPageIndex > 0) {
                    TextButton(
                        onClick = { currentPageIndex-- },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Back", color = TextSecondary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                // Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPageIndex) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (index == currentPageIndex) CyberCyan else TextSecondary.copy(alpha = 0.4f))
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (currentPageIndex < pages.lastIndex) {
                            currentPageIndex++
                        } else {
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(
                        text = if (currentPageIndex == pages.lastIndex) "Get Started" else "Next",
                        color = DarkVoid,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun IllustrationCanvas(type: String, accentColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val r = size.width / 3

        when (type) {
            "SPEED" -> {
                // Draw Speedometer
                drawCircle(
                    color = accentColor.copy(alpha = 0.1f),
                    center = Offset(cx, cy),
                    radius = r
                )
                drawArc(
                    color = accentColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx()),
                    size = size.copy(width = r * 2, height = r * 2),
                    topLeft = Offset(cx - r, cy - r)
                )
                // Speed needle
                drawLine(
                    color = PremiumOrange,
                    start = Offset(cx, cy),
                    end = Offset(cx + r * 0.7f, cy - r * 0.4f),
                    strokeWidth = 4.dp.toPx()
                )
                drawCircle(
                    color = Color.White,
                    center = Offset(cx, cy),
                    radius = 8.dp.toPx()
                )
            }
            "SECURITY" -> {
                // Draw Shield and Key Lock
                drawCircle(
                    color = accentColor.copy(alpha = 0.1f),
                    center = Offset(cx, cy),
                    radius = r
                )
                // Draw Shield outline
                val path = Path().apply {
                    moveTo(cx, cy - r * 0.7f)
                    quadraticTo(cx + r * 0.6f, cy - r * 0.7f, cx + r * 0.6f, cy - r * 0.1f)
                    quadraticTo(cx + r * 0.6f, cy + r * 0.5f, cx, cy + r * 0.8f)
                    quadraticTo(cx - r * 0.6f, cy + r * 0.5f, cx - r * 0.6f, cy - r * 0.1f)
                    quadraticTo(cx - r * 0.6f, cy - r * 0.7f, cx, cy - r * 0.7f)
                }
                drawPath(
                    path = path,
                    color = accentColor,
                    style = Stroke(width = 4.dp.toPx())
                )
                // Draw keyhole inside shield
                drawCircle(
                    color = Color.White,
                    center = Offset(cx, cy - 8.dp.toPx()),
                    radius = 12.dp.toPx()
                )
                val keyholePath = Path().apply {
                    moveTo(cx - 6.dp.toPx(), cy - 4.dp.toPx())
                    lineTo(cx + 6.dp.toPx(), cy - 4.dp.toPx())
                    lineTo(cx + 10.dp.toPx(), cy + 18.dp.toPx())
                    lineTo(cx - 10.dp.toPx(), cy + 18.dp.toPx())
                    close()
                }
                drawPath(path = keyholePath, color = Color.White)
            }
            "TUNNEL" -> {
                // Draw Tunnel / Pipes
                drawCircle(
                    color = accentColor.copy(alpha = 0.1f),
                    center = Offset(cx, cy),
                    radius = r
                )
                // Multiple concentric circles representing tunnel
                for (i in 1..4) {
                    drawCircle(
                        color = accentColor.copy(alpha = i * 0.2f),
                        center = Offset(cx, cy),
                        radius = r * (i * 0.25f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                // Secure center point
                drawCircle(
                    color = Color.White,
                    center = Offset(cx, cy),
                    radius = 16.dp.toPx()
                )
            }
            "SMART_ROUTING" -> {
                // Draw Node Network
                drawCircle(
                    color = accentColor.copy(alpha = 0.1f),
                    center = Offset(cx, cy),
                    radius = r
                )
                val p1 = Offset(cx, cy - r * 0.6f)
                val p2 = Offset(cx - r * 0.6f, cy + r * 0.4f)
                val p3 = Offset(cx + r * 0.6f, cy + r * 0.4f)
                
                // Draw network lines
                drawLine(color = accentColor, start = p1, end = p2, strokeWidth = 3.dp.toPx())
                drawLine(color = accentColor, start = p2, end = p3, strokeWidth = 3.dp.toPx())
                drawLine(color = accentColor, start = p3, end = p1, strokeWidth = 3.dp.toPx())
                drawLine(color = Color.White, start = Offset(cx, cy), end = p1, strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(cx, cy), end = p2, strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(cx, cy), end = p3, strokeWidth = 2.dp.toPx())

                // Draw node points
                drawCircle(color = accentColor, center = p1, radius = 12.dp.toPx())
                drawCircle(color = accentColor, center = p2, radius = 12.dp.toPx())
                drawCircle(color = accentColor, center = p3, radius = 12.dp.toPx())
                drawCircle(color = Color.White, center = Offset(cx, cy), radius = 14.dp.toPx())
            }
            "FAMILY" -> {
                // Family / Lock / Tree style
                drawCircle(
                    color = accentColor.copy(alpha = 0.1f),
                    center = Offset(cx, cy),
                    radius = r
                )
                // Draw two stylized adult/child heads
                drawCircle(color = accentColor, center = Offset(cx - 16.dp.toPx(), cy - 10.dp.toPx()), radius = 16.dp.toPx())
                drawCircle(color = Color.White, center = Offset(cx + 18.dp.toPx(), cy + 6.dp.toPx()), radius = 12.dp.toPx())
                
                // Body curves
                drawArc(
                    color = accentColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 36.dp.toPx(), cy + 10.dp.toPx()),
                    size = Size(40.dp.toPx(), 40.dp.toPx())
                )
                drawArc(
                    color = Color.White,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx + 4.dp.toPx(), cy + 20.dp.toPx()),
                    size = Size(28.dp.toPx(), 28.dp.toPx())
                )
            }
        }
    }
}
