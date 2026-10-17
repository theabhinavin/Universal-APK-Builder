package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AuditFinding
import com.example.domain.model.Severity
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmDarkSurfaceVariant
import com.example.ui.theme.AbmElectricBlue
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmSeverityCritical
import com.example.ui.theme.AbmSeverityHigh
import com.example.ui.theme.AbmSeverityInfo
import com.example.ui.theme.AbmSeverityLow
import com.example.ui.theme.AbmSeverityMedium
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.theme.AbmVioletSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AbmBrandHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 8.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Monogram Shield
        Box(
            modifier = Modifier
                .size(if (compact) 44.dp else 56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0F203D), Color(0xFF0A1224))
                    )
                )
                .border(1.5.dp, AbmCyanPrimary, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ABM",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = if (compact) 16.sp else 20.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "AB.M. Scan",
                color = AbmTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 18.sp else 22.sp,
                letterSpacing = 0.5.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Personal Privacy Intelligence",
                    color = AbmCyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = " • ",
                    color = AbmTextTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = "ABM — Abhinav Mishra",
                    color = AbmTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AbmRadarAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val transition = rememberInfiniteTransition(label = "RadarSweep")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // Background cyber glow circle
            drawCircle(
                color = Color(0x1000E5FF),
                radius = radius
            )

            // Concentric radar circles
            for (i in 1..4) {
                val r = radius * (i / 4f)
                drawCircle(
                    color = Color(0x3300E5FF),
                    radius = r,
                    style = Stroke(width = 1.2f)
                )
            }

            // Crosshairs
            drawLine(
                color = Color(0x2200E5FF),
                start = Offset(center.x, 0f),
                end = Offset(center.x, this.size.height),
                strokeWidth = 1.2f
            )
            drawLine(
                color = Color(0x2200E5FF),
                start = Offset(0f, center.y),
                end = Offset(this.size.width, center.y),
                strokeWidth = 1.2f
            )

            // Radar sweep line
            val rad = Math.toRadians(angle.toDouble())
            val sweepEnd = Offset(
                x = center.x + (radius * cos(rad)).toFloat(),
                y = center.y + (radius * sin(rad)).toFloat()
            )
            drawLine(
                color = AbmCyanPrimary,
                start = center,
                end = sweepEnd,
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }

        // Center ABM Monogram Core
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            color = Color(0xFF0F203D),
            border = androidx.compose.foundation.BorderStroke(2.dp, AbmCyanPrimary)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "ABM",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SeverityBadge(
    severity: Severity,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (severity) {
        Severity.CRITICAL -> Triple(AbmSeverityCritical.copy(alpha = 0.15f), AbmSeverityCritical, Icons.Default.Error)
        Severity.HIGH -> Triple(AbmSeverityHigh.copy(alpha = 0.15f), AbmSeverityHigh, Icons.Default.Warning)
        Severity.MEDIUM -> Triple(AbmSeverityMedium.copy(alpha = 0.15f), AbmSeverityMedium, Icons.Default.Warning)
        Severity.LOW -> Triple(AbmSeverityLow.copy(alpha = 0.15f), AbmSeverityLow, Icons.Default.Info)
        Severity.INFORMATIONAL -> Triple(AbmSeverityInfo.copy(alpha = 0.15f), AbmSeverityInfo, Icons.Default.CheckCircle)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = severity.label,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = severity.label.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun FindingCard(
    finding: AuditFinding,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("finding_card_${finding.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = finding.severity)
                Text(
                    text = finding.category.displayName,
                    color = AbmTextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = finding.title,
                color = AbmTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (!finding.affectedAppName.isNullOrEmpty()) {
                Text(
                    text = "${finding.affectedAppName} (${finding.affectedPackage})",
                    color = AbmCyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Evidence Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = AbmDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1A00E5FF))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "OBSERVED EVIDENCE",
                        color = AbmCyanPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = finding.evidence,
                        color = AbmTextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = finding.explanation,
                color = AbmTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (finding.limitations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Platform boundary: ${finding.limitations}",
                    color = AbmTextTertiary,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recommendation & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = finding.recommendedAction,
                    color = AbmVioletSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (finding.systemIntentAction != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { launchSystemSetting(context, finding.systemIntentAction, finding.systemIntentPackage) },
                        modifier = Modifier.testTag("action_btn_${finding.id}"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Review", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open Settings", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

fun launchSystemSetting(context: Context, action: String, targetPackage: String? = null) {
    try {
        val intent = when (action) {
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS -> {
                if (!targetPackage.isNullOrEmpty()) {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", targetPackage, null)
                    }
                } else {
                    Intent(Settings.ACTION_SETTINGS)
                }
            }
            else -> Intent(action)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback to main settings
        try {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {
            // Unhandled
        }
    }
}
