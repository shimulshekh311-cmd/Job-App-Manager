package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SyncUiState
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StatusFailed

@Composable
fun GoogleSyncDialog(
    initialUrl: String,
    syncState: SyncUiState,
    onSaveUrl: (String) -> Unit,
    onSyncNow: () -> Unit,
    onOpenCodeHelper: () -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }
    val isConfigured = urlText.isNotBlank() && urlText.startsWith("https://script.google.com")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Google Sheets & Drive Sync",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Connect directly with your existing Google Sheet (Database) and Google Drive folder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sheet & Drive Target Info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active Sheet: Database",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, null, tint = ActionBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Drive Folder: 1XaGrZw4...JH0fx",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Web App URL input
                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        onSaveUrl(it)
                    },
                    label = { Text("Google Apps Script Web App URL") },
                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, tint = EmeraldGreen)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("web_app_url_input"),
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sync status indicator
                when (syncState) {
                    is SyncUiState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = EmeraldGreen,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing with Google Sheets...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is SyncUiState.Success -> {
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(syncState.message, color = EmeraldGreen, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    is SyncUiState.Error -> {
                        Surface(
                            color = StatusFailed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = StatusFailed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(syncState.message, color = StatusFailed, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Button to open Code.gs Helper
                OutlinedButton(
                    onClick = onOpenCodeHelper,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_code_helper_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Code, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View & Copy Code.gs Script")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveUrl(urlText)
                    onSyncNow()
                },
                enabled = isConfigured && syncState !is SyncUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("sync_now_btn")
            ) {
                Icon(Icons.Default.CloudDone, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Now")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_sync_dialog_btn")
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun CodeHelperDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scriptCode = """
// --- Updated Code.gs (Supports both Web Browser & Android Native App) ---

function doGet(e) {
  // If called by Android app to fetch sheet rows
  if (e && e.parameter && e.parameter.action === 'getData') {
    var data = getData();
    return ContentService.createTextOutput(JSON.stringify({ status: "success", data: data }))
      .setMimeType(ContentService.MimeType.JSON);
  }
  // Otherwise serve standard Web App HTML in browser
  return HtmlService.createTemplateFromFile('Index')
   .evaluate()
   .setTitle('Job App Manager')
   .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
   .addMetaTag('viewport', 'width=1200');
}

function doPost(e) {
  try {
    var body = JSON.parse(e.postData.contents);
    var action = body.action;
    if (action === 'processForm') {
      var res = processForm(body.formObject);
      return ContentService.createTextOutput(JSON.stringify({ status: "success", result: res }))
        .setMimeType(ContentService.MimeType.JSON);
    } else if (action === 'deleteRow') {
      var res = deleteRow(body.org, body.post);
      return ContentService.createTextOutput(JSON.stringify({ status: "success", result: res }))
        .setMimeType(ContentService.MimeType.JSON);
    } else if (action === 'deleteDriveFiles') {
      var res = deleteDriveFiles(body.org, body.post, body.appUrl, body.admitUrl);
      return ContentService.createTextOutput(JSON.stringify({ status: "success", result: res }))
        .setMimeType(ContentService.MimeType.JSON);
    } else if (action === 'getData') {
      var data = getData();
      return ContentService.createTextOutput(JSON.stringify({ status: "success", data: data }))
        .setMimeType(ContentService.MimeType.JSON);
    }
    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "Unknown action" }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

// Your existing processForm, deleteRow, deleteDriveFiles, getData functions remain unchanged below!
""".trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Apps Script Guide", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add these doGet(e) and doPost(e) handlers to your Code.gs in Google Apps Script. It keeps your web app working while enabling the Android app to sync with the same Sheet & Drive folder!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF212529), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = scriptCode,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF75B798)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Deployment Steps:\n1. Open your Apps Script project\n2. Replace doGet() and add doPost() from above\n3. Click 'Deploy' > 'Manage deployments' > Edit > New version > Deploy\n4. Copy the Web app URL and paste it in the Android App!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code.gs script", scriptCode))
                    Toast.makeText(context, "Code.gs copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("copy_script_btn")
            ) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Code")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_code_helper_btn")
            ) {
                Text("Close")
            }
        }
    )
}
