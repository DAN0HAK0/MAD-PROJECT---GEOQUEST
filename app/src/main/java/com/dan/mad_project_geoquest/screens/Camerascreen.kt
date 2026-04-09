package com.dan.mad_project_geoquest.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.viewmodel.CameraUiState
import com.dan.mad_project_geoquest.viewmodel.CameraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    cacheName: String,
    cameraViewModel: CameraViewModel,
    onPhotoConfirmed: (imageUrl: String) -> Unit,
    onBack: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraState by cameraViewModel.state.collectAsStateWithLifecycle()
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(cameraState) {
        if (cameraState is CameraUiState.Success) {
            val url = (cameraState as CameraUiState.Success).imageUrl
            onPhotoConfirmed(url)
            cameraViewModel.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Capture Discovery", fontWeight = FontWeight.Bold)
                        Text(text = cacheName, fontSize = 12.sp, color = Cream.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBrown,
                    titleContentColor = Cream,
                    navigationIconContentColor = Cream
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        val providerFuture = ProcessCameraProvider.getInstance(ctx)
                        providerFuture.addListener({
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            try {
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ctx.mainExecutor)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (cameraState is CameraUiState.Capturing || cameraState is CameraUiState.Uploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Cream)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (cameraState is CameraUiState.Capturing)
                                "Capturing photo…" else "Uploading photo…",
                            color = Cream,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            if (cameraState is CameraUiState.Error) {
                val msg = (cameraState as CameraUiState.Error).message
                Snackbar(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { cameraViewModel.reset() }) { Text("Retry") }
                    }
                ) { Text(msg) }
            }

            val isIdle = cameraState is CameraUiState.Idle || cameraState is CameraUiState.Error
            if (isIdle) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Take a photo to log your find",
                        color = Cream.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledIconButton(
                            onClick = { cameraViewModel.captureAndUpload(imageCapture) },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Filled.Camera,
                                contentDescription = "Capture",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}