package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LatestScanEntity
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
import com.example.ui.viewmodel.ScreenDestination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    state: ScanUiState,
    onClearHistory: () -> Unit,
    onNavigate: (ScreenDestination) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var showClearDialog by remember { mutableStateOf(false) }
    val cached: LatestScanEntity? = state.cachedEntity

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Scan History?", color = AbmTextPrimary) },
            text = { Text("This will permanently remove the cached audit record from local device storage.", color = AbmTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Delete", color = AbmSeverityCritical)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = AbmTextSecondary)
                }
            },
            containerColor = AbmDarkSurface
        )
    }

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = {
            TopAppBar(
                title = { Text("Scan History", color = AbmTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AbmTextPrimary)
                    }
                },
                actions = {
                    if (cached != null) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_history_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = AbmSeverityCritical)
                        }
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = AbmCyanPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Local Audit Persistence", color = AbmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "In accordance with your retention policy, AB.M. Scan stores only the single latest completed audit on-device. All audit records remain strictly private and can be deleted at any time.",
                            color = AbmTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (cached == null && state.report == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, tint = AbmTextTertiary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No audit history saved yet", color = AbmTextSecondary, fontSize = 14.sp)
                            Text("Run your first ABM Smart Scan to establish a baseline.", color = AbmTextTertiary, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                val timestamp = cached?.timestamp ?: state.report?.timestamp ?: System.currentTimeMillis()
                val score = cached?.riskScore ?: state.report?.overallRiskScore ?: 100
                val totalApps = cached?.totalApps ?: state.report?.totalAppsScanned ?: 0
                val critical = cached?.criticalCount ?: (state.report?.findingsBySeverity?.get(com.example.domain.model.Severity.CRITICAL) ?: 0)
                val high = cached?.highCount ?: (state.report?.findingsBySeverity?.get(com.example.domain.model.Severity.HIGH) ?: 0)
                val medium = cached?.mediumCount ?: (state.report?.findingsBySeverity?.get(com.example.domain.model.Severity.MEDIUM) ?: 0)
                val low = cached?.lowCount ?: (state.report?.findingsBySeverity?.get(com.example.domain.model.Severity.LOW) ?: 0)
                val summary = cached?.riskSummary ?: state.report?.overallRiskSummary ?: ""

                item {
                    Text("SAVED AUDIT SNAPSHOT", color = AbmCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AbmCyanPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("MMMM d, yyyy • HH:mm:ss", Locale.getDefault()).format(Date(timestamp)),
                                    color = AbmTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0x3300E5FF)) {
                                    Text(
                                        text = "SCORE: $score",
                                        color = AbmCyanPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = summary, color = AbmTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Inspected: $totalApps apps", color = AbmVioletSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (critical > 0) Text("Crit: $critical", color = AbmSeverityCritical, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (high > 0) Text("High: $high", color = AbmSeverityHigh, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (medium > 0) Text("Med: $medium", color = AbmSeverityMedium, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (low > 0) Text("Low: $low", color = AbmSeverityLow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onNavigate(ScreenDestination.REPORT_EXPORT) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AbmCyanPrimary, contentColor = Color(0xFF002933)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("View Full Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
