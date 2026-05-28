package com.notai.app.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.notai.app.ui.theme.Border
import com.notai.app.ui.theme.Primary
import com.notai.app.ui.theme.TextHint
import com.notai.app.ui.theme.TextSecondary

@Composable
fun ImageUploader(
    imageUri: Uri?,
    isUploading: Boolean,
    onChoose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color(0xFFFAFAFA))
            .border(2.dp, Border, RoundedCornerShape(16.dp))
            .clickable(enabled = !isUploading) { onChoose() },
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUri != null -> {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            isUploading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                    Text("正在处理...", fontSize = 14.sp, color = TextHint)
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("+", fontSize = 48.sp, color = TextHint)
                    Text("点击上传图片", fontSize = 16.sp, color = TextSecondary)
                    Text("支持 JPG、PNG 格式", fontSize = 13.sp, color = TextHint)
                }
            }
        }
    }
}
