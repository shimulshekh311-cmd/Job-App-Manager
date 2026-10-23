package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class GitHubReleaseInfo(
    val tagName: String,
    val title: String,
    val changelog: String,
    val publishedAt: String,
    val htmlUrl: String,
    val apkDownloadUrl: String?,
    val apkName: String?,
    val apkSizeBytes: Long,
    val isNewer: Boolean
)

sealed interface UpdateDownloadProgress {
    object Idle : UpdateDownloadProgress
    object Connecting : UpdateDownloadProgress
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long, val percent: Int) : UpdateDownloadProgress
    data class Completed(val file: File) : UpdateDownloadProgress
    data class Error(val message: String) : UpdateDownloadProgress
}

class GitHubUpdateManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("job_app_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    companion object {
        const val DEFAULT_REPO = "shimulshekh311-cmd/Job-App-Manager"

        fun cleanRepoString(input: String): String {
            return input.trim()
                .removePrefix("https://github.com/")
                .removePrefix("http://github.com/")
                .removePrefix("github.com/")
                .removeSuffix(".git")
                .removeSuffix("/")
                .trim()
        }
    }

    // Default or user-configured GitHub repo: "owner/repo"
    var githubRepo: String
        get() {
            val saved = prefs.getString("github_repo", null)
            if (saved.isNullOrBlank() || saved.equals("shimulshekh311/job-app-manager", ignoreCase = true)) {
                return DEFAULT_REPO
            }
            return saved
        }
        set(value) {
            val clean = cleanRepoString(value)
            prefs.edit().putString("github_repo", clean).apply()
        }

    fun getCurrentVersionName(): String = BuildConfig.VERSION_NAME
    fun getCurrentVersionCode(): Int = BuildConfig.VERSION_CODE

    /**
     * Checks GitHub API for the latest release in the repository.
     */
    suspend fun checkLatestRelease(): Result<GitHubReleaseInfo> = withContext(Dispatchers.IO) {
        val repo = cleanRepoString(githubRepo)
        if (repo.isBlank() || !repo.contains("/")) {
            return@withContext Result.failure(
                IllegalArgumentException("অনুগ্রহ করে সঠিক GitHub Repository (যেমন: shimulshekh311-cmd/Job-App-Manager) প্রদান করুন।")
            )
        }

        try {
            var releaseJson: JSONObject? = null

            // 1. First try /releases/latest
            val latestUrl = "https://api.github.com/repos/$repo/releases/latest"
            val latestRequest = Request.Builder()
                .url(latestUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "JobAppManager-Android")
                .get()
                .build()

            try {
                client.newCall(latestRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string().orEmpty()
                        if (bodyStr.isNotBlank()) {
                            releaseJson = JSONObject(bodyStr)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore and proceed to fallback
            }

            // 2. If /releases/latest failed or returned null (e.g. pre-releases or automated workflow releases), fetch /releases
            if (releaseJson == null) {
                val listUrl = "https://api.github.com/repos/$repo/releases"
                val listRequest = Request.Builder()
                    .url(listUrl)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "JobAppManager-Android")
                    .get()
                    .build()

                client.newCall(listRequest).execute().use { response ->
                    if (response.code == 404) {
                        return@withContext Result.failure(
                            Exception("GitHub Repository ($repo) খুঁজে পাওয়া যায়নি (404 Not Found)।\n\nঅনুগ্রহ করে নিশ্চিত করুন যে:\n১. ইউজারনেম সঠিক (shimulshekh311-cmd)\n২. রিপোজিটরির নাম সঠিক (Job-App-Manager)\n৩. রিপোজিটরিটি Public রয়েছে।")
                        )
                    }
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("GitHub API ত্রুটি: HTTP ${response.code} ${response.message}")
                        )
                    }

                    val bodyStr = response.body?.string().orEmpty()
                    val array = org.json.JSONArray(bodyStr)
                    if (array.length() == 0) {
                        return@withContext Result.failure(
                            Exception("GitHub Repository ($repo)-এ কোনো Release খুঁজে পাওয়া যায়নি।")
                        )
                    }

                    // Look for the first release that has an APK
                    for (i in 0 until array.length()) {
                        val candidate = array.getJSONObject(i)
                        val assets = candidate.optJSONArray("assets")
                        var hasApk = false
                        if (assets != null) {
                            for (j in 0 until assets.length()) {
                                if (assets.getJSONObject(j).optString("name").endsWith(".apk", ignoreCase = true)) {
                                    hasApk = true
                                    break
                                }
                            }
                        }
                        if (hasApk) {
                            releaseJson = candidate
                            break
                        }
                    }
                    if (releaseJson == null) {
                        releaseJson = array.getJSONObject(0)
                    }
                }
            }

            val json = releaseJson ?: return@withContext Result.failure(
                Exception("GitHub Repository ($repo)-এ কোনো Release বা APK পাওয়া যায়নি।")
            )

            val tagName = json.optString("tag_name", "").trim()
            val title = json.optString("name", tagName)
            val body = json.optString("body", "নতুন আপডেট পাওয়া গেছে।")
            val publishedAt = json.optString("published_at", "")
            val htmlUrl = json.optString("html_url", "https://github.com/$repo")

            var apkDownloadUrl: String? = null
            var apkName: String? = null
            var apkSizeBytes: Long = 0L

            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url")
                        apkName = name
                        apkSizeBytes = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            val isNewer = isRemoteVersionNewer(
                remoteTag = tagName,
                releaseTitle = title,
                currentVersionName = BuildConfig.VERSION_NAME,
                currentVersionCode = BuildConfig.VERSION_CODE
            )

            Result.success(
                GitHubReleaseInfo(
                    tagName = tagName,
                    title = title.ifBlank { tagName },
                    changelog = body,
                    publishedAt = publishedAt,
                    htmlUrl = htmlUrl,
                    apkDownloadUrl = apkDownloadUrl,
                    apkName = apkName,
                    apkSizeBytes = apkSizeBytes,
                    isNewer = isNewer
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads the APK file to cache directory with live progress.
     */
    suspend fun downloadApk(
        apkUrl: String,
        fileName: String,
        onProgress: (UpdateDownloadProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress(UpdateDownloadProgress.Connecting)

            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }
            val targetFile = File(updateDir, fileName.ifBlank { "app-update.apk" })
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val request = Request.Builder()
                .url(apkUrl)
                .header("User-Agent", "JobAppManager-Android")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onProgress(UpdateDownloadProgress.Error("Download failed with HTTP ${response.code}"))
                    return@withContext
                }

                val responseBody = response.body ?: run {
                    onProgress(UpdateDownloadProgress.Error("Empty response body"))
                    return@withContext
                }

                val contentLength = responseBody.contentLength()
                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(targetFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            val percent = if (contentLength > 0) {
                                ((totalBytesRead * 100) / contentLength).toInt().coerceIn(0, 100)
                            } else {
                                -1
                            }
                            onProgress(UpdateDownloadProgress.Downloading(totalBytesRead, contentLength, percent))
                        }
                        output.flush()
                    }
                }

                onProgress(UpdateDownloadProgress.Completed(targetFile))
            }
        } catch (e: Exception) {
            onProgress(UpdateDownloadProgress.Error(e.localizedMessage ?: "Download failed"))
        }
    }

    /**
     * Triggers the Android package installer to install the downloaded APK.
     */
    fun installApk(file: File): Result<Unit> {
        return try {
            if (!file.exists()) {
                return Result.failure(Exception("APK file not found."))
            }

            // On Android Oreo (API 26+) check unknown sources permission if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper to compare version strings or build numbers e.g. "Debug APK Build #9" vs versionCode 1.
     */
    private fun isRemoteVersionNewer(
        remoteTag: String,
        releaseTitle: String,
        currentVersionName: String,
        currentVersionCode: Int
    ): Boolean {
        try {
            // 1. Try to extract build number e.g. "debug-apk-build-9-1", "Debug APK Build #9", "build-9"
            val buildRegex = Regex("""(?:build[-_\s#]*|run[-_\s#]*)(\d+)""", RegexOption.IGNORE_CASE)
            val tagBuildMatch = buildRegex.find(remoteTag) ?: buildRegex.find(releaseTitle)
            if (tagBuildMatch != null) {
                val remoteBuildNum = tagBuildMatch.groupValues[1].toIntOrNull()
                if (remoteBuildNum != null) {
                    return remoteBuildNum > currentVersionCode
                }
            }

            // 2. Try standard semver comparison e.g. "v1.2.0" vs "1.0"
            val semverRegex = Regex("""(\d+(?:\.\d+)+)""")
            val remoteSemver = semverRegex.find(remoteTag)?.groupValues?.get(1)
            val currentSemver = semverRegex.find(currentVersionName)?.groupValues?.get(1)

            if (remoteSemver != null && currentSemver != null) {
                val rParts = remoteSemver.split(".").mapNotNull { it.toIntOrNull() }
                val cParts = currentSemver.split(".").mapNotNull { it.toIntOrNull() }
                val maxLen = maxOf(rParts.size, cParts.size)
                for (i in 0 until maxLen) {
                    val r = rParts.getOrElse(i) { 0 }
                    val c = cParts.getOrElse(i) { 0 }
                    if (r > c) return true
                    if (r < c) return false
                }
            }

            // 3. Fallback: if tag is not blank and does not equal current version name, treat as newer
            val cleanRemote = remoteTag.removePrefix("v").removePrefix("V").trim()
            val cleanCurrent = currentVersionName.removePrefix("v").removePrefix("V").trim()
            return cleanRemote.isNotBlank() && cleanRemote != cleanCurrent
        } catch (_: Exception) {
            return remoteTag.isNotBlank() && remoteTag != currentVersionName
        }
    }
}
