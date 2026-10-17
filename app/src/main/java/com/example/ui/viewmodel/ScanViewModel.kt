package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.ReportExporter
import com.example.data.local.LatestScanEntity
import com.example.data.repository.ScanRepository
import com.example.domain.model.AppAuditItem
import com.example.domain.model.ScanReport
import com.example.domain.model.ScanStage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenDestination {
    SPLASH,
    DASHBOARD,
    ACTIVE_SCAN,
    SCAN_RESULTS,
    MODULE_DEVICE,
    MODULE_APPS,
    MODULE_CAMERA,
    MODULE_MICROPHONE,
    MODULE_LOCATION,
    MODULE_SENSITIVE_PERMISSIONS,
    MODULE_SPECIAL_ACCESS,
    MODULE_UNKNOWN_SOURCES,
    MODULE_SECURITY_POSTURE,
    MODULE_HISTORY,
    MODULE_STALKERWARE,
    APP_DETAIL,
    REPORT_EXPORT,
    SETTINGS_ABOUT
}

enum class AppFilter(val label: String) {
    ALL("All Visible"),
    USER("User Installed"),
    SYSTEM("System Packages"),
    SENSITIVE("Sensitive Perms"),
    UNKNOWN_SOURCE("Sideloaded / Unknown"),
    SPECIAL_ACCESS("Special Access"),
    STALKERWARE("Surveillance Threats")
}

data class ScanUiState(
    val isScanning: Boolean = false,
    val currentStage: ScanStage = ScanStage.DEVICE_CONFIG,
    val progress: Float = 0f,
    val report: ScanReport? = null,
    val cachedEntity: LatestScanEntity? = null,
    val selectedApp: AppAuditItem? = null,
    val appFilter: AppFilter = AppFilter.ALL,
    val appSearchQuery: String = "",
    val errorMessage: String? = null
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScanRepository(application)

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    val backStack = mutableStateListOf(ScreenDestination.SPLASH)

    val currentScreen: ScreenDestination
        get() = backStack.lastOrNull() ?: ScreenDestination.DASHBOARD

    private var scanJob: Job? = null

    init {
        // Observe Room latest scan
        viewModelScope.launch {
            repository.getLatestScanEntity().collect { entity ->
                _uiState.value = _uiState.value.copy(cachedEntity = entity)
            }
        }
        // Run silent baseline audit on startup so all modules immediately show live device telemetry
        viewModelScope.launch {
            try {
                val initialReport = repository.executeScan { _, _ -> }
                if (_uiState.value.report == null) {
                    _uiState.value = _uiState.value.copy(report = initialReport)
                }
            } catch (_: Exception) {
                // Ignore fallback
            }
        }
    }

    fun onSplashFinished() {
        if (backStack.size == 1 && backStack.first() == ScreenDestination.SPLASH) {
            backStack.clear()
            backStack.add(ScreenDestination.DASHBOARD)
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        if (currentScreen != destination) {
            backStack.add(destination)
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            return true
        }
        return false
    }

    fun startSmartScan() {
        if (_uiState.value.isScanning) return

        scanJob?.cancel()
        navigateTo(ScreenDestination.ACTIVE_SCAN)

        scanJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                currentStage = ScanStage.DEVICE_CONFIG,
                progress = 0f,
                errorMessage = null
            )

            try {
                val resultReport = repository.executeScan { stage, progressPercent ->
                    _uiState.value = _uiState.value.copy(
                        currentStage = stage,
                        progress = progressPercent
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    report = resultReport,
                    progress = 1f
                )

                delay(300)
                // Transition cleanly to scan results
                backStack.remove(ScreenDestination.ACTIVE_SCAN)
                backStack.add(ScreenDestination.SCAN_RESULTS)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    errorMessage = e.message ?: "Scan failed"
                )
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            progress = 0f
        )
        navigateBack()
    }

    fun selectApp(app: AppAuditItem) {
        _uiState.value = _uiState.value.copy(selectedApp = app)
        navigateTo(ScreenDestination.APP_DETAIL)
    }

    fun updateAppSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(appSearchQuery = query)
    }

    fun setAppFilter(filter: AppFilter) {
        _uiState.value = _uiState.value.copy(appFilter = filter)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun shareCurrentReport(context: Context) {
        val current = _uiState.value.report
        if (current != null) {
            ReportExporter.shareReport(context, current)
        }
    }
}
