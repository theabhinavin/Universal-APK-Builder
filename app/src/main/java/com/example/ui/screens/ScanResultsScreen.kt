package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AuditFinding
import com.example.domain.model.ScanReport
import com.example.domain.model.Severity
import com.example.ui.components.FindingCard
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
import com.example.ui.viewmodel.ScreenDestination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultsScreen(
    report: ScanReport?,
    onNavigate: (ScreenDestination) -> Unit,
    onBackToDashboard: () -> Unit
) {
    BackHandler {
        onBackToDashboard()
    }

    if (report == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AbmDarkNavyBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No scan completed yet", color = AbmTextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBackToDashboard) {
                    Text("Return to Dashboard")
                }
            }
        }
        return
    }

    var selectedSeverityFilter by remember { mutableStateOf<Severity?>(null) }

    val filteredFindings = remember(report, selectedSeverityFilter) {
        if (selectedSeverityFilter == null) {
            report.findings
        } else {
            report.findings.filter { it.severity == selectedSeverityFilter }
        }
    }

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Scan Results",
                            color = AbmTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(report.timestamp)),
                            color = AbmCyanPrimary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AbmTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate(ScreenDestination.REPORT_EXPORT) },
                        modifier = Modifier.testTag("results_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = AbmCyanPrimary
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Posture Score & Executive Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SECURITY SCORE",
                                    color = AbmTextTertiary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${report.overallRiskScore} / 100",
                                    color = when {
                                        report.overallRiskScore >= 85 -> AbmPostureSafe
                                        report.overallRiskScore >= 60 -> AbmSeverityMedium
                                        else -> AbmSeverityHigh
                                    },
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Device info badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AbmDarkSurfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "${report.deviceInfo.manufacturer} ${report.deviceInfo.model}",
                                        color = AbmTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Android ${report.deviceInfo.androidVersion}",
                                        color = AbmVioletSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = report.overallRiskSummary,
                            color = AbmTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryStatBadge(label = "Apps Inspected", value = "${report.totalAppsScanned}")
                            SummaryStatBadge(label = "Checks Done", value = "${report.completedStages.size}/9")
                            SummaryStatBadge(label = "Total Findings", value = "${report.findings.size}")
                        }
                    }
                }
            }

            // Severity Breakdown Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeverityCountPill(label = "Critical", count = report.findingsBySeverity[Severity.CRITICAL] ?: 0, color = AbmSeverityCritical, modifier = Modifier.weight(1f))
                    SeverityCountPill(label = "High", count = report.findingsBySeverity[Severity.HIGH] ?: 0, color = AbmSeverityHigh, modifier = Modifier.weight(1f))
                    SeverityCountPill(label = "Medium", count = report.findingsBySeverity[Severity.MEDIUM] ?: 0, color = AbmSeverityMedium, modifier = Modifier.weight(1f))
                    SeverityCountPill(label = "Low", count = report.findingsBySeverity[Severity.LOW] ?: 0, color = AbmSeverityLow, modifier = Modifier.weight(1f))
                    SeverityCountPill(label = "Info", count = report.findingsBySeverity[Severity.INFORMATIONAL] ?: 0, color = AbmSeverityInfo, modifier = Modifier.weight(1f))
                }
            }

            // Quick Navigation Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigate(ScreenDestination.REPORT_EXPORT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("view_report_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AbmCyanPrimary, contentColor = Color(0xFF002933)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audit Report", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { onNavigate(ScreenDestination.MODULE_APPS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("review_apps_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onNavigate(ScreenDestination.MODULE_STALKERWARE) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("stalkerware_audit_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Spyware", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Severity Filter Row
            item {
                Text(
                    text = "AUDIT FINDINGS (${filteredFindings.size})",
                    color = AbmTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedSeverityFilter == null,
                            onClick = { selectedSeverityFilter = null },
                            label = { Text("All (${report.findings.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AbmCyanPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                    items(Severity.values()) { sev ->
                        val count = report.findingsBySeverity[sev] ?: 0
                        FilterChip(
                            selected = selectedSeverityFilter == sev,
                            onClick = { selectedSeverityFilter = if (selectedSeverityFilter == sev) null else sev },
                            label = { Text("${sev.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AbmCyanPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }

            // Findings List
            if (filteredFindings.isEmpty()) {
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
                            Icon(Icons.Default.Security, contentDescription = null, tint = AbmPostureSafe, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No findings for selected severity filter",
                                color = AbmTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                items(filteredFindings) { finding ->
                    FindingCard(finding = finding)
                }
            }

            // Platform limitations transparency note
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = AbmDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AbmVioletSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Platform Honesty: Normal Android apps cannot access other apps' private storage, network payloads, or historical camera/mic logs. This audit evaluates permissions, installer metadata, and system privileges exposed by documented APIs.",
                            color = AbmTextTertiary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
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
private fun SummaryStatBadge(label: String, value: String) {
    Column {
        Text(text = label, color = AbmTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = AbmTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SeverityCountPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), color = color, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(text = label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
