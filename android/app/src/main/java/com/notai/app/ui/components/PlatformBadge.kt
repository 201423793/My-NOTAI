package com.notai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notai.app.domain.model.Platform
import com.notai.app.ui.theme.Primary
import com.notai.app.ui.theme.PrimaryLight
import com.notai.app.ui.theme.TextHint

@Composable
fun PlatformBadge(
    platform: Platform,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryLight)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = platform.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Primary
        )
        if (confidence > 0) {
            Text(
                text = " ${(confidence * 100).toInt()}%",
                fontSize = 11.sp,
                color = TextHint,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
