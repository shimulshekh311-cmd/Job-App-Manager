package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfHelper {

    /**
     * Copies a Uri stream into the app's internal "pdfs" storage directory,
     * ensuring persistent access even if the original content URI permission expires.
     */
    fun copyUriToAppStorage(context: Context, sourceUri: Uri, prefix: String): File? {
        return try {
            val pdfsDir = File(context.filesDir, "pdfs")
            if (!pdfsDir.exists()) {
                pdfsDir.mkdirs()
            }
            val originalName = getFileName(context, sourceUri)
            val sanitizedName = originalName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val targetFile = File(pdfsDir, "${prefix}_${System.currentTimeMillis()}_$sanitizedName")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resolves a File from a local URI string (file:// or content:// or path).
     */
    fun getFileFromUriString(context: Context, uriString: String): File? {
        if (uriString.isBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            when (uri.scheme) {
                "file" -> uri.path?.let { File(it) }
                "content" -> {
                    // Check if it's already one of our app files
                    val pdfsDir = File(context.filesDir, "pdfs")
                    if (pdfsDir.exists()) {
                        val matchingFile = pdfsDir.listFiles()?.firstOrNull { file ->
                            uriString.contains(file.name)
                        }
                        if (matchingFile != null && matchingFile.exists()) {
                            return matchingFile
                        }
                    }
                    // Alternatively, copy it to a cache file
                    copyUriToAppStorage(context, uri, "cached_view")
                }
                null -> {
                    val f = File(uriString)
                    if (f.exists()) f else null
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets display file name from Uri.
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "document.pdf"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val displayName = it.getString(nameIndex)
                        if (!displayName.isNullOrBlank()) {
                            name = displayName
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (name == "document.pdf") {
            val lastSegment = uri.lastPathSegment?.substringAfterLast('/')
            if (!lastSegment.isNullOrBlank()) {
                name = lastSegment
            }
        }
        return name
    }

    /**
     * Returns a FileProvider content:// Uri for a File.
     */
    fun getFileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Opens PDF in external application or browser with appropriate MIME type & permissions.
     */
    fun openPdfExternally(context: Context, uriOrUrl: String) {
        if (uriOrUrl.isBlank()) {
            Toast.makeText(context, "কোনো ফাইল নেই (No file)", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = Uri.parse(uriOrUrl)
            if (uriOrUrl.startsWith("http://", ignoreCase = true) || uriOrUrl.startsWith("https://", ignoreCase = true)) {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                return
            }

            // Local file or content URI
            val contentUri: Uri = if (uri.scheme == "content") {
                uri
            } else {
                val file = if (uri.scheme == "file") File(uri.path ?: "") else File(uriOrUrl)
                if (file.exists()) {
                    getFileProviderUri(context, file)
                } else {
                    uri
                }
            }

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "পিডিএফ দেখার জন্য কোনো অ্যাপ পাওয়া যায়নি (No PDF viewer found)",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
