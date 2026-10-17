package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ScanStage
import com.example.ui.components.AbmRadarAnimation
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.viewmodel.ScanUiState

@Composable
fun ActiveScanScreen(
    state: ScanUiState,
    onCancelScan: () -> Unit
) {
    BackHandler {
        onCancelScan()
    }

    val stages = ScanStage.values().toList()
    val currentStageIndex = stages.indexOf(state.currentStage)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbmDarkNavyBg)
            .testTag("active_scan_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SCANNING YOUR DEVICE",
                color = AbmCyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Radar Animation
            AbmRadarAnimation(size = 180.dp)

            Spacer(modifier = Modifier.height(20.dp))

            // Percentage and current stage header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = state.currentStage.title,
                        color = AbmTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.currentStage.description,
                        color = AbmTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${(state.progress * 100).toInt()}%",
                    color = AbmCyanPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smooth progress indicator
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AbmCyanPrimary,
                trackColor = Color(0x3300E5FF)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 9 Real Inspection Stages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stages) { stage ->
                    val stageIdx = stages.indexOf(stage)
                    val isCompleted = stageIdx < currentStageIndex
                    val isCurrent = stageIdx == currentStageIndex

                    StageItemRow(
                        stage = stage,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCancelScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("cancel_scan_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "CANCEL SCAN",
                    color = AbmTextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StageItemRow(
    stage: ScanStage,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isCurrent) Color(0x1A00E5FF) else AbmDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrent) AbmCyanPrimary.copy(alpha = 0.5f) else AbmDarkBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> AbmPostureSafe.copy(alpha = 0.15f)
                            isCurrent -> AbmCyanPrimary.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isCompleted -> AbmPostureSafe
                            isCurrent -> AbmCyanPrimary
                            else -> AbmTextTertiary.copy(alpha = 0.4f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompleted -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = AbmPostureSafe,
                        modifier = Modifier.size(14.dp)
                    )
                    isCurrent -> CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = AbmCyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stage.title,
                    color = if (isCompleted || isCurrent) AbmTextPrimary else AbmTextTertiary,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = stage.description,
                    color = if (isCurrent) AbmCyanPrimary else AbmTextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            if (isCompleted) {
                Text(
                    text = "VERIFIED",
                    color = AbmPostureSafe,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            } else if (isCurrent) {
                Text(
                    text = "AUDITING",
                    color = AbmCyanPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
