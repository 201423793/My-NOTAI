package com.notai.app.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.notai.app.domain.model.Platform
import com.notai.app.ui.components.ImageUploader
import com.notai.app.ui.components.PlatformBadge
import com.notai.app.ui.theme.Background
import com.notai.app.ui.theme.Primary
import com.notai.app.ui.theme.TextHint
import com.notai.app.ui.theme.TextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val tempPhotoUri = remember {
        val photoFile = File(context.cacheDir, "camera").apply { mkdirs() }
        val f = File(photoFile, "photo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = File(context.cacheDir, "compressed").apply { mkdirs() }
                .let { dir -> File(dir, "img_${System.currentTimeMillis()}.jpg") }
            context.contentResolver.openInputStream(it)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            viewModel.onImageSelected(it, file)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = File(context.cacheDir, "compressed").apply { mkdirs() }
                .let { dir -> File(dir, "img_${System.currentTimeMillis()}.jpg") }
            context.contentResolver.openInputStream(tempPhotoUri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            viewModel.onImageSelected(tempPhotoUri, file)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(tempPhotoUri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("AI 去水印", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("上传AI生成的图片，一键去除水印", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        ImageUploader(
            imageUri = state.imageUri,
            isUploading = state.isUploading,
            onChoose = { galleryLauncher.launch("image/*") }
        )

        if (state.imageUri != null && !state.isUploading) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(androidx.compose.ui.graphics.Color.White).padding(16.dp)
            ) {
                Text("水印检测", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                if (state.isDetecting) {
                    Text("正在分析图片...", fontSize = 13.sp, color = TextHint, modifier = Modifier.padding(top = 12.dp))
                } else if (state.detectedPlatform != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PlatformBadge(platform = state.detectedPlatform!!, confidence = state.platformConfidence)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("平台不正确？手动选择：", fontSize = 12.sp, color = TextHint)

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = state.selectedPlatform?.displayName ?: "选择平台",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            Platform.entries.filter { it != Platform.UNKNOWN }.forEach { platform ->
                                DropdownMenuItem(
                                    text = { Text(platform.displayName) },
                                    onClick = { viewModel.onPlatformSelected(platform); expanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.getProcessRoute()?.let { onNavigate(it) } },
                enabled = state.canProcess && state.selectedPlatform != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("去除水印", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Text(
                "剩余免费次数：${state.freeCredits} 次",
                fontSize = 12.sp, color = TextHint,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        if (state.imageUri == null) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(androidx.compose.ui.graphics.Color.White).padding(16.dp)
            ) {
                Text("支持的平台", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Platform.entries.filter { it != Platform.UNKNOWN }.forEach { platform ->
                        Text(
                            platform.displayName,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Background)
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
