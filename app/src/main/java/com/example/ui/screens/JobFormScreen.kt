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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val isNewApplication = (editingApp == null)

    // Basic Details
    var applyDate by remember {
        mutableStateOf(editingApp?.applyDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var day by remember { mutableStateOf(editingApp?.day ?: "") }
    var orgName by remember { mutableStateOf(editingApp?.orgName ?: "") }
    var postName by remember { mutableStateOf(editingApp?.postName ?: "") }
    var userId by remember { mutableStateOf(editingApp?.userId ?: "") }
    var password by remember { mutableStateOf(editingApp?.password ?: "") }
    var isOverallCanceled by remember { mutableStateOf(editingApp?.isCanceled ?: false) }

    // PDFs
    var appPdfUri by remember { mutableStateOf<Uri?>(null) }
    var appPdfName by remember {
        mutableStateOf(if (editingApp?.appPdfUrl?.isNotBlank() == true) "Current Drive App PDF Attached" else "")
    }

    var admitPdfUri by remember { mutableStateOf<Uri?>(null) }
    var admitPdfName by remember {
        mutableStateOf(if (editingApp?.admitPdfUrl?.isNotBlank() == true) "Current Drive Admit Card Attached" else "")
    }

    // MCQ Stage State
    var mcqCanceled by remember { mutableStateOf(editingApp?.mcq?.isCanceled ?: false) }
    var mcqDate by remember { mutableStateOf(editingApp?.mcq?.date ?: "") }
    var mcqRating by remember { mutableStateOf(editingApp?.mcq?.rating?.ifBlank { "ভালো" } ?: "ভালো") }
    var mcqObtainedMarks by remember { mutableStateOf(editingApp?.mcq?.obtainedMarks ?: "") }
    var mcqTotalMarks by remember { mutableStateOf(editingApp?.mcq?.totalMarks ?: "") }
    var mcqStatus by remember { mutableStateOf(editingApp?.mcq?.status ?: StageStatus.NO) }

    // Written Stage State
    var writtenCanceled by remember { mutableStateOf(editingApp?.written?.isCanceled ?: false) }
    var writtenDate by remember { mutableStateOf(editingApp?.written?.date ?: "") }
    var writtenRating by remember { mutableStateOf(editingApp?.written?.rating?.ifBlank { "ভালো" } ?: "ভালো") }
    var writtenObtainedMarks by remember { mutableStateOf(editingApp?.written?.obtainedMarks ?: "") }
    var writtenTotalMarks by remember { mutableStateOf(editingApp?.written?.totalMarks ?: "") }
    var writtenStatus by remember { mutableStateOf(editingApp?.written?.status ?: StageStatus.NO) }

    // Typing Stage State
    var typingCanceled by remember { mutableStateOf(editingApp?.typing?.isCanceled ?: false) }
    var typingDate by remember { mutableStateOf(editingApp?.typing?.date ?: "") }
    var typingRating by remember { mutableStateOf(editingApp?.typing?.rating?.ifBlank { "ভালো" } ?: "ভালো") }
    var typingObtainedMarks by remember { mutableStateOf(editingApp?.typing?.obtainedMarks ?: "") }
    var typingTotalMarks by remember { mutableStateOf(editingApp?.typing?.totalMarks ?: "") }
    var typingStatus by remember { mutableStateOf(editingApp?.typing?.status ?: StageStatus.NO) }

    // Viva Stage State
    var vivaCanceled by remember { mutableStateOf(editingApp?.viva?.isCanceled ?: false) }
    var vivaDate by remember { mutableStateOf(editingApp?.viva?.date ?: "") }
    var vivaRating by remember { mutableStateOf(editingApp?.viva?.rating?.ifBlank { "ভালো" } ?: "ভালো") }
    var vivaObtainedMarks by remember { mutableStateOf(editingApp?.viva?.obtainedMarks ?: "") }
    var vivaTotalMarks by remember { mutableStateOf(editingApp?.viva?.totalMarks ?: "") }
    var vivaStatus by remember { mutableStateOf(editingApp?.viva?.status ?: StageStatus.NO) }

    // Auto-calculate Day of week
    fun calculateDay(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d = sdf.parse(dateString) ?: return ""
            SimpleDateFormat("EEEE", Locale.getDefault()).format(d)
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

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNewApplication) "নতুন আবেদন (New Application)" else "আবেদন সম্পাদনা ও পরীক্ষা (Edit & Stages)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.testTag("form_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Card 1: Basic Information
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
                        text = "আবেদনের প্রাথমিক তথ্য (Basic Information)",
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
                            label = { Text("আবেদনের তারিখ (Apply Date)") },
                            modifier = Modifier
                                .weight(1.3f)
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
                            label = { Text("বার (Day)") },
                            readOnly = true,
                            modifier = Modifier
                                .weight(0.7f)
                                .testTag("form_day_display"),
                            singleLine = true
                        )
                    }

                    // Organization Name
                    OutlinedTextField(
                        value = orgName,
                        onValueChange = { orgName = it },
                        label = { Text("প্রতিষ্ঠানের নাম (Organization) *") },
                        placeholder = { Text("যেমন: বাংলাদেশ ব্যাংক / Biman") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_org_name"),
                        singleLine = true
                    )

                    // Post Name
                    OutlinedTextField(
                        value = postName,
                        onValueChange = { postName = it },
                        label = { Text("পদের নাম (Post Name) *") },
                        placeholder = { Text("যেমন: সহকারী পরিচালক (AD)") },
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
                            label = { Text("ইউজার আইডি (User ID)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_user_id"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("পাসওয়ার্ড (Password)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_password"),
                            singleLine = true
                        )
                    }

                    // Application PDF Upload (Always available)
                    PdfSelectorItem(
                        title = "আবেদন কপি PDF (Application PDF)",
                        fileName = appPdfName,
                        onPickFile = { appPdfLauncher.launch("application/pdf") },
                        onClear = {
                            appPdfUri = null
                            appPdfName = ""
                        }
                    )
                }
            }

            // Edit Mode: Progressive Exam Stages
            if (!isNewApplication) {
                // Admit Card Upload Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "অ্যাডমিট কার্ড (Admit Card PDF)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldGreen
                        )
                        Text(
                            text = "পরিক্ষা শুরু হলে অ্যাডমিট কার্ড আপলোড করে সংরক্ষণ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PdfSelectorItem(
                            title = "অ্যাডমিট কার্ড PDF",
                            fileName = admitPdfName,
                            onPickFile = { admitPdfLauncher.launch("application/pdf") },
                            onClear = {
                                admitPdfUri = null
                                admitPdfName = ""
                            }
                        )
                    }
                }

                // Stage 1: MCQ Exam Stage
                ProgressiveStageCard(
                    stageNumber = 1,
                    stageTitle = "MCQ ধাপ (MCQ Exam)",
                    isUnlocked = true,
                    isCanceled = mcqCanceled,
                    onToggleCanceled = { mcqCanceled = it },
                    examDate = mcqDate,
                    onDateChange = { mcqDate = it },
                    onPickDate = { showDatePicker(mcqDate) { mcqDate = it } },
                    rating = mcqRating,
                    onRatingChange = { mcqRating = it },
                    obtainedMarks = mcqObtainedMarks,
                    onObtainedMarksChange = { mcqObtainedMarks = it },
                    totalMarks = mcqTotalMarks,
                    onTotalMarksChange = { mcqTotalMarks = it },
                    status = mcqStatus,
                    onStatusChange = { mcqStatus = it }
                )

                // Stage 2: Written Exam Stage (Unlocked only if MCQ Passed!)
                val isWrittenUnlocked = (mcqStatus == StageStatus.PASSED && !mcqCanceled)
                ProgressiveStageCard(
                    stageNumber = 2,
                    stageTitle = "লিখিত ধাপ (Written Exam)",
                    isUnlocked = isWrittenUnlocked,
                    unlockRequirementText = "MCQ ধাপে 'পাস করেছেন: হ্যাঁ' সিলেক্ট করলে এই লিখিত ধাপটি খুলবে।",
                    isCanceled = writtenCanceled,
                    onToggleCanceled = { writtenCanceled = it },
                    examDate = writtenDate,
                    onDateChange = { writtenDate = it },
                    onPickDate = { showDatePicker(writtenDate) { writtenDate = it } },
                    rating = writtenRating,
                    onRatingChange = { writtenRating = it },
                    obtainedMarks = writtenObtainedMarks,
                    onObtainedMarksChange = { writtenObtainedMarks = it },
                    totalMarks = writtenTotalMarks,
                    onTotalMarksChange = { writtenTotalMarks = it },
                    status = writtenStatus,
                    onStatusChange = { writtenStatus = it }
                )

                // Stage 3: Typing Exam Stage (Unlocked only if Written Passed!)
                val isTypingUnlocked = isWrittenUnlocked && (writtenStatus == StageStatus.PASSED && !writtenCanceled)
                ProgressiveStageCard(
                    stageNumber = 3,
                    stageTitle = "টাইপিং ধাপ (Typing Exam)",
                    isUnlocked = isTypingUnlocked,
                    unlockRequirementText = "লিখিত ধাপে 'পাস করেছেন: হ্যাঁ' সিলেক্ট করলে টাইপিং ধাপটি খুলবে।",
                    isCanceled = typingCanceled,
                    onToggleCanceled = { typingCanceled = it },
                    examDate = typingDate,
                    onDateChange = { typingDate = it },
                    onPickDate = { showDatePicker(typingDate) { typingDate = it } },
                    rating = typingRating,
                    onRatingChange = { typingRating = it },
                    obtainedMarks = typingObtainedMarks,
                    onObtainedMarksChange = { typingObtainedMarks = it },
                    totalMarks = typingTotalMarks,
                    onTotalMarksChange = { typingTotalMarks = it },
                    status = typingStatus,
                    onStatusChange = { typingStatus = it }
                )

                // Stage 4: Viva Exam Stage (Unlocked only if Typing Passed!)
                val isVivaUnlocked = isTypingUnlocked && (typingStatus == StageStatus.PASSED && !typingCanceled)
                ProgressiveStageCard(
                    stageNumber = 4,
                    stageTitle = "ভাইভা ধাপ (Viva Voce)",
                    isUnlocked = isVivaUnlocked,
                    unlockRequirementText = "টাইপিং ধাপে 'পাস করেছেন: হ্যাঁ' সিলেক্ট করলে ভাইভা ধাপটি খুলবে।",
                    isCanceled = vivaCanceled,
                    onToggleCanceled = { vivaCanceled = it },
                    examDate = vivaDate,
                    onDateChange = { vivaDate = it },
                    onPickDate = { showDatePicker(vivaDate) { vivaDate = it } },
                    rating = vivaRating,
                    onRatingChange = { vivaRating = it },
                    obtainedMarks = vivaObtainedMarks,
                    onObtainedMarksChange = { vivaObtainedMarks = it },
                    totalMarks = vivaTotalMarks,
                    onTotalMarksChange = { vivaTotalMarks = it },
                    status = vivaStatus,
                    onStatusChange = { vivaStatus = it }
                )
            }

            // Save / Update Button
            val hasCanceledAny = isOverallCanceled || mcqCanceled || writtenCanceled || typingCanceled || vivaCanceled

            Button(
                onClick = {
                    if (orgName.isNotBlank() && postName.isNotBlank()) {
                        val updatedApp = JobApplication(
                            id = editingApp?.id ?: 0,
                            applyDate = applyDate,
                            day = day,
                            orgName = orgName.trim(),
                            postName = postName.trim(),
                            userId = userId.trim(),
                            password = password.trim(),
                            mcq = StageInfo(
                                status = if (mcqCanceled) StageStatus.CANCELED else mcqStatus,
                                date = mcqDate,
                                rating = mcqRating,
                                obtainedMarks = mcqObtainedMarks.trim(),
                                totalMarks = mcqTotalMarks.trim(),
                                isCanceled = mcqCanceled
                            ),
                            written = StageInfo(
                                status = if (writtenCanceled) StageStatus.CANCELED else writtenStatus,
                                date = writtenDate,
                                rating = writtenRating,
                                obtainedMarks = writtenObtainedMarks.trim(),
                                totalMarks = writtenTotalMarks.trim(),
                                isCanceled = writtenCanceled
                            ),
                            typing = StageInfo(
                                status = if (typingCanceled) StageStatus.CANCELED else typingStatus,
                                date = typingDate,
                                rating = typingRating,
                                obtainedMarks = typingObtainedMarks.trim(),
                                totalMarks = typingTotalMarks.trim(),
                                isCanceled = typingCanceled
                            ),
                            viva = StageInfo(
                                status = if (vivaCanceled) StageStatus.CANCELED else vivaStatus,
                                date = vivaDate,
                                rating = vivaRating,
                                obtainedMarks = vivaObtainedMarks.trim(),
                                totalMarks = vivaTotalMarks.trim(),
                                isCanceled = vivaCanceled
                            ),
                            appPdfUrl = editingApp?.appPdfUrl ?: "",
                            admitPdfUrl = editingApp?.admitPdfUrl ?: "",
                            localAppPdfUri = editingApp?.localAppPdfUri ?: "",
                            localAdmitPdfUri = editingApp?.localAdmitPdfUri ?: "",
                            isCanceled = hasCanceledAny,
                            syncStatus = "Saved"
                        )
                        onSave(updatedApp, appPdfUri, admitPdfUri)
                    }
                },
                enabled = orgName.isNotBlank() && postName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_application_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isNewApplication) "আবেদন সংরক্ষণ করুন (Save Application)" else "তথ্য ও ফলাফল আপডেট করুন (Update)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressiveStageCard(
    stageNumber: Int,
    stageTitle: String,
    isUnlocked: Boolean,
    unlockRequirementText: String = "",
    isCanceled: Boolean,
    onToggleCanceled: (Boolean) -> Unit,
    examDate: String,
    onDateChange: (String) -> Unit,
    onPickDate: () -> Unit,
    rating: String,
    onRatingChange: (String) -> Unit,
    obtainedMarks: String,
    onObtainedMarksChange: (String) -> Unit,
    totalMarks: String,
    onTotalMarksChange: (String) -> Unit,
    status: StageStatus,
    onStatusChange: (StageStatus) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isUnlocked) 2.dp else 0.dp),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) EmeraldGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stage Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isUnlocked) EmeraldGreen else Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stageNumber.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stageTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) EmeraldGreen else Color.Gray
                        )
                    )
                }

                if (!isUnlocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("লক করা", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            if (!isUnlocked) {
                Text(
                    text = unlockRequirementText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else {
                // Cancel toggle: "বাতিল হয়েছে?"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isCanceled) StatusFailed.copy(alpha = 0.1f) else Color(0xFFF8F9FA),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "বাতিল হয়েছে? (Is Canceled?)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isCanceled) StatusFailed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCanceled) "হ্যাঁ সিলেক্ট করায় এই আবেদন বাতিল তালিকায় যাবে" else "না (পরিক্ষার তথ্য ও ফলাফল লিখুন)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCanceled) StatusFailed else Color.Gray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = !isCanceled,
                            onClick = { onToggleCanceled(false) },
                            label = { Text("না (No)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen.copy(alpha = 0.15f),
                                selectedLabelColor = EmeraldGreen
                            )
                        )
                        FilterChip(
                            selected = isCanceled,
                            onClick = { onToggleCanceled(true) },
                            label = { Text("হ্যাঁ (Yes)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusFailed.copy(alpha = 0.2f),
                                selectedLabelColor = StatusFailed
                            )
                        )
                    }
                }

                // If not canceled, show exam date, rating, marks, auto percentage, and pass selection
                AnimatedVisibility(visible = !isCanceled) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Exam Date & How was the exam
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = examDate,
                                onValueChange = onDateChange,
                                label = { Text("পরীক্ষার তারিখ (Date)") },
                                placeholder = { Text("YYYY-MM-DD") },
                                trailingIcon = {
                                    IconButton(onClick = onPickDate) {
                                        Icon(Icons.Default.CalendarMonth, null, tint = EmeraldGreen)
                                    }
                                },
                                modifier = Modifier.weight(1.1f),
                                singleLine = true
                            )

                            // Rating Dropdown (কেমন হয়েছে?: খুব ভালো, ভালো, মাঝারি, খারাপ, খুব খারাপ)
                            var ratingExpanded by remember { mutableStateOf(false) }
                            val ratings = listOf("খুব ভালো (Very Good)", "ভালো (Good)", "মাঝারি (Medium)", "খারাপ (Bad)", "খুব খারাপ (Very Bad)")

                            ExposedDropdownMenuBox(
                                expanded = ratingExpanded,
                                onExpandedChange = { ratingExpanded = it },
                                modifier = Modifier.weight(1.1f)
                            ) {
                                OutlinedTextField(
                                    value = rating,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("কেমন হয়েছে?") },
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
                                                onRatingChange(r.substringBefore(" ("))
                                                ratingExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Marks: Obtained Marks, Total Marks, and Auto Percentage
                        val obt = obtainedMarks.toDoubleOrNull()
                        val tot = totalMarks.toDoubleOrNull()
                        val autoPercentage: String = if (obt != null && tot != null && tot > 0) {
                            String.format(Locale.US, "%.1f%%", (obt / tot) * 100.0)
                        } else {
                            "—"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = obtainedMarks,
                                onValueChange = onObtainedMarksChange,
                                label = { Text("প্রাপ্ত নম্বর") },
                                placeholder = { Text("যেমন: 75") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = totalMarks,
                                onValueChange = onTotalMarksChange,
                                label = { Text("মোট নম্বর") },
                                placeholder = { Text("যেমন: 100") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // Auto Percentage Box
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "পার্সেন্টেজ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        text = autoPercentage,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    )
                                }
                            }
                        }

                        // Did you pass? (পাশ করেছেন - হ্যাঁ বা না - এইটা পরে সিলেক্ট করার জন্য)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "পাশ করেছেন? (Did you pass?)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = status == StageStatus.APPEARED_PENDING_RESULT || status == StageStatus.NO,
                                    onClick = { onStatusChange(StageStatus.APPEARED_PENDING_RESULT) },
                                    label = { Text("ফলাফল অপেক্ষমাণ") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StatusPending.copy(alpha = 0.15f),
                                        selectedLabelColor = StatusPending
                                    ),
                                    modifier = Modifier.weight(1.3f)
                                )

                                FilterChip(
                                    selected = status == StageStatus.PASSED,
                                    onClick = { onStatusChange(StageStatus.PASSED) },
                                    label = { Text("হ্যাঁ (পাস)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen.copy(alpha = 0.15f),
                                        selectedLabelColor = EmeraldGreen
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                FilterChip(
                                    selected = status == StageStatus.FAILED,
                                    onClick = { onStatusChange(StageStatus.FAILED) },
                                    label = { Text("না (ফেল)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StatusFailed.copy(alpha = 0.15f),
                                        selectedLabelColor = StatusFailed
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
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
                        text = if (fileName.isNotBlank()) fileName else "কোনো ফাইল নির্বাচন করা হয়নি",
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
