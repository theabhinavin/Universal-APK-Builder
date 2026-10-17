package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmDarkSurfaceVariant
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmSeverityCritical
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.theme.AbmVioletSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen(
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var showWipeDialog by remember { mutableStateOf(false) }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("Delete Audit History?", color = AbmTextPrimary) },
            text = { Text("This will remove all cached scan results and snapshots from local device storage.", color = AbmTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showWipeDialog = false
                        Toast.makeText(context, "Audit history cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = AbmSeverityCritical)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) {
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
                title = { Text("Settings & About", color = AbmTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AbmTextPrimary)
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
            // Brand Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmCyanPrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0F203D), Color(0xFF0A1224))
                                    )
                                )
                                .border(2.dp, AbmCyanPrimary, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ABM",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "AB.M. Scan",
                            color = AbmTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Personal Privacy Intelligence",
                            color = AbmCyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "ABM — Abhinav Mishra",
                            color = AbmVioletSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Version 1.0 (Build 1)",
                            color = AbmTextTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Privacy Pledge Card
            item {
                Text("PRIVACY PLEDGE", color = AbmCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PledgeRow(title = "Zero Network Uploads", desc = "The app does not request Internet permission. All scans run strictly on-device.")
                        PledgeRow(title = "No Trackers or Analytics", desc = "No advertising SDKs, telemetry, or user tracking libraries.")
                        PledgeRow(title = "No Account Required", desc = "No sign-in, authentication, or personal identifiers collected.")
                        PledgeRow(title = "Data Under Your Control", desc = "Audit snapshots remain in local Room storage until you wipe them or share them.")
                    }
                }
            }

            // Platform Limitations
            item {
                Text("PLATFORM BOUNDARIES", color = AbmCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Legitimate Android Security Model",
                            color = AbmTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• AB.M. Scan operates within Android's official security sandbox.\n• Android does not allow third-party audit apps to secretly intercept other apps' traffic, monitor private storage, or revoke other apps' permissions automatically.\n• Historical 24-hour camera/microphone access timestamps are managed exclusively by Android's Privacy Dashboard in System Settings.\n• This audit inspects declared permissions, grant flags, installer origins, and observable system posture.",
                            color = AbmTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Data Wipe
            item {
                Text("LOCAL DATA MANAGEMENT", color = AbmCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AbmDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Clear Audit History", color = AbmTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Erase all cached scan data from Room database", color = AbmTextTertiary, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { showWipeDialog = true },
                            modifier = Modifier.testTag("wipe_data_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = AbmSeverityCritical, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", color = AbmSeverityCritical, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Open-source notices
            item {
                Text("FRAMEWORK NOTICES", color = AbmTextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                Text(
                    text = "Built with Kotlin, Jetpack Compose, Material 3, AndroidX Room, and Moshi.\nLicensed under Android Open Source Project and Apache 2.0.",
                    color = AbmTextTertiary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun PledgeRow(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Security, contentDescription = null, tint = AbmPostureSafe, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = AbmTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc, color = AbmTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
