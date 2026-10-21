package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DashboardStats
import com.example.data.model.ExamCategory
import com.example.data.model.JobApplication
import com.example.data.remote.GoogleAppsScriptClient
import com.example.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SyncUiState {
    object Idle : SyncUiState
    object Loading : SyncUiState
    data class Success(val message: String) : SyncUiState
    data class Error(val message: String) : SyncUiState
}

class JobAppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val remoteClient = GoogleAppsScriptClient(application)
    val repository = JobRepository(application, database.jobDao(), remoteClient)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow(ExamCategory.ALL)
    val selectedCategory: StateFlow<ExamCategory> = _selectedCategory

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState

    private val _editingApplication = MutableStateFlow<JobApplication?>(null)
    val editingApplication: StateFlow<JobApplication?> = _editingApplication

    private val _deletingApplication = MutableStateFlow<JobApplication?>(null)
    val deletingApplication: StateFlow<JobApplication?> = _deletingApplication

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog

    private val _showScriptHelperDialog = MutableStateFlow(false)
    val showScriptHelperDialog: StateFlow<Boolean> = _showScriptHelperDialog

    private val _isFormOpen = MutableStateFlow(false)
    val isFormOpen: StateFlow<Boolean> = _isFormOpen

    val stats: StateFlow<DashboardStats> = repository.stats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    val applications: StateFlow<List<JobApplication>> = combine(
        repository.allApplications,
        _searchQuery,
        _selectedCategory
    ) { all, query, category ->
        all.filter { app ->
            val matchesCategory = (category == ExamCategory.ALL || app.category == category)
            val matchesQuery = query.isBlank() ||
                    app.orgName.contains(query, ignoreCase = true) ||
                    app.postName.contains(query, ignoreCase = true) ||
                    app.category.displayName().contains(query, ignoreCase = true) ||
                    app.userId.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectCategory(category: ExamCategory) {
        _selectedCategory.value = category
    }

    fun openNewApplicationForm() {
        _editingApplication.value = null
        _isFormOpen.value = true
    }

    fun openEditApplicationForm(app: JobApplication) {
        _editingApplication.value = app
        _isFormOpen.value = true
    }

    fun closeForm() {
        _isFormOpen.value = false
        _editingApplication.value = null
    }

    fun promptDelete(app: JobApplication) {
        _deletingApplication.value = app
    }

    fun cancelDelete() {
        _deletingApplication.value = null
    }

    fun confirmDelete() {
        val app = _deletingApplication.value ?: return
        viewModelScope.launch {
            repository.deleteApplication(app)
            _deletingApplication.value = null
        }
    }

    fun clearDriveFiles(app: JobApplication) {
        viewModelScope.launch {
            repository.clearDriveFiles(app)
        }
    }

    fun saveApplication(
        app: JobApplication,
        appPdfUri: Uri? = null,
        admitPdfUri: Uri? = null
    ) {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading
            val result = repository.saveApplication(app, appPdfUri, admitPdfUri)
            if (result.isSuccess) {
                _syncState.value = SyncUiState.Success("Application saved successfully!")
                closeForm()
            } else {
                _syncState.value = SyncUiState.Error("Error saving application.")
            }
        }
    }

    fun syncWithGoogle() {
        viewModelScope.launch {
            if (!remoteClient.isConfigured()) {
                _showSettingsDialog.value = true
                return@launch
            }
            _syncState.value = SyncUiState.Loading
            val res = repository.syncFromGoogleSheet()
            if (res.isSuccess) {
                _syncState.value = SyncUiState.Success("Synced ${res.getOrNull()} items with Google Sheets!")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Failed to sync"
                _syncState.value = SyncUiState.Error(err)
            }
        }
    }

    fun clearSyncStatus() {
        _syncState.value = SyncUiState.Idle
    }

    fun openSettingsDialog() {
        _showSettingsDialog.value = true
    }

    fun closeSettingsDialog() {
        _showSettingsDialog.value = false
    }

    fun openScriptHelperDialog() {
        _showScriptHelperDialog.value = true
    }

    fun closeScriptHelperDialog() {
        _showScriptHelperDialog.value = false
    }

    fun updateWebAppUrl(url: String) {
        remoteClient.webAppUrl = url
    }

    fun getWebAppUrl(): String = remoteClient.webAppUrl
    fun isWebConfigured(): Boolean = remoteClient.isConfigured()
}
