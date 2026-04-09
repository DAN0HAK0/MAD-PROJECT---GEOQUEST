package com.dan.mad_project_geoquest.components.map

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MapCompassOverlay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }

    // ── Sensor setup ──────────────────────────────────────────────
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity    = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val orientation    = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER ->
                        System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                    Sensor.TYPE_MAGNETIC_FIELD ->
                        System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                }
                val success = SensorManager.getRotationMatrix(
                    rotationMatrix, null, gravity, geomagnetic
                )
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (degrees + 360f) % 360f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magnetometer,  SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // ── Smooth rotation animation ─────────────────────────────────
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 200),
        label = "compass_rotation"
    )

    val cardinalLabel = when {
        azimuth < 22.5f  || azimuth >= 337.5f -> "N"
        azimuth < 67.5f  -> "NE"
        azimuth < 112.5f -> "E"
        azimuth < 157.5f -> "SE"
        azimuth < 202.5f -> "S"
        azimuth < 247.5f -> "SW"
        azimuth < 292.5f -> "W"
        else             -> "NW"
    }

    // ── UI ────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        // Compass needle drawn on canvas
        Canvas(
            modifier = Modifier
                .size(48.dp)
                .rotate(-animatedAzimuth)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val needleLength = size.minDimension / 2f * 0.75f
            val needleWidth  = size.minDimension * 0.12f

            // North needle (red)
            val northPath = Path().apply {
                moveTo(cx, cy - needleLength)
                lineTo(cx - needleWidth / 2, cy)
                lineTo(cx + needleWidth / 2, cy)
                close()
            }
            drawPath(northPath, color = Color(0xFFE53935))

            // South needle (white/light)
            val southPath = Path().apply {
                moveTo(cx, cy + needleLength)
                lineTo(cx - needleWidth / 2, cy)
                lineTo(cx + needleWidth / 2, cy)
                close()
            }
            drawPath(southPath, color = Color(0xFFBDBDBD))

            // Centre dot
            drawCircle(color = Color.DarkGray, radius = needleWidth / 2f, center = Offset(cx, cy))
        }

        // Cardinal label below needle
        Text(
            text = cardinalLabel,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}