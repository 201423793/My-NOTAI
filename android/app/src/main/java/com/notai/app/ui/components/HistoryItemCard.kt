package com.notai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.notai.app.data.local.entity.HistoryEntity
import com.notai.app.domain.model.Platform
import com.notai.app.ui.theme.Danger
import com.notai.app.ui.theme.Primary
import com.notai.app.ui.theme.PrimaryLight
import com.notai.app.ui.theme.TextHint
import com.notai.app.util.DateUtils

@Composable
fun HistoryItemCard(
    item: HistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.resultUri,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val platform = Platform.fromId(item.platform)
                Text(
                    text = platform.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (item.status == "completed") "已完成" else "失败",
                    fontSize = 11.sp,
                    color = if (item.status == "completed") Primary else Danger,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.status == "completed") PrimaryLight else Danger.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                text = DateUtils.formatRelative(item.processedAt),
                fontSize = 12.sp,
                color = TextHint
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除", tint = TextHint)
        }
    }
}
