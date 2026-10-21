package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamCategory
import com.example.data.model.JobApplication
import com.example.data.model.StageInfo
import com.example.data.model.StageStatus
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StatusCanceled
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending

@Composable
fun FiveTablesSection(
    allApplications: List<JobApplication>,
    onEdit: (JobApplication) -> Unit,
    onDelete: (JobApplication) -> Unit,
    onClearFiles: (JobApplication) -> Unit,
    modifier: Modifier = Modifier
) {
    val upcoming = allApplications.filter { it.category == ExamCategory.UPCOMING }
    val running = allApplications.filter { it.category == ExamCategory.RUNNING }
    val completed = allApplications.filter { it.category == ExamCategory.COMPLETED }
    val failed = allApplications.filter { it.category == ExamCategory.FAILED }
    val canceled = allApplications.filter { it.category == ExamCategory.CANCELED }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Table 1: Upcoming Exams
        CategoryTableBlock(
            title = "1. Upcoming Exams (আসন্ন পরীক্ষা)",
            accentColor = EmeraldGreen,
            applications = upcoming,
            onEdit = onEdit,
            onDelete = onDelete,
            onClearFiles = onClearFiles
        )

        // Table 2: Running Exams
        CategoryTableBlock(
            title = "2. Running Exams (চলমান পরীক্ষা)",
            accentColor = ActionBlue,
            applications = running,
            onEdit = onEdit,
            onDelete = onDelete,
            onClearFiles = onClearFiles
        )

        // Table 3: Fully Completed
        CategoryTableBlock(
            title = "3. Fully Completed (সম্পূর্ণ হয়েছে)",
            accentColor = Color(0xFF198754),
            applications = completed,
            onEdit = onEdit,
            onDelete = onDelete,
            onClearFiles = onClearFiles
        )

        // Table 4: Failed Applications
        CategoryTableBlock(
            title = "4. Failed Applications (ব্যর্থ আবেদন)",
            accentColor = StatusFailed,
            applications = failed,
            onEdit = onEdit,
            onDelete = onDelete,
            onClearFiles = onClearFiles
        )

        // Table 5: Canceled Applications
        CategoryTableBlock(
            title = "5. Canceled Applications (বাতিল আবেদন)",
            accentColor = StatusCanceled,
            applications = canceled,
            onEdit = onEdit,
            onDelete = onDelete,
            onClearFiles = onClearFiles
        )
    }
}

@Composable
fun CategoryTableBlock(
    title: String,
    accentColor: Color,
    applications: List<JobApplication>,
    onEdit: (JobApplication) -> Unit,
    onDelete: (JobApplication) -> Unit,
    onClearFiles: (JobApplication) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(24.dp)
                    .background(accentColor, RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${applications.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        if (applications.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "কোনো আবেদন নেই (No applications in this category)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Horizontal Scrollable Table Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Column {
                        // Table Header Row
                        TableHeaderRow()

                        // Table Body Rows
                        applications.forEachIndexed { index, app ->
                            TableRowItem(
                                index = index,
                                application = app,
                                onEdit = { onEdit(app) },
                                onDelete = { onDelete(app) },
                                onClearFiles = { onClearFiles(app) }
                            )
                            if (index < applications.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fixed Column Widths for alignment
private val COL_ACTION_WIDTH = 130.dp
private val COL_DATE_WIDTH = 120.dp
private val COL_DAY_WIDTH = 100.dp
private val COL_ORG_WIDTH = 180.dp
private val COL_POST_WIDTH = 180.dp
private val COL_USERID_WIDTH = 130.dp
private val COL_PASSWORD_WIDTH = 130.dp
private val COL_PDF_WIDTH = 120.dp
private val COL_STAGE_WIDTH = 220.dp

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .background(EmeraldGreen)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("অ্যাকশন (Actions)", COL_ACTION_WIDTH)
        HeaderCell("আবেদনের তারিখ", COL_DATE_WIDTH)
        HeaderCell("বার (Day)", COL_DAY_WIDTH)
        HeaderCell("প্রতিষ্ঠানের নাম", COL_ORG_WIDTH)
        HeaderCell("পদের নাম", COL_POST_WIDTH)
        HeaderCell("ইউজার আইডি", COL_USERID_WIDTH)
        HeaderCell("পাসওয়ার্ড", COL_PASSWORD_WIDTH)
        HeaderCell("আবেদন PDF", COL_PDF_WIDTH)
        HeaderCell("অ্যাডমিট PDF", COL_PDF_WIDTH)
        HeaderCell("MCQ (তারিখ, নম্বর, রেজাল্ট)", COL_STAGE_WIDTH)
        HeaderCell("Written (তারিখ, নম্বর, রেজাল্ট)", COL_STAGE_WIDTH)
        HeaderCell("Typing (তারিখ, নম্বর, রেজাল্ট)", COL_STAGE_WIDTH)
        HeaderCell("Viva (তারিখ, নম্বর, রেজাল্ট)", COL_STAGE_WIDTH)
    }
}

@Composable
private fun HeaderCell(title: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun TableRowItem(
    index: Int,
    application: JobApplication,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClearFiles: () -> Unit
) {
    val context = LocalContext.current
    val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF9FBF9)

    Row(
        modifier = Modifier
            .background(rowBg)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Action Icons Column (Edit, Delete, Clear Drive Files)
        Box(
            modifier = Modifier
                .width(COL_ACTION_WIDTH)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Edit Icon
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("table_edit_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFFD39E00),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("table_delete_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = StatusFailed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // File Delete Icon (🗑️ Files)
                if (application.appPdfUrl.isNotBlank() || application.admitPdfUrl.isNotBlank() ||
                    application.category == ExamCategory.FAILED || application.category == ExamCategory.CANCELED
                ) {
                    IconButton(
                        onClick = onClearFiles,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("table_clear_files_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Drive Files",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Apply Date
        TextCell(text = application.applyDate.ifBlank { "—" }, width = COL_DATE_WIDTH)

        // Day
        TextCell(text = application.day.ifBlank { "—" }, width = COL_DAY_WIDTH)

        // Org Name
        TextCell(
            text = application.orgName,
            width = COL_ORG_WIDTH,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Post Name
        TextCell(
            text = application.postName,
            width = COL_POST_WIDTH,
            fontWeight = FontWeight.SemiBold
        )

        // User ID with copy button
        CopyableTextCell(
            text = application.userId,
            width = COL_USERID_WIDTH,
            label = "User ID",
            context = context
        )

        // Password with copy button
        CopyableTextCell(
            text = application.password,
            width = COL_PASSWORD_WIDTH,
            label = "Password",
            context = context
        )

        // App PDF Cell
        PdfCell(
            url = application.appPdfUrl,
            localUri = application.localAppPdfUri,
            width = COL_PDF_WIDTH,
            context = context
        )

        // Admit PDF Cell
        PdfCell(
            url = application.admitPdfUrl,
            localUri = application.localAdmitPdfUri,
            width = COL_PDF_WIDTH,
            context = context
        )

        // MCQ Stage Cell
        StageTableCell(stage = application.mcq, width = COL_STAGE_WIDTH)

        // Written Stage Cell
        StageTableCell(stage = application.written, width = COL_STAGE_WIDTH)

        // Typing Stage Cell
        StageTableCell(stage = application.typing, width = COL_STAGE_WIDTH)

        // Viva Stage Cell
        StageTableCell(stage = application.viva, width = COL_STAGE_WIDTH)
    }
}

@Composable
private fun TextCell(
    text: String,
    width: Dp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = fontWeight),
            color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
        )
    }
}

@Composable
private fun CopyableTextCell(
    text: String,
    width: Dp,
    label: String,
    context: Context
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (text.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText(label, text))
                        Toast.makeText(context, "$label কপি হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = ActionBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else {
            Text("—", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun PdfCell(
    url: String,
    localUri: String,
    width: Dp,
    context: Context
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val hasUrl = url.isNotBlank() && url.startsWith("http")
        val hasLocal = localUri.isNotBlank()

        if (hasUrl) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // View
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, ActionBlue, RoundedCornerShape(4.dp))
                        .clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "URL খোলা যাচ্ছে না", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "View",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ActionBlue
                        )
                    )
                }

                // Download (DL)
                val match = Regex("""[-\w]{25,}""").find(url)
                val fileId = match?.value ?: ""
                val dlUrl = if (fileId.isNotBlank()) "https://docs.google.com/uc?export=download&id=$fileId" else url

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldGreen)
                        .clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(dlUrl)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "ডাউনলোড লিংক খোলা যাচ্ছে না", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        } else if (hasLocal) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ActionBlue)
                    .clickable {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(localUri)))
                        } catch (e: Exception) {
                            Toast.makeText(context, "ফাইল খোলা যাচ্ছে না", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Open",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        } else {
            Text("—", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun StageTableCell(
    stage: StageInfo,
    width: Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (stage.isCanceled || stage.status == StageStatus.CANCELED) {
            Surface(
                color = Color(0xFFE2E3E5),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "⚠️ বাতিল (Canceled)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF6C757D),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else if (stage.status == StageStatus.NO) {
            Text("⏳ No", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            val (icon, tint, statusText) = when (stage.status) {
                StageStatus.PASSED -> Triple(Icons.Default.CheckCircle, EmeraldGreen, "পাস")
                StageStatus.FAILED -> Triple(Icons.Default.Error, StatusFailed, "ফেল")
                StageStatus.APPEARED_PENDING_RESULT -> Triple(Icons.Default.HourglassEmpty, StatusPending, "অপেক্ষমাণ")
                else -> Triple(Icons.Default.HourglassEmpty, Color.Gray, "No")
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Status & Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$statusText (${stage.rating.ifBlank { "ভালো" }})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = tint
                        )
                    )
                }

                // Date
                if (stage.date.isNotBlank()) {
                    Text(
                        text = "📅 ${stage.date}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Marks and Percentage
                if (stage.obtainedMarks.isNotBlank() && stage.totalMarks.isNotBlank()) {
                    Surface(
                        color = tint.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "নম্বর: ${stage.obtainedMarks}/${stage.totalMarks} (${stage.formattedPercentage})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = tint
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
