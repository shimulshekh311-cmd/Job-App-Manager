package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.JobApplication
import com.example.data.model.StageInfo
import com.example.data.model.StageStatus
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFormScreen(
    editingApp: JobApplication?,
    onSave: (JobApplication, Uri?, Uri?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler(onBack = onCancel)

    // Form states
    var applyDate by remember {
        mutableStateOf(editingApp?.applyDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var day by remember { mutableStateOf(editingApp?.day ?: "") }
    var orgName by remember { mutableStateOf(editingApp?.orgName ?: "") }
    var postName by remember { mutableStateOf(editingApp?.postName ?: "") }
    var userId by remember { mutableStateOf(editingApp?.userId ?: "") }
    var password by remember { mutableStateOf(editingApp?.password ?: "") }
    var isCanceled by remember { mutableStateOf(editingApp?.isCanceled ?: false) }

    // PDFs
    var appPdfUri by remember { mutableStateOf<Uri?>(null) }
    var appPdfName by remember { mutableStateOf(if (editingApp?.appPdfUrl?.isNotBlank() == true) "Current Drive File Attached" else "") }

    var admitPdfUri by remember { mutableStateOf<Uri?>(null) }
    var admitPdfName by remember { mutableStateOf(if (editingApp?.admitPdfUrl?.isNotBlank() == true) "Current Drive File Attached" else "") }

    // Stages
    var mcqStatus by remember { mutableStateOf(editingApp?.mcq?.status ?: StageStatus.NO) }
    var mcqDate by remember { mutableStateOf(editingApp?.mcq?.date ?: "") }
    var mcqRating by remember { mutableStateOf(editingApp?.mcq?.rating ?: "Good") }

    var writtenStatus by remember { mutableStateOf(editingApp?.written?.status ?: StageStatus.NO) }
    var writtenDate by remember { mutableStateOf(editingApp?.written?.date ?: "") }
    var writtenRating by remember { mutableStateOf(editingApp?.written?.rating ?: "Good") }

    var typingStatus by remember { mutableStateOf(editingApp?.typing?.status ?: StageStatus.NO) }
    var typingDate by remember { mutableStateOf(editingApp?.typing?.date ?: "") }
    var typingRating by remember { mutableStateOf(editingApp?.typing?.rating ?: "Good") }

    var vivaStatus by remember { mutableStateOf(editingApp?.viva?.status ?: StageStatus.NO) }
    var vivaDate by remember { mutableStateOf(editingApp?.viva?.date ?: "") }
    var vivaRating by remember { mutableStateOf(editingApp?.viva?.rating ?: "Good") }

    // Auto-compute day when applyDate changes
    fun calculateDay(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d = sdf.parse(dateString) ?: return ""
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            dayFormat.format(d)
        } catch (e: Exception) {
            ""
        }
    }

    LaunchedEffect(applyDate) {
        day = calculateDay(applyDate)
    }

    // PDF Pickers
    val appPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            appPdfUri = uri
            appPdfName = uri.lastPathSegment?.substringAfterLast('/') ?: "App_Document.pdf"
        }
    }

    val admitPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            admitPdfUri = uri
            admitPdfName = uri.lastPathSegment?.substringAfterLast('/') ?: "Admit_Card.pdf"
        }
    }

    // Date Picker Helper
    fun showDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d = sdf.parse(currentDate)
            if (d != null) calendar.time = d
        } catch (_: Exception) {}

        val picker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        picker.show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingApp != null) "Edit Application" else "New Job Application",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("form_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = EmeraldGreen
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldGreen
                    )

                    // Apply Date & Auto Day
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = applyDate,
                            onValueChange = { applyDate = it },
                            label = { Text("Apply Date") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("form_apply_date"),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker(applyDate) { applyDate = it } }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = EmeraldGreen)
                                }
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = day,
                            onValueChange = {},
                            label = { Text("Day") },
                            readOnly = true,
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("form_day_display"),
                            singleLine = true
                        )
                    }

                    // Organization Name
                    OutlinedTextField(
                        value = orgName,
                        onValueChange = { orgName = it },
                        label = { Text("Organization Name *") },
                        placeholder = { Text("e.g. Bangladesh Bank") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_org_name"),
                        singleLine = true
                    )

                    // Post Name
                    OutlinedTextField(
                        value = postName,
                        onValueChange = { postName = it },
                        label = { Text("Post Name *") },
                        placeholder = { Text("e.g. Assistant Director") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_post_name"),
                        singleLine = true
                    )

                    // User ID & Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = userId,
                            onValueChange = { userId = it },
                            label = { Text("User ID") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_user_id"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_password"),
                            singleLine = true
                        )
                    }

                    // Cancel status toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Is Canceled?",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !isCanceled,
                                onClick = { isCanceled = false },
                                label = { Text("No") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldGreen.copy(alpha = 0.15f),
                                    selectedLabelColor = EmeraldGreen
                                )
                            )
                            FilterChip(
                                selected = isCanceled,
                                onClick = { isCanceled = true },
                                label = { Text("Yes") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusFailed.copy(alpha = 0.15f),
                                    selectedLabelColor = StatusFailed
                                )
                            )
                        }
                    }
                }
            }

            // Attached Documents (Drive / Local PDF)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Document Attachments (PDF)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldGreen
                    )

                    // App PDF
                    PdfSelectorItem(
                        title = "Application PDF",
                        fileName = appPdfName,
                        onPickFile = { appPdfLauncher.launch("application/pdf") },
                        onClear = {
                            appPdfUri = null
                            appPdfName = ""
                        }
                    )

                    // Admit Card PDF
                    PdfSelectorItem(
                        title = "Admit Card PDF",
                        fileName = admitPdfName,
                        onPickFile = { admitPdfLauncher.launch("application/pdf") },
                        onClear = {
                            admitPdfUri = null
                            admitPdfName = ""
                        }
                    )
                }
            }

            // 4 Stages Progress Details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Exam Stages & Ratings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldGreen
                    )

                    // MCQ Stage
                    StageFormField(
                        stageName = "MCQ",
                        status = mcqStatus,
                        date = mcqDate,
                        rating = mcqRating,
                        onStatusChange = { mcqStatus = it },
                        onDateChange = { mcqDate = it },
                        onRatingChange = { mcqRating = it },
                        onPickDate = { showDatePicker(mcqDate) { mcqDate = it } }
                    )

                    // Written Stage
                    StageFormField(
                        stageName = "Written",
                        status = writtenStatus,
                        date = writtenDate,
                        rating = writtenRating,
                        onStatusChange = { writtenStatus = it },
                        onDateChange = { writtenDate = it },
                        onRatingChange = { writtenRating = it },
                        onPickDate = { showDatePicker(writtenDate) { writtenDate = it } }
                    )

                    // Typing Stage
                    StageFormField(
                        stageName = "Typing",
                        status = typingStatus,
                        date = typingDate,
                        rating = typingRating,
                        onStatusChange = { typingStatus = it },
                        onDateChange = { typingDate = it },
                        onRatingChange = { typingRating = it },
                        onPickDate = { showDatePicker(typingDate) { typingDate = it } }
                    )

                    // Viva Stage
                    StageFormField(
                        stageName = "Viva",
                        status = vivaStatus,
                        date = vivaDate,
                        rating = vivaRating,
                        onStatusChange = { vivaStatus = it },
                        onDateChange = { vivaDate = it },
                        onRatingChange = { vivaRating = it },
                        onPickDate = { showDatePicker(vivaDate) { vivaDate = it } }
                    )
                }
            }

            // Save / Update Button
            Button(
                onClick = {
                    if (orgName.isNotBlank() && postName.isNotBlank()) {
                        val updated = JobApplication(
                            id = editingApp?.id ?: 0,
                            applyDate = applyDate,
                            day = day,
                            orgName = orgName.trim(),
                            postName = postName.trim(),
                            userId = userId.trim(),
                            password = password.trim(),
                            mcq = StageInfo(mcqStatus, if (mcqStatus == StageStatus.PASSED) mcqDate else "", mcqRating),
                            written = StageInfo(writtenStatus, if (writtenStatus == StageStatus.PASSED) writtenDate else "", writtenRating),
                            typing = StageInfo(typingStatus, if (typingStatus == StageStatus.PASSED) typingDate else "", typingRating),
                            viva = StageInfo(vivaStatus, if (vivaStatus == StageStatus.PASSED) vivaDate else "", vivaRating),
                            appPdfUrl = editingApp?.appPdfUrl ?: "",
                            admitPdfUrl = editingApp?.admitPdfUrl ?: "",
                            localAppPdfUri = editingApp?.localAppPdfUri ?: "",
                            localAdmitPdfUri = editingApp?.localAdmitPdfUri ?: "",
                            isCanceled = isCanceled,
                            syncStatus = "Saved"
                        )
                        onSave(updated, appPdfUri, admitPdfUri)
                    }
                },
                enabled = orgName.isNotBlank() && postName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_application_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingApp != null) "Update Application" else "Save Application",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun PdfSelectorItem(
    title: String,
    fileName: String,
    onPickFile: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = if (fileName.isNotBlank()) ActionBlue else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (fileName.isNotBlank()) fileName else "No PDF selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (fileName.isNotBlank()) ActionBlue else Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Row {
                Button(
                    onClick = onPickFile,
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Select", style = MaterialTheme.typography.labelSmall)
                }

                if (fileName.isNotBlank()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, contentDescription = "Clear file", tint = StatusFailed)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StageFormField(
    stageName: String,
    status: StageStatus,
    date: String,
    rating: String,
    onStatusChange: (StageStatus) -> Unit,
    onDateChange: (String) -> Unit,
    onRatingChange: (String) -> Unit,
    onPickDate: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Stage Name & Status selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$stageName Exam",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = status == StageStatus.NO,
                        onClick = { onStatusChange(StageStatus.NO) },
                        label = { Text("No") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusPending.copy(alpha = 0.15f),
                            selectedLabelColor = StatusPending
                        )
                    )
                    FilterChip(
                        selected = status == StageStatus.PASSED,
                        onClick = { onStatusChange(StageStatus.PASSED) },
                        label = { Text("Passed") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen.copy(alpha = 0.15f),
                            selectedLabelColor = EmeraldGreen
                        )
                    )
                    FilterChip(
                        selected = status == StageStatus.FAILED,
                        onClick = { onStatusChange(StageStatus.FAILED) },
                        label = { Text("Failed") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusFailed.copy(alpha = 0.15f),
                            selectedLabelColor = StatusFailed
                        )
                    )
                }
            }

            // If Passed, show Date and Rating fields
            AnimatedVisibility(visible = status == StageStatus.PASSED) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = onDateChange,
                            label = { Text("Exam Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            trailingIcon = {
                                IconButton(onClick = onPickDate) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = EmeraldGreen)
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )

                        // Rating Dropdown
                        var ratingExpanded by remember { mutableStateOf(false) }
                        val ratings = listOf("Very Good", "Good", "Medium", "Bad", "Very Bad")

                        ExposedDropdownMenuBox(
                            expanded = ratingExpanded,
                            onExpandedChange = { ratingExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = rating,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Rating") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ratingExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = ratingExpanded,
                                onDismissRequest = { ratingExpanded = false }
                            ) {
                                ratings.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            onRatingChange(r)
                                            ratingExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
