package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.domain.model.ThreatCategory
import com.example.scanner.StalkerwareIocDatabase
import com.example.scanner.ThreatScoringEngine
import com.example.ui.components.launchSystemSetting
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmDarkSurfaceVariant
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmSeverityCritical
import com.example.ui.theme.AbmSeverityHigh
import com.example.ui.theme.AbmSeverityLow
import com.example.ui.theme.AbmSeverityMedium
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.theme.AbmVioletSecondary
import com.example.ui.viewmodel.ScanUiState

enum class StalkerwareFilter(val label: String) {
    ALL("All Screened"),
    HIGH_RISK("High Threat (≥60)"),
    ELEVATED("Elevated (≥40)"),
    IOC_MATCHES("Known IoC Matches"),
    NO_LAUNCHER("Hidden (No Icon)"),
    ACCESSIBILITY("Accessibility"),
    ADMINS("Device Admins"),
    SIDELOADED("Non-Store Origins")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StalkerwareAuditScreen(
    state: ScanUiState,
    onSelectApp: (AppAuditItem) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val allApps = state.report?.apps ?: emptyList()
    var selectedFilter by remember { mutableStateOf(StalkerwareFilter.ALL) }

    val highRiskApps = remember(allApps) { allApps.filter { it.threatScore >= 60 } }
    val elevatedApps = remember(allApps) { allApps.filter { it.threatScore in 40..59 } }
    val iocMatchApps = remember(allApps) { allApps.filter { it.iocMatch != null } }

    val filteredApps = remember(allApps, selectedFilter) {
        when (selectedFilter) {
            StalkerwareFilter.ALL -> allApps.filter { it.threatScore > 0 || it.iocMatch != null }
            StalkerwareFilter.HIGH_RISK -> highRiskApps
            StalkerwareFilter.ELEVATED -> elevatedApps
            StalkerwareFilter.IOC_MATCHES -> iocMatchApps
            StalkerwareFilter.NO_LAUNCHER -> allApps.filter { !it.isSystemApp && !it.hasLauncherActivity }
            StalkerwareFilter.ACCESSIBILITY -> allApps.filter { it.specialAccessFlags.any { f -> f.contains("Accessibility") } }
            StalkerwareFilter.ADMINS -> allApps.filter { it.specialAccessFlags.any { f -> f.contains("Device Administrator") } }
            StalkerwareFilter.SIDELOADED -> allApps.filter { it.isUnknownSource }
        }
    }

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Stalkerware & Surveillance",
                            color = AbmTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Evidence-Based Spyware Audit",
                            color = AbmCyanPrimary,
                            fontSize = 11.sp
                        )
                    }
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
            // Surveillance Posture Metrics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (highRiskApps.isNotEmpty() || iocMatchApps.isNotEmpty()) AbmSeverityHigh.copy(alpha = 0.6f) else AbmDarkBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SURVEILLANCE POSTURE",
                                    color = AbmTextTertiary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = when {
                                        iocMatchApps.isNotEmpty() -> "Known IoC Matched"
                                        highRiskApps.isNotEmpty() -> "Elevated Risk Detected"
                                        else -> "No Stalkerware Identified"
                                    },
                                    color = when {
                                        iocMatchApps.isNotEmpty() -> AbmSeverityCritical
                                        highRiskApps.isNotEmpty() -> AbmSeverityHigh
                                        else -> AbmPostureSafe
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AbmDarkSurfaceVariant
                            ) {
                                Text(
                                    text = "DB: ${StalkerwareIocDatabase.DATABASE_VERSION}",
                                    color = AbmVioletSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SurveillanceStatPill(label = "High Threat (≥60)", count = highRiskApps.size, color = AbmSeverityHigh)
                            SurveillanceStatPill(label = "Elevated (≥40)", count = elevatedApps.size, color = AbmSeverityMedium)
                            SurveillanceStatPill(label = "IoC Matches", count = iocMatchApps.size, color = AbmSeverityCritical)
                            SurveillanceStatPill(label = "Total Screened", count = allApps.size, color = AbmCyanPrimary)
                        }
                    }
                }
            }

            // CRITICAL Safety Notice Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E1010),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmSeverityHigh.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AbmSeverityHigh, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Safety & Survivor Advisory",
                                color = AbmSeverityHigh,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "If someone may be actively monitoring your device and could react to changes, immediately altering settings or uninstalling surveillance software might alert them.\n\n" +
                                    "Consider using an unmonitored computer or trusted friend's phone to contact support organizations (e.g. Coalition Against Stalkerware at stopstalkerware.org or local domestic support hotlines).",
                            color = AbmTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // High Threat Warning Banner if score >= 60 exists
            if (highRiskApps.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x2BFF5252),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AbmSeverityHigh)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AbmSeverityHigh, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = ThreatScoringEngine.WARNING_TEXT,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Filter Chips Row
            item {
                Text(
                    text = "FILTER AUDITED APPLICATIONS",
                    color = AbmTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(StalkerwareFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AbmCyanPrimary,
                                selectedLabelColor = Color(0xFF002933),
                                containerColor = AbmDarkSurface,
                                labelColor = AbmTextSecondary
                            )
                        )
                    }
                }
            }

            // Flagged Applications List
            if (filteredApps.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = AbmDarkSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AbmPostureSafe, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (allApps.isEmpty()) "Audit in progress or scan not yet run.\nRun an ABM Smart Scan to inspect your device." else "No applications match the selected threat filter.",
                                color = AbmTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (allApps.isEmpty()) "ABM inspects accessibility services, device administrators, overlays, installation origins, and stalkerware IoCs." else "A negative indicator result indicates that no bundled pattern matched, not that the device is completely free of potential tampering.",
                                color = AbmTextTertiary,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredApps) { app ->
                    StalkerwareAppCard(
                        app = app,
                        onClick = { onSelectApp(app) },
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Safe Remediation & Removal Guidance Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SAFE MANUAL REMEDIATION GUIDE",
                            color = AbmCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AB.M. Scan never automatically uninstalls apps or revokes permissions without your explicit action. The user remains in full control.",
                            color = AbmTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        RemediationStep(
                            step = "1",
                            title = "Review Device Administrators",
                            desc = "Some stalkerware registers as a Device Admin to block uninstallation.",
                            actionLabel = "Open Security Settings",
                            onAction = { launchSystemSetting(context, "android.settings.SECURITY_SETTINGS") }
                        )
                        RemediationStep(
                            step = "2",
                            title = "Review Accessibility Services",
                            desc = "Disable any unfamiliar accessibility tools that can intercept on-screen activity.",
                            actionLabel = "Open Accessibility",
                            onAction = { launchSystemSetting(context, Settings.ACTION_ACCESSIBILITY_SETTINGS) }
                        )
                        RemediationStep(
                            step = "3",
                            title = "Review Overlay Permissions (Draw Over Apps)",
                            desc = "Apps with overlay permission can render invisible touch targets (tapjacking) or capture sensitive inputs.",
                            actionLabel = "Open Overlay Settings",
                            onAction = { launchSystemSetting(context, Settings.ACTION_MANAGE_OVERLAY_PERMISSION) }
                        )
                        RemediationStep(
                            step = "4",
                            title = "Review Notification Access (Listeners)",
                            desc = "Notification listeners can intercept incoming two-factor verification SMS codes and chat messages.",
                            actionLabel = "Open Notification Access",
                            onAction = { launchSystemSetting(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
                        )
                        RemediationStep(
                            step = "5",
                            title = "Safe Mode Removal (If Uninstallation is Blocked)",
                            desc = "Rebooting into Android Safe Mode temporarily disables third-party services, allowing you to uninstall resistant apps via Settings > Apps.",
                            actionLabel = "Manual Safe Mode Instructions",
                            onAction = {
                                launchSystemSetting(context, Settings.ACTION_SETTINGS)
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun StalkerwareAppCard(
    app: AppAuditItem,
    onClick: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("stalkerware_card_${app.packageName}"),
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
            // Header Row: App name + Threat Score Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        color = AbmTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Text(
                        text = app.packageName,
                        color = AbmTextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                ThreatScoreBadge(score = app.threatScore, category = app.threatCategory)
            }

            // Known IoC Match Callout if present
            if (app.iocMatch != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33FF3B30),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmSeverityCritical)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AbmSeverityCritical, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KNOWN STALKERWARE ARTIFACT",
                                color = AbmSeverityCritical,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "${app.iocMatch.ioc.knownName} • ${app.iocMatch.ioc.classification}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = app.iocMatch.matchDescription,
                            color = AbmTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Contributing Threat Indicators Breakdown
            if (app.contributingThreatIndicators.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    app.contributingThreatIndicators.forEach { indicator ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x26FF5252)
                            ) {
                                Text(
                                    text = "+${indicator.score}",
                                    color = AbmSeverityHigh,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = indicator.title,
                                color = AbmTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("App Info", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AbmCyanPrimary, contentColor = Color(0xFF002933)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Audit Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ThreatScoreBadge(score: Int, category: ThreatCategory) {
    val (color, bg) = when {
        score >= 80 -> Pair(AbmSeverityCritical, Color(0x2BFF3B30))
        score >= 60 -> Pair(AbmSeverityHigh, Color(0x2BFF5252))
        score >= 40 -> Pair(AbmSeverityMedium, Color(0x2BFFB300))
        score >= 20 -> Pair(Color(0xFF38BDF8), Color(0x2B38BDF8))
        else -> Pair(AbmPostureSafe, Color(0x2B00E676))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score / 100",
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
            Text(
                text = category.label.take(12),
                color = color,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SurveillanceStatPill(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), color = color, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(text = label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RemediationStep(
    step: String,
    title: String,
    desc: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(4.dp), color = AbmDarkSurfaceVariant) {
                Text(
                    text = "STEP $step",
                    color = AbmCyanPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = AbmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Text(text = desc, color = AbmTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = onAction,
            modifier = Modifier.height(34.dp),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(actionLabel, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
        }
    }
}
