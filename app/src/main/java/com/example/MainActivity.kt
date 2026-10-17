package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ActiveScanScreen
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.AppInventoryScreen
import com.example.ui.screens.CameraPrivacyScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceOverviewScreen
import com.example.ui.screens.LocationPrivacyScreen
import com.example.ui.screens.MicrophonePrivacyScreen
import com.example.ui.screens.ReportExportScreen
import com.example.ui.screens.ScanHistoryScreen
import com.example.ui.screens.ScanResultsScreen
import com.example.ui.screens.SecurityPostureScreen
import com.example.ui.screens.SensitivePermissionsScreen
import com.example.ui.screens.SettingsAboutScreen
import com.example.ui.screens.SpecialAccessScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UnknownSourcesScreen
import com.example.ui.theme.ABMScanTheme
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.viewmodel.ScanViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ABMScanTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AbmDarkNavyBg
                ) {
                    AbmAppNavigation()
                }
            }
        }
    }
}

@Composable
fun AbmAppNavigation(
    viewModel: ScanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = viewModel.backStack.size > 1) {
        viewModel.navigateBack()
    }

    Crossfade(targetState = viewModel.currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            ScreenDestination.SPLASH -> {
                SplashScreen(
                    onSplashComplete = { viewModel.onSplashFinished() }
                )
            }
            ScreenDestination.DASHBOARD -> {
                DashboardScreen(
                    state = uiState,
                    onStartScan = { viewModel.startSmartScan() },
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
            ScreenDestination.ACTIVE_SCAN -> {
                ActiveScanScreen(
                    state = uiState,
                    onCancelScan = { viewModel.cancelScan() }
                )
            }
            ScreenDestination.SCAN_RESULTS -> {
                ScanResultsScreen(
                    report = uiState.report,
                    onNavigate = { viewModel.navigateTo(it) },
                    onBackToDashboard = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_DEVICE -> {
                DeviceOverviewScreen(
                    state = uiState,
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_APPS -> {
                AppInventoryScreen(
                    state = uiState,
                    onSearchChange = { viewModel.updateAppSearchQuery(it) },
                    onFilterChange = { viewModel.setAppFilter(it) },
                    onSelectApp = { viewModel.selectApp(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.APP_DETAIL -> {
                AppDetailScreen(
                    app = uiState.selectedApp,
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_CAMERA -> {
                CameraPrivacyScreen(
                    state = uiState,
                    onSelectApp = { viewModel.selectApp(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_MICROPHONE -> {
                MicrophonePrivacyScreen(
                    state = uiState,
                    onSelectApp = { viewModel.selectApp(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_LOCATION -> {
                LocationPrivacyScreen(
                    state = uiState,
                    onSelectApp = { viewModel.selectApp(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_SENSITIVE_PERMISSIONS -> {
                SensitivePermissionsScreen(
                    state = uiState,
                    onSelectApp = { viewModel.selectApp(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_SPECIAL_ACCESS -> {
                SpecialAccessScreen(
                    state = uiState,
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_UNKNOWN_SOURCES -> {
                UnknownSourcesScreen(
                    state = uiState,
                    onSelectApp = { viewModel.selectApp(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_SECURITY_POSTURE -> {
                SecurityPostureScreen(
                    state = uiState,
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_HISTORY -> {
                ScanHistoryScreen(
                    state = uiState,
                    onClearHistory = { viewModel.clearHistory() },
                    onNavigate = { viewModel.navigateTo(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.MODULE_STALKERWARE -> {
                com.example.ui.screens.StalkerwareAuditScreen(
                    state = uiState,
                    onSelectApp = { viewModel.selectApp(it) },
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.REPORT_EXPORT -> {
                ReportExportScreen(
                    state = uiState,
                    onBack = { viewModel.navigateBack() }
                )
            }
            ScreenDestination.SETTINGS_ABOUT -> {
                SettingsAboutScreen(
                    onClearHistory = { viewModel.clearHistory() },
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}
