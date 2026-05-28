package com.notai.app.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun ComparisonSlider(
    originalUri: Uri,
    resultUri: Uri,
    modifier: Modifier = Modifier
) {
    var sliderX by remember { mutableFloatStateOf(0.5f) }
    var containerWidth by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    sliderX = (change.position.x / containerWidth).coerceIn(0f, 1f)
                }
            }
    ) {
        val context = LocalContext.current

        AsyncImage(
            model = ImageRequest.Builder(context).data(resultUri).size(Size.ORIGINAL).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            containerWidth = size.width
            val clipWidth = size.width * sliderX

            clipRect(left = 0f, top = 0f, right = clipWidth, bottom = size.height) {
                drawRect(Color.Transparent)
            }

            drawLine(
                color = Color.White,
                start = Offset(clipWidth, 0f),
                end = Offset(clipWidth, size.height),
                strokeWidth = 4f
            )

            drawCircle(
                color = Color.White,
                radius = 24f,
                center = Offset(clipWidth, size.height / 2f)
            )
        }

        AsyncImage(
            model = ImageRequest.Builder(context).data(originalUri).size(Size.ORIGINAL).build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .let { mod ->
                    mod
                },
            contentScale = ContentScale.Fit
        )
    }
}
