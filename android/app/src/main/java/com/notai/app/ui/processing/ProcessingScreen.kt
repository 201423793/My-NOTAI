package com.notai.app.ui.processing

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notai.app.ui.theme.Danger
import com.notai.app.ui.theme.Primary
import com.notai.app.ui.theme.TextSecondary

@Composable
fun ProcessingScreen(
    onSuccess: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: ProcessingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.successHistoryId) {
        state.successHistoryId?.let { onSuccess(it) }
    }

    LaunchedEffect(state.error) {
        state.error?.let { onCancel() }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            val transition = rememberInfiniteTransition(label = "pulse")
            val scale by transition.animateFloat(
                initialValue = 0.9f, targetValue = 1.1f,
                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                label = "scale"
            )

            Box(
                modifier = Modifier.size(100.dp).scale(scale).clip(CircleShape).background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(state.statusText, fontSize = 18.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isProcessing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxSize().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("AI正在智能去除水印，请耐心等待...", fontSize = 14.sp, color = TextSecondary)
            Text("通常需要 5-15 秒", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(onClick = onCancel, colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)) {
                Text("取消处理")
            }
        }
    }
}
