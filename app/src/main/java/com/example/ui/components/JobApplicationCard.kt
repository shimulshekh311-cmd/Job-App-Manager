package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobApplicationCard(
    application: JobApplication,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClearFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val categoryColor = when (application.category) {
        ExamCategory.COMPLETED -> EmeraldGreen
        ExamCategory.RUNNING -> ActionBlue
        ExamCategory.UPCOMING -> Color(0xFF6F42C1)
        ExamCategory.FAILED -> StatusFailed
        ExamCategory.CANCELED -> StatusCanceled
        ExamCategory.ALL -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_card_${application.orgName}_${application.postName}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Header Row: Organization & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = application.orgName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = application.postName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Category Tag
                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = application.category.displayName(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Apply Date & Day tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Applied: ${application.applyDate.ifBlank { "N/A" }} (${application.day.ifBlank { "N/A" }})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (application.isCanceled) {
                    Text(
                        text = "• CANCELED",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = StatusCanceled
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Stages Chips Grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StageChip(label = "MCQ", stage = application.mcq)
                StageChip(label = "Written", stage = application.written)
                StageChip(label = "Typing", stage = application.typing)
                StageChip(label = "Viva", stage = application.viva)
            }

            // Expandable Details (Credentials & Documents)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Credentials Box
                    if (application.userId.isNotBlank() || application.password.isNotBlank()) {
                        CredentialsBox(
                            userId = application.userId,
                            password = application.password,
                            isPasswordVisible = isPasswordVisible,
                            onTogglePassword = { isPasswordVisible = !isPasswordVisible },
                            onCopy = { text, label ->
                                copyToClipboard(context, text, label)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Attached Files Section (Google Drive & Local PDFs)
                    DocumentsRow(
                        application = application,
                        onOpenUrl = { url ->
                            openLinkInBrowser(context, url)
                        },
                        onDownloadUrl = { url ->
                            val downloadUrl = getDirectDownloadUrl(url)
                            openLinkInBrowser(context, downloadUrl)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Expand / Collapse toggle button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide details" else "View credentials & files",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Action Buttons: Edit, Delete, Clear Drive Files
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (application.appPdfUrl.isNotBlank() || application.admitPdfUrl.isNotBlank() ||
                        application.category == ExamCategory.FAILED || application.category == ExamCategory.CANCELED
                    ) {
                        IconButton(
                            onClick = onClearFiles,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_files_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Drive Files",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_app_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Application",
                            tint = Color(0xFFD39E00), // Amber/Warning color matching web app
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_app_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Application",
                            tint = StatusFailed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageChip(
    label: String,
    stage: StageInfo
) {
    val (bgColor, textColor, icon) = when (stage.status) {
        StageStatus.PASSED -> Triple(EmeraldGreen.copy(alpha = 0.12f), EmeraldGreen, Icons.Default.CheckCircle)
        StageStatus.FAILED -> Triple(StatusFailed.copy(alpha = 0.12f), StatusFailed, Icons.Default.Error)
        StageStatus.APPEARED_PENDING_RESULT -> Triple(StatusPending.copy(alpha = 0.15f), StatusPending, Icons.Default.HourglassEmpty)
        StageStatus.CANCELED -> Triple(Color(0xFFE2E3E5), Color(0xFF6C757D), Icons.Default.Cancel)
        StageStatus.NO -> Triple(Color(0xFFE9ECEF), Color(0xFF6C757D), Icons.Default.HourglassEmpty)
    }

    val marksPart = if (stage.obtainedMarks.isNotBlank() && stage.totalMarks.isNotBlank()) {
        " • ${stage.obtainedMarks}/${stage.totalMarks} (${stage.formattedPercentage})"
    } else ""

    val detail = when (stage.status) {
        StageStatus.PASSED -> {
            val r = if (stage.rating.isNotBlank()) " (${stage.rating})" else ""
            val d = if (stage.date.isNotBlank()) " [${stage.date}]" else ""
            "$label: ✅ $r$marksPart$d"
        }
        StageStatus.FAILED -> "$label: ❌ ফেল$marksPart"
        StageStatus.APPEARED_PENDING_RESULT -> {
            val r = if (stage.rating.isNotBlank()) " (${stage.rating})" else ""
            "$label: ⏳ অপেক্ষমাণ$r$marksPart"
        }
        StageStatus.CANCELED -> "$label: ⚠️ বাতিল"
        StageStatus.NO -> "$label: ⏳ বাকি"
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

@Composable
private fun CredentialsBox(
    userId: String,
    password: String,
    isPasswordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Application Credentials",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            // User ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "User ID: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = userId.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
                if (userId.isNotBlank()) {
                    IconButton(
                        onClick = { onCopy(userId, "User ID") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy User ID",
                            modifier = Modifier.size(16.dp),
                            tint = ActionBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Password: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (isPasswordVisible) password.ifBlank { "—" } else "••••••••",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (password.isNotBlank()) {
                        IconButton(
                            onClick = onTogglePassword,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Password Visibility",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onCopy(password, "Password") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Password",
                                modifier = Modifier.size(16.dp),
                                tint = ActionBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentsRow(
    application: JobApplication,
    onOpenUrl: (String) -> Unit,
    onDownloadUrl: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Attached Documents",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // App PDF
            FileActionPill(
                label = "App PDF",
                url = application.appPdfUrl,
                localUri = application.localAppPdfUri,
                onOpen = onOpenUrl,
                onDownload = onDownloadUrl,
                modifier = Modifier.weight(1f)
            )

            // Admit PDF
            FileActionPill(
                label = "Admit Card",
                url = application.admitPdfUrl,
                localUri = application.localAdmitPdfUri,
                onOpen = onOpenUrl,
                onDownload = onDownloadUrl,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FileActionPill(
    label: String,
    url: String,
    localUri: String,
    onOpen: (String) -> Unit,
    onDownload: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasUrl = url.isNotBlank() && url.startsWith("http")
    val hasLocal = localUri.isNotBlank()

    Surface(
        modifier = modifier,
        color = if (hasUrl || hasLocal) ActionBlue.copy(alpha = 0.08f) else Color(0xFFF1F3F5),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (hasUrl || hasLocal) ActionBlue.copy(alpha = 0.3f) else Color(0xFFE9ECEF)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
                    tint = if (hasUrl || hasLocal) ActionBlue else Color(0xFFADB5BD),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (hasUrl || hasLocal) ActionBlue else Color(0xFF6C757D),
                    maxLines = 1
                )
            }

            val target = if (hasUrl) url else localUri
            if (hasUrl || hasLocal) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ActionBlue.copy(alpha = 0.12f))
                        .border(1.dp, ActionBlue, RoundedCornerShape(4.dp))
                        .clickable { onOpen(target) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ActionBlue
                        )
                    )
                }
            } else {
                Text(
                    text = "None",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFADB5BD)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun openLinkInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open URL: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun getDirectDownloadUrl(url: String): String {
    val match = Regex("""[-\w]{25,}""").find(url)
    val fileId = match?.value ?: ""
    return if (fileId.isNotBlank()) {
        "https://docs.google.com/uc?export=download&id=$fileId"
    } else {
        url
    }
}
