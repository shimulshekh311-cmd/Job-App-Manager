package com.example.data.remote

import android.content.Context
import android.util.Base64
import com.example.data.model.JobApplication
import com.example.data.model.StageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

class GoogleAppsScriptClient(private val context: Context) {

    private val prefs = context.getSharedPreferences("job_app_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    var webAppUrl: String
        get() = prefs.getString("web_app_url", "") ?: ""
        set(value) = prefs.edit().putString("web_app_url", value.trim()).apply()

    fun isConfigured(): Boolean = webAppUrl.isNotBlank() && webAppUrl.startsWith("https://script.google.com")

    suspend fun fetchSheetData(): Result<List<JobApplication>> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(IllegalStateException("Web App URL is not configured. Please set your Google Apps Script URL in Settings."))
        }

        try {
            val url = if (webAppUrl.contains("?")) "$webAppUrl&action=getData" else "$webAppUrl?action=getData"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP Error: ${response.code} ${response.message}"))
                }

                val bodyStr = response.body?.string().orEmpty()
                parseSheetResponse(bodyStr)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseSheetResponse(responseString: String): Result<List<JobApplication>> {
        return try {
            val trimmed = responseString.trim()
            if (trimmed.startsWith("<!DOCTYPE", ignoreCase = true) || trimmed.startsWith("<html", ignoreCase = true)) {
                return Result.failure(
                    Exception("Received an HTML webpage instead of JSON API response. This happens when you paste the script editor URL (/edit) instead of the deployed Web App URL (/exec), or if 'Who has access' was not set to 'Anyone'.")
                )
            }
            val rawArray: JSONArray = if (trimmed.startsWith("{")) {
                val jsonObj = JSONObject(trimmed)
                if (jsonObj.has("data")) {
                    jsonObj.getJSONArray("data")
                } else if (jsonObj.has("values")) {
                    jsonObj.getJSONArray("values")
                } else {
                    return Result.failure(Exception("Unknown JSON structure: $responseString"))
                }
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                return Result.failure(Exception("Invalid response format. Expected JSON array or object."))
            }

            val list = mutableListOf<JobApplication>()
            // Row 0 is header: ["Apply Date", "Day", "Org Name", "Post Name", ...]
            val startIdx = if (rawArray.length() > 0 && isHeaderRow(rawArray.getJSONArray(0))) 1 else 0

            for (i in startIdx until rawArray.length()) {
                val row = rawArray.getJSONArray(i)
                if (row.length() < 4) continue

                val applyDate = row.optString(0, "")
                val day = row.optString(1, "")
                val orgName = row.optString(2, "")
                val postName = row.optString(3, "")
                if (orgName.isBlank() && postName.isBlank()) continue

                val userId = row.optString(4, "")
                val password = row.optString(5, "")
                val mcq = row.optString(6, "")
                val written = row.optString(7, "")
                val typing = row.optString(8, "")
                val viva = row.optString(9, "")
                val statusFlag = row.optString(10, "Saved")
                val timestamp = row.optString(11, "")
                val appPdfUrl = row.optString(12, "")
                val admitPdfUrl = row.optString(13, "")
                val isCanceled = row.optString(14, "No").equals("Yes", ignoreCase = true)

                list.add(
                    JobApplication(
                        applyDate = applyDate,
                        day = day,
                        orgName = orgName,
                        postName = postName,
                        userId = userId,
                        password = password,
                        mcq = StageInfo.fromSheetString(mcq),
                        written = StageInfo.fromSheetString(written),
                        typing = StageInfo.fromSheetString(typing),
                        viva = StageInfo.fromSheetString(viva),
                        appPdfUrl = appPdfUrl,
                        admitPdfUrl = admitPdfUrl,
                        isCanceled = isCanceled,
                        syncStatus = "Synced",
                        lastUpdated = timestamp.ifBlank { statusFlag }
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isHeaderRow(firstRow: JSONArray): Boolean {
        val firstCell = firstRow.optString(0, "").lowercase()
        return firstCell.contains("date") || firstCell.contains("apply")
    }

    suspend fun saveOrUpdateApplication(
        app: JobApplication,
        appPdfBytes: ByteArray? = null,
        appPdfName: String = "",
        admitPdfBytes: ByteArray? = null,
        admitPdfName: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(IllegalStateException("Web App URL is not configured."))
        }

        try {
            val appPdfData = appPdfBytes?.let {
                "data:application/pdf;base64," + Base64.encodeToString(it, Base64.NO_WRAP)
            } ?: ""
            val admitPdfData = admitPdfBytes?.let {
                "data:application/pdf;base64," + Base64.encodeToString(it, Base64.NO_WRAP)
            } ?: ""

            val formObject = JSONObject().apply {
                put("applyDate", app.applyDate)
                put("day", app.day)
                put("orgName", app.orgName)
                put("postName", app.postName)
                put("userId", app.userId)
                put("password", app.password)
                put("isCanceled", if (app.isCanceled) "Yes" else "No")
                put("mcqStatus", if (app.mcq.status.name == "NO") "No" else if (app.mcq.status.name == "PASSED") "Yes" else "Failed")
                put("mcqDate", app.mcq.date)
                put("mcqRating", app.mcq.rating)
                put("writtenStatus", if (app.written.status.name == "NO") "No" else if (app.written.status.name == "PASSED") "Yes" else "Failed")
                put("writtenDate", app.written.date)
                put("writtenRating", app.written.rating)
                put("typingStatus", if (app.typing.status.name == "NO") "No" else if (app.typing.status.name == "PASSED") "Yes" else "Failed")
                put("typingDate", app.typing.date)
                put("typingRating", app.typing.rating)
                put("vivaStatus", if (app.viva.status.name == "NO") "No" else if (app.viva.status.name == "PASSED") "Yes" else "Failed")
                put("vivaDate", app.viva.date)
                put("vivaRating", app.viva.rating)
                put("appPdfData", appPdfData)
                put("appPdfName", appPdfName)
                put("admitPdfData", admitPdfData)
                put("admitPdfName", admitPdfName)
            }

            val payload = JSONObject().apply {
                put("action", "processForm")
                put("formObject", formObject)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(webAppUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                }
                val bodyStr = response.body?.string().orEmpty()
                Result.success(bodyStr.ifBlank { "Success" })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRow(org: String, post: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext Result.failure(IllegalStateException("Web App URL is not configured."))

        try {
            val payload = JSONObject().apply {
                put("action", "deleteRow")
                put("org", org)
                put("post", post)
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)
            val request = Request.Builder().url(webAppUrl).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                Result.success("Deleted")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDriveFiles(org: String, post: String, appUrl: String, admitUrl: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext Result.failure(IllegalStateException("Web App URL is not configured."))

        try {
            val payload = JSONObject().apply {
                put("action", "deleteDriveFiles")
                put("org", org)
                put("post", post)
                put("appUrl", appUrl)
                put("admitUrl", admitUrl)
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)
            val request = Request.Builder().url(webAppUrl).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                Result.success("Success")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
