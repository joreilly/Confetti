package dev.johnoreilly.confetti.ai

import android.util.Log
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest


class OnDeviceAI {

    private val generativeModel = Generation.getClient()
    private var modelDownloaded = false

    private val TAG = "genai"


    suspend fun checkFeatureStatus() {
        val status = generativeModel.checkStatus()

        when (status) {
            FeatureStatus.UNAVAILABLE -> {
                // Gemini Nano not supported on this device or device hasn't fetched the latest configuration to support it
            }

            FeatureStatus.DOWNLOADABLE -> {
                // Gemini Nano can be downloaded on this device, but is not currently downloaded
                generativeModel.download().collect { status ->
                    when (status) {
                        is DownloadStatus.DownloadStarted ->
                            Log.d(TAG, "starting download for Gemini Nano")

                        is DownloadStatus.DownloadProgress ->
                            Log.d(TAG, "Nano ${status.totalBytesDownloaded} bytes downloaded")

                        DownloadStatus.DownloadCompleted -> {
                            Log.d(TAG, "Gemini Nano download complete")
                            modelDownloaded = true
                        }

                        is DownloadStatus.DownloadFailed -> {
                            Log.e(TAG, "Nano download failed ${status.e.message}")
                        }
                    }
                }
            }

            FeatureStatus.DOWNLOADING -> {
                // Gemini Nano currently being downloaded
            }

            FeatureStatus.AVAILABLE -> {
                // Gemini Nano currently downloaded and available to use on this device
                generativeModel.warmup()

            }
        }

/*
        Futures.addCallback(
            checkFeatureStatus(),
            object : FutureCallback<Int> {
                override fun onSuccess(featureStatus: Int) {
                    when (featureStatus) {
                        FeatureStatus.AVAILABLE -> {
                            modelDownloaded = true
                            runInference(request)
                        }
                        FeatureStatus.UNAVAILABLE -> {} //displayErrorMessage("Feature is unavailable.")
                        else -> downloadAndRunInference(request)
                    }
                }

                override fun onFailure(t: Throwable) {
//                    Log.e(TAG, "Failed to check status.", t)
//                    displayErrorMessage("Failed to check status", t)
                }
            },
            ContextCompat.getMainExecutor(this),
        )

 */
    }

    suspend fun generateContent(prompt: String): String? {
        val request = generateContentRequest(
            TextPart(prompt)
        ) {
            temperature = 0.2f
            topK = 10
            candidateCount = 1
            maxOutputTokens = 256
        }

        val response = generativeModel.generateContent(request)



//        val request = generativeModel.generateContentRequest { text("Your input text here.") }
//
//        val response = generativeModel.generateContent(prompt)

        val tokenLimit = generativeModel.getTokenLimit()
        println("JFOR, tokenLimit = tokenLimit")

        return response.candidates.map { it.text }.toString()
    }

}