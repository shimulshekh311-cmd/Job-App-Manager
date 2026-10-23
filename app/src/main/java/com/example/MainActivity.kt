package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JobAppViewModel
import com.example.ui.components.CodeHelperDialog
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.GitHubUpdateDialog
import com.example.ui.components.GoogleSyncDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JobFormScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JobAppManagerRoot()
            }
        }
    }
}

@Composable
fun JobAppManagerRoot(
    viewModel: JobAppViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val applications by viewModel.applications.collectAsStateWithLifecycle()
    val rawAllApplications by viewModel.rawAllApplications.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val isFormOpen by viewModel.isFormOpen.collectAsStateWithLifecycle()
    val editingApp by viewModel.editingApplication.collectAsStateWithLifecycle()
    val deletingApp by viewModel.deletingApplication.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showScriptHelperDialog by viewModel.showScriptHelperDialog.collectAsStateWithLifecycle()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = isFormOpen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition",
        modifier = Modifier.fillMaxSize()
    ) { openForm ->
        if (openForm) {
            JobFormScreen(
                editingApp = editingApp,
                onSave = { app, appUri, admitUri ->
                    viewModel.saveApplication(app, appUri, admitUri)
                },
                onCancel = {
                    viewModel.closeForm()
                }
            )
        } else {
            HomeScreen(
                stats = stats,
                applications = applications,
                allApplications = rawAllApplications,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                syncState = syncState,
                isWebConfigured = viewModel.isWebConfigured(),
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                onCategorySelect = { viewModel.onSelectCategory(it) },
                onAddApplication = { viewModel.openNewApplicationForm() },
                onEditApplication = { viewModel.openEditApplicationForm(it) },
                onDeleteApplication = { viewModel.promptDelete(it) },
                onClearDriveFiles = { viewModel.clearDriveFiles(it) },
                onOpenSyncSettings = { viewModel.openSettingsDialog() },
                onOpenCodeHelper = { viewModel.openScriptHelperDialog() },
                onOpenUpdater = { viewModel.openUpdateDialog() },
                onDismissSyncStatus = { viewModel.clearSyncStatus() }
            )
        }
    }

    // Deletion Modal
    if (deletingApp != null) {
        DeleteConfirmationDialog(
            application = deletingApp,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }

    // Google Sheets & Drive Sync Dialog
    if (showSettingsDialog) {
        GoogleSyncDialog(
            initialUrl = viewModel.getWebAppUrl(),
            syncState = syncState,
            onSaveUrl = { viewModel.updateWebAppUrl(it) },
            onSyncNow = { viewModel.syncWithGoogle() },
            onOpenCodeHelper = {
                viewModel.closeSettingsDialog()
                viewModel.openScriptHelperDialog()
            },
            onDismiss = { viewModel.closeSettingsDialog() }
        )
    }

    // Code.gs Helper Script Dialog
    if (showScriptHelperDialog) {
        CodeHelperDialog(
            onDismiss = { viewModel.closeScriptHelperDialog() }
        )
    }

    // GitHub In-App Auto-Update Dialog
    if (showUpdateDialog) {
        GitHubUpdateDialog(
            currentVersion = viewModel.getCurrentAppVersion(),
            currentRepo = viewModel.getGitHubRepo(),
            updateState = updateState,
            downloadProgress = downloadProgress,
            onSaveRepo = { viewModel.updateGitHubRepo(it) },
            onCheckUpdate = { viewModel.checkForGitHubUpdates() },
            onStartDownload = { viewModel.startDownloadUpdate(it) },
            onInstallApk = { viewModel.installDownloadedApk(it) },
            onDismiss = { viewModel.closeUpdateDialog() }
        )
    }
}
