package com.dan.mad_project_geoquest.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

private const val IMGBB_API_KEY = "bdf9842fb1aba8ba5c2685be62668992"

class CameraViewModel : ViewModel() {

    private val _state = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val state: StateFlow<CameraUiState> = _state.asStateFlow()

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val httpClient = OkHttpClient()

    fun captureAndUpload(imageCapture: ImageCapture) {
        _state.value = CameraUiState.Capturing

        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    viewModelScope.launch {
                        try {
                            val base64 = imageProxyToBase64(image)
                            image.close()
                            _state.value = CameraUiState.Uploading
                            val url = uploadToImgBB(base64)
                            _state.value = CameraUiState.Success(url)
                        } catch (e: Exception) {
                            _state.value = CameraUiState.Error(
                                e.localizedMessage ?: "Upload failed"
                            )
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _state.value = CameraUiState.Error(
                        exception.localizedMessage ?: "Capture failed"
                    )
                }
            }
        )
    }

    fun reset() {
        _state.value = CameraUiState.Idle
    }

    private suspend fun imageProxyToBase64(image: ImageProxy): String =
        withContext(Dispatchers.IO) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val scaled = scaleBitmap(original, maxDimension = 1024)
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        }

    private fun scaleBitmap(source: Bitmap, maxDimension: Int): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= maxDimension && h <= maxDimension) return source
        val scale = maxDimension.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(source, (w * scale).toInt(), (h * scale).toInt(), true)
    }

    private suspend fun uploadToImgBB(base64: String): String =
        withContext(Dispatchers.IO) {
            val body = FormBody.Builder()
                .add("key", IMGBB_API_KEY)
                .add("image", base64)
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response from ImgBB")

            if (!response.isSuccessful) {
                throw Exception("ImgBB error ${response.code}: $responseBody")
            }

            val json = JSONObject(responseBody)
            json.getJSONObject("data").getString("url")
        }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}