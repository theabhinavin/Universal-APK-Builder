package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppAuditItem
import com.example.domain.model.SensitivePermissionItem
import com.example.scanner.ThreatScoringEngine
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmDarkSurfaceVariant
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmSeverityCritical
import com.example.ui.theme.AbmSeverityHigh
import com.example.ui.theme.AbmSeverityMedium
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.theme.AbmVioletSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppAuditItem?,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val context = LocalContext.current

    if (app == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AbmDarkNavyBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No application selected", color = AbmTextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) {
                    Text("Back to Inventory")
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Application Audit",
                        color = AbmTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AbmTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AbmDarkNavyBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // App Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1F00E5FF))
                                    .border(1.dp, AbmCyanPrimary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.appName.take(1).uppercase(),
                                    color = AbmCyanPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    color = AbmTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = app.packageName,
                                    color = AbmTextTertiary,
                                    fontSize = 12.sp
                                )
                                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "v${app.versionName} (${app.versionCode})",
                                        color = AbmVioletSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (app.isSystemApp) Color(0x33818CF8) else Color(0x3300E676)
                                    ) {
                                        Text(
                                            text = if (app.isSystemApp) "SYSTEM" else "USER INSTALLED",
                                            color = if (app.isSystemApp) AbmVioletSecondary else AbmPostureSafe,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Remediation System Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", app.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("app_info_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AbmCyanPrimary, contentColor = Color(0xFF002933)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("App Info", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", app.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("manage_perms_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Permissions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        if (!app.isSystemApp) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DELETE).apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("uninstall_app_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFFF5252)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Request Uninstall via System", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Stalkerware & Surveillance Threat Assessment Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            app.iocMatch != null -> AbmSeverityCritical
                            app.threatScore >= 60 -> AbmSeverityHigh
                            app.threatScore >= 40 -> AbmSeverityMedium
                            else -> AbmDarkBorder
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SURVEILLANCE THREAT AUDIT",
                                color = AbmCyanPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            ThreatScoreBadge(score = app.threatScore, category = app.threatCategory)
                        }

                        if (app.threatScore >= 60) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33FF5252),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AbmSeverityHigh)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = AbmSeverityHigh, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ThreatScoringEngine.WARNING_TEXT,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        if (app.iocMatch != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33FF3B30),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AbmSeverityCritical)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "MATCHED KNOWN STALKERWARE INDICATOR",
                                        color = AbmSeverityCritical,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${app.iocMatch.ioc.knownName} (${app.iocMatch.ioc.classification})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Text(
                                        text = app.iocMatch.matchDescription,
                                        color = AbmTextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Launcher Visibility & Hidden status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (app.hasLauncherActivity) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (app.hasLauncherActivity) AbmPostureSafe else AbmSeverityHigh,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (app.hasLauncherActivity) "Launcher activity present (visible in app drawer)" else "No launcher activity (hidden background app)",
                                color = if (app.hasLauncherActivity) AbmTextPrimary else AbmSeverityHigh,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Contributing Indicators
                        if (app.contributingThreatIndicators.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Contributing Threat Signals:",
                                color = AbmTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            app.contributingThreatIndicators.forEach { ind ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = RoundedCornerShape(3.dp), color = Color(0x2BFF5252)) {
                                        Text(
                                            text = "+${ind.score}",
                                            color = AbmSeverityHigh,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = ind.title, color = AbmTextPrimary, fontSize = 12.sp)
                                }
                            }
                        }

                        // Certificate SHA-256 fingerprint
                        if (!app.signingCertSha256.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Signing Certificate SHA-256 Fingerprint:",
                                color = AbmTextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = app.signingCertSha256,
                                color = AbmVioletSecondary,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Installation Origin Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "INSTALLATION ORIGIN",
                            color = AbmTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.installerLabel,
                                color = if (app.isUnknownSource) AbmSeverityHigh else AbmPostureSafe,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (app.installerPackage != null) {
                                Text(
                                    text = " (${app.installerPackage})",
                                    color = AbmTextTertiary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (app.isUnknownSource) {
                                "This app was installed from a source other than the Google Play Store or manufacturer market. While sideloading is often legitimate (e.g., F-Droid or direct download), it warrants manual review."
                            } else {
                                "Verified installation from a recognized platform app marketplace."
                            },
                            color = AbmTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Sensitive Permissions Breakdown
            item {
                Text(
                    text = "SENSITIVE PERMISSIONS (${app.sensitivePermissions.count { it.isGranted }} granted / ${app.sensitivePermissions.size} declared)",
                    color = AbmTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (app.sensitivePermissions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = AbmDarkSurface
                    ) {
                        Text(
                            text = "No high-privilege sensitive permissions declared.",
                            color = AbmPostureSafe,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(app.sensitivePermissions) { perm ->
                    SensitivePermRow(perm = perm)
                }
            }

            // Special Access Flags
            if (app.specialAccessFlags.isNotEmpty()) {
                item {
                    Text(
                        text = "SPECIAL SYSTEM PRIVILEGES",
                        color = AbmTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                items(app.specialAccessFlags) { flag ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = AbmDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AbmSeverityMedium, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = flag, color = AbmTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Educational Notice
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = AbmDarkSurfaceVariant
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AbmVioletSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Permission grant confirms technical capability, not active or malicious use. Use Android's Privacy Dashboard to inspect when sensors were last accessed.",
                            color = AbmTextTertiary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SensitivePermRow(perm: SensitivePermissionItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = AbmDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (perm.isGranted) AbmCyanPrimary.copy(alpha = 0.3f) else AbmDarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = perm.group,
                    color = AbmCyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (perm.isGranted) AbmPostureSafe.copy(alpha = 0.15f) else Color(0x1FFFFFFF)
                ) {
                    Text(
                        text = if (perm.isGranted) "GRANTED" else "NOT GRANTED",
                        color = if (perm.isGranted) AbmPostureSafe else AbmTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = perm.permission.substringAfterLast("."),
                color = AbmTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = perm.description,
                color = AbmTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
