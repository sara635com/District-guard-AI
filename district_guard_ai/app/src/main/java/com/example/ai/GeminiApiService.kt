package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GeminiAnalysisResult(
    val riskLevel: String,
    val riskScore: Int,
    val confidence: Int,
    val reason: String,
    val evidence: List<String>,
    val recommendedAction: String,
    val dataLimitations: List<String>,
    val isAiGenerated: Boolean = true
)

object GeminiApiService {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeOutbreakData(
        aggregatedDataJson: String
    ): GeminiAnalysisResult? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val prompt = """
            You are an epidemiological early-warning assistant for district public-health surveillance.
            
            Analyze the following aggregated, anonymized public-health surveillance data.
            Your task is NOT to diagnose individual patients.
            
            Identify whether the data contains an unusual temporal, geographic, or symptom-based cluster that may warrant public-health investigation.
            
            Consider:
            1. Number of reports
            2. Rate of increase
            3. Geographic concentration
            4. Symptom similarity
            5. Severity
            6. Historical baseline
            7. Data quality
            
            Aggregated Surveillance Data:
            $aggregatedDataJson
            
            Return JSON only strictly following this schema:
            {
              "risk_level": "LOW|MODERATE|HIGH|CRITICAL",
              "risk_score": 85,
              "confidence": 90,
              "reason": "Detailed natural language explanation referencing actual evidence from the dataset...",
              "evidence": ["Evidence point 1", "Evidence point 2"],
              "recommended_action": "Recommended public health response...",
              "data_limitations": ["Data limitation 1"]
            }
        """.trimIndent()

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val responseBodyString = response.body?.string() ?: return@withContext null
            val responseJson = JSONObject(responseBodyString)

            val candidates = responseJson.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")

            val parsedJson = JSONObject(text)
            val riskLevel = parsedJson.optString("risk_level", "MODERATE")
            val riskScore = parsedJson.optInt("risk_score", 50)
            val confidence = parsedJson.optInt("confidence", 80)
            val reason = parsedJson.optString("reason", "Outbreak cluster detected based on symptom frequency.")
            
            val evidenceArray = parsedJson.optJSONArray("evidence")
            val evidenceList = mutableListOf<String>()
            if (evidenceArray != null) {
                for (i in 0 until evidenceArray.length()) {
                    evidenceList.add(evidenceArray.getString(i))
                }
            }

            val recommendedAction = parsedJson.optString("recommended_action", "Increase local surveillance.")

            val limitationsArray = parsedJson.optJSONArray("data_limitations")
            val limitationsList = mutableListOf<String>()
            if (limitationsArray != null) {
                for (i in 0 until limitationsArray.length()) {
                    limitationsList.add(limitationsArray.getString(i))
                }
            }

            GeminiAnalysisResult(
                riskLevel = riskLevel,
                riskScore = riskScore,
                confidence = confidence,
                reason = reason,
                evidence = evidenceList,
                recommendedAction = recommendedAction,
                dataLimitations = limitationsList,
                isAiGenerated = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
