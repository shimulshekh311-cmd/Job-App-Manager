package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GitHubReleaseInfo
import com.example.data.remote.UpdateDownloadProgress
import com.example.ui.GitHubUpdateUiState
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StatusFailed
import java.io.File

@Composable
fun GitHubUpdateDialog(
    currentVersion: String,
    currentRepo: String,
    updateState: GitHubUpdateUiState,
    downloadProgress: UpdateDownloadProgress,
    onSaveRepo: (String) -> Unit,
    onCheckUpdate: () -> Unit,
    onStartDownload: (GitHubReleaseInfo) -> Unit,
    onInstallApk: (File) -> Unit,
    onDismiss: () -> Unit
) {
    var repoText by remember { mutableStateOf(currentRepo) }
    var isEditingRepo by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ইন-অ্যাপ আপডেট (GitHub)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "বর্তমান সংস্করণ: v$currentVersion",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "GitHub Releases থেকে নতুন কোড ও ফিচারের আপডেট সরাসরি এক ক্লিকে ডাউনলোড ও ইন্সটল করুন। ম্যানুয়ালি ডাউনলোড করার প্রয়োজন নেই।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Repository Selection Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GitHub Repository:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { isEditingRepo = !isEditingRepo },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEditingRepo) "Done" else "পরিবর্তন", fontSize = 11.sp)
                            }
                        }

                        if (isEditingRepo) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = repoText,
                                onValueChange = {
                                    repoText = it
                                    onSaveRepo(it)
                                },
                                label = { Text("username/repository") },
                                placeholder = { Text("shimulshekh311/job-app-manager") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("github_repo_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        } else {
                            Text(
                                text = repoText.ifBlank { "shimulshekh311/job-app-manager" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldGreen
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // State Views
                when (updateState) {
                    is GitHubUpdateUiState.Idle -> {
                        // Prompt to check
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = onCheckUpdate,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_check_update_now")
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("আপডেট চেক করুন (Check Update)")
                            }
                        }
                    }

                    is GitHubUpdateUiState.Checking -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = EmeraldGreen,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "GitHub-এ নতুন সংস্করণ খোঁজা হচ্ছে...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    is GitHubUpdateUiState.UpToDate -> {
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "আপনার অ্যাপটি আপ-টু-ডেট!",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    )
                                    Text(
                                        text = "সর্বশেষ সংস্করণ (${updateState.latestTag}) সক্রিয় আছে। কোনো নতুন আপডেটের প্রয়োজন নেই।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = onCheckUpdate,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পুনরায় চেক করুন")
                        }
                    }

                    is GitHubUpdateUiState.Available -> {
                        val release = updateState.release
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "নতুন সংস্করণ পাওয়া গেছে! 🎉",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    )
                                    Surface(
                                        color = EmeraldGreen,
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = release.tagName,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = release.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )

                                if (release.apkSizeBytes > 0) {
                                    val sizeMb = String.format("%.1f MB", release.apkSizeBytes / (1024.0 * 1024.0))
                                    Text(
                                        text = "APK সাইজ: $sizeMb",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Changelog Box
                                Text(
                                    text = "নতুন পরিবর্তনসমূহ:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = release.changelog.ifBlank { "বাগ ফিক্স এবং কার্যক্ষমতা উন্নত করা হয়েছে।" },
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 6
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Download progress or download button
                                when (downloadProgress) {
                                    is UpdateDownloadProgress.Idle -> {
                                        if (!release.apkDownloadUrl.isNullOrBlank()) {
                                            Button(
                                                onClick = { onStartDownload(release) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("btn_download_update")
                                            ) {
                                                Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("ডাউনলোড ও আপডেট করুন", fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Text(
                                                text = "এই রিলিজে কোনো .apk ফাইল সংযুক্ত নেই। অনুগ্রহ করে GitHub-এ যান।",
                                                color = StatusFailed,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OutlinedButton(
                                                onClick = { uriHandler.openUri(release.htmlUrl) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("GitHub Release ওপেন করুন")
                                            }
                                        }
                                    }

                                    is UpdateDownloadProgress.Connecting -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            LinearProgressIndicator(
                                                modifier = Modifier.fillMaxWidth(),
                                                color = EmeraldGreen
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("GitHub থেকে ডাউনলোড কানেক্ট হচ্ছে...", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    is UpdateDownloadProgress.Downloading -> {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            if (downloadProgress.percent >= 0) {
                                                LinearProgressIndicator(
                                                    progress = { downloadProgress.percent / 100f },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = EmeraldGreen
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                val downloadedMb = String.format("%.1f", downloadProgress.bytesDownloaded / (1024.0 * 1024.0))
                                                val totalMb = String.format("%.1f", downloadProgress.totalBytes / (1024.0 * 1024.0))
                                                Text(
                                                    text = "${downloadProgress.percent}% ($downloadedMb MB / $totalMb MB)",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            } else {
                                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = EmeraldGreen)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                val downloadedMb = String.format("%.1f", downloadProgress.bytesDownloaded / (1024.0 * 1024.0))
                                                Text(text = "ডাউনলোড হচ্ছে: $downloadedMb MB", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }

                                    is UpdateDownloadProgress.Completed -> {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Surface(
                                                color = EmeraldGreen.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, null, tint = EmeraldGreen)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "APK ডাউনলোড সম্পন্ন হয়েছে!",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = EmeraldGreen
                                                        )
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { onInstallApk(downloadProgress.file) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("btn_install_apk")
                                            ) {
                                                Icon(Icons.Default.SystemUpdate, null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("এখনই ইন্সটল করুন (Install APK)", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    is UpdateDownloadProgress.Error -> {
                                        Text(
                                            text = "ডাউনলোড ব্যর্থ: ${downloadProgress.message}",
                                            color = StatusFailed,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedButton(
                                            onClick = { onStartDownload(release) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("পুনরায় ডাউনলোড করুন")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is GitHubUpdateUiState.Error -> {
                        Surface(
                            color = StatusFailed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null, tint = StatusFailed, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "আপডেট চেক ব্যর্থ হয়েছে",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StatusFailed
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = updateState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onCheckUpdate,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আবার চেষ্টা করুন")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_update_dialog")
            ) {
                Text("বন্ধ করুন (Close)")
            }
        }
    )
}
