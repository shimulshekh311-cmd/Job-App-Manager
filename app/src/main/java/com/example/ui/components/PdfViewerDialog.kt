package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StatusFailed
import com.example.util.PdfHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfViewerDialog(
    title: String,
    targetFile: File? = null,
    targetUriOrUrl: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isWebUrl = targetUriOrUrl?.startsWith("http", ignoreCase = true) == true

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Dialog Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            tint = ActionBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "PDF প্রিভিউ ও ভিউয়ার (PDF Viewer)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Open externally button
                        val externalTarget = targetFile?.let { PdfHelper.getFileProviderUri(context, it).toString() }
                            ?: targetUriOrUrl
                        if (!externalTarget.isNullOrBlank()) {
                            IconButton(
                                onClick = { PdfHelper.openPdfExternally(context, externalTarget) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open externally",
                                    tint = ActionBlue
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (isWebUrl) {
                    // Google Drive or Remote URL view
                    RemotePdfNotice(
                        url = targetUriOrUrl ?: "",
                        context = context,
                        onDismiss = onDismiss
                    )
                } else {
                    // Local PDF Renderer View
                    LocalPdfRenderView(
                        targetFile = targetFile,
                        targetUriString = targetUriOrUrl,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun RemotePdfNotice(
    url: String,
    context: Context,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = ActionBlue,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Google Drive PDF ডকুমেন্ট",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Text(
                text = "এই ফাইলটি গুগল ড্রাইভে সংরক্ষিত আছে। নিচের বাটনে ক্লিক করে সরাসরি গুগল ড্রাইভ বা ব্রাউজারে এটি ওপেন করে দেখতে পারবেন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    PdfHelper.openPdfExternally(context, url)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("গুগল ড্রাইভে দেখুন (Open in Drive)")
            }
        }
    }
}

@Composable
private fun LocalPdfRenderView(
    targetFile: File?,
    targetUriString: String?,
    context: Context
) {
    var resolvedFile by remember(targetFile, targetUriString) {
        mutableStateOf(
            targetFile ?: targetUriString?.let { PdfHelper.getFileFromUriString(context, it) }
        )
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Zoom & pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        if (scale > 1f) {
            offset += panChange
        } else {
            offset = Offset.Zero
        }
    }

    // Function to render a specific page
    fun renderPage(file: File, pageIndex: Int) {
        isLoading = true
        errorMessage = null
        try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount

            if (pageCount > 0 && pageIndex in 0 until pageCount) {
                val page = renderer.openPage(pageIndex)
                // High quality rendering
                val width = page.width * 2
                val height = page.height * 2
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                canvas.drawColor(AndroidColor.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                currentBitmap = bmp
            }
            renderer.close()
            pfd.close()
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "পিডিএফ লোড করা যাচ্ছে না: ${e.message}"
            isLoading = false
        }
    }

    LaunchedEffect(resolvedFile, currentPageIndex) {
        val file = resolvedFile
        if (file != null && file.exists()) {
            withContext(Dispatchers.IO) {
                renderPage(file, currentPageIndex)
            }
        } else {
            // Check if we can copy from targetUriString
            if (!targetUriString.isNullOrBlank()) {
                withContext(Dispatchers.IO) {
                    try {
                        val uri = Uri.parse(targetUriString)
                        val copied = PdfHelper.copyUriToAppStorage(context, uri, "view_temp")
                        if (copied != null && copied.exists()) {
                            resolvedFile = copied
                            renderPage(copied, currentPageIndex)
                        } else {
                            errorMessage = "ফাইলটি খুঁজে পাওয়া যায়নি (File not found)"
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "ফাইলটি লোড ব্যর্থ: ${e.message}"
                        isLoading = false
                    }
                }
            } else {
                errorMessage = "কোনো ফাইল পাওয়া যায়নি (No PDF file found)"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF2B2B2B)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = "পিডিএফ প্রস্তুত হচ্ছে...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = StatusFailed,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                val target = resolvedFile?.let { PdfHelper.getFileProviderUri(context, it).toString() }
                                    ?: targetUriString ?: ""
                                PdfHelper.openPdfExternally(context, target)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                        ) {
                            Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("বাইরের অ্যাপে ওপেন করুন")
                        }
                    }
                }
                currentBitmap != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .transformable(state = transformableState),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page ${currentPageIndex + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // Bottom Page Navigation Bar
        if (pageCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Button
                IconButton(
                    onClick = {
                        if (currentPageIndex > 0) {
                            scale = 1f
                            offset = Offset.Zero
                            currentPageIndex--
                        }
                    },
                    enabled = currentPageIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Page",
                        tint = if (currentPageIndex > 0) ActionBlue else Color.Gray
                    )
                }

                // Page count info
                Text(
                    text = "পৃষ্ঠা ${currentPageIndex + 1} / $pageCount",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Reset zoom button if zoomed
                if (scale > 1f) {
                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Zoom",
                            tint = EmeraldGreen
                        )
                    }
                }

                // Next Button
                IconButton(
                    onClick = {
                        if (currentPageIndex < pageCount - 1) {
                            scale = 1f
                            offset = Offset.Zero
                            currentPageIndex++
                        }
                    },
                    enabled = currentPageIndex < pageCount - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Page",
                        tint = if (currentPageIndex < pageCount - 1) ActionBlue else Color.Gray
                    )
                }
            }
        }
    }
}
