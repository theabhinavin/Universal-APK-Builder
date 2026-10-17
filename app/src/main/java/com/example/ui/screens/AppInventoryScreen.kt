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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmDarkSurfaceVariant
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmSeverityHigh
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.theme.AbmVioletSecondary
import com.example.ui.viewmodel.AppFilter
import com.example.ui.viewmodel.ScanUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInventoryScreen(
    state: ScanUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (AppFilter) -> Unit,
    onSelectApp: (AppAuditItem) -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val appsList = remember(state.report, state.cachedEntity) {
        state.report?.apps ?: emptyList()
    }

    val filteredApps = remember(appsList, state.appSearchQuery, state.appFilter) {
        appsList.filter { app ->
            val matchesQuery = state.appSearchQuery.isEmpty() ||
                    app.appName.contains(state.appSearchQuery, ignoreCase = true) ||
                    app.packageName.contains(state.appSearchQuery, ignoreCase = true)

            val matchesFilter = when (state.appFilter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
                AppFilter.SENSITIVE -> app.sensitivePermissions.any { it.isGranted }
                AppFilter.UNKNOWN_SOURCE -> app.isUnknownSource
                AppFilter.SPECIAL_ACCESS -> app.specialAccessFlags.isNotEmpty()
                AppFilter.STALKERWARE -> app.threatScore >= 40 || app.iocMatch != null
            }

            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Installed Applications",
                            color = AbmTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredApps.size} apps visible",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.appSearchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_input"),
                placeholder = { Text("Search by name or package...", color = AbmTextTertiary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AbmCyanPrimary) },
                trailingIcon = {
                    if (state.appSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AbmTextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AbmCyanPrimary,
                    unfocusedBorderColor = AbmDarkBorder,
                    focusedContainerColor = AbmDarkSurface,
                    unfocusedContainerColor = AbmDarkSurface,
                    focusedTextColor = AbmTextPrimary,
                    unfocusedTextColor = AbmTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AppFilter.values()) { filter ->
                    FilterChip(
                        selected = state.appFilter == filter,
                        onClick = { onFilterChange(filter) },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Apps List
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = AbmTextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (appsList.isEmpty()) "No applications audited yet.\nRun an ABM Smart Scan from the home dashboard." else "No apps match the selected filter.",
                            color = AbmTextSecondary,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredApps) { app ->
                        AppInventoryCard(
                            app = app,
                            onClick = { onSelectApp(app) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AppInventoryCard(
    app: AppAuditItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("app_row_${app.packageName}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Avatar Icon Placeholder
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (app.isSystemApp) Color(0x1F818CF8) else Color(0x1F00E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.appName.take(1).uppercase(),
                    color = if (app.isSystemApp) AbmVioletSecondary else AbmCyanPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        color = AbmTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (app.isSystemApp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AbmDarkSurfaceVariant
                        ) {
                            Text(
                                text = "SYSTEM",
                                color = AbmTextTertiary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = app.packageName,
                    color = AbmTextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Source: ${app.installerLabel}",
                        color = if (app.isUnknownSource) Color(0xFFFF9100) else AbmTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "•", color = AbmTextTertiary, fontSize = 10.sp)
                    val grantedSensitives = app.sensitivePermissions.count { it.isGranted }
                    Text(
                        text = "$grantedSensitives sensitive",
                        color = if (grantedSensitives > 0) AbmCyanPrimary else AbmTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Details",
                tint = AbmTextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
