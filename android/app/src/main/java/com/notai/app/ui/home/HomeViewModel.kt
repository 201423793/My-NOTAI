package com.notai.app.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notai.app.data.repository.UserRepository
import com.notai.app.domain.model.DetectionResult
import com.notai.app.domain.model.Platform
import com.notai.app.domain.usecase.DetectWatermarkUseCase
import com.notai.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class HomeUiState(
    val imageUri: Uri? = null,
    val imageFile: File? = null,
    val isUploading: Boolean = false,
    val isDetecting: Boolean = false,
    val detectedPlatform: Platform? = null,
    val platformConfidence: Float = 0f,
    val selectedPlatform: Platform? = null,
    val freeCredits: Int = 0,
    val canProcess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val detectWatermarkUseCase: DetectWatermarkUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.initUser()
            userRepository.credits.collect { credits ->
                _state.update { it.copy(freeCredits = credits) }
            }
        }
    }

    fun onImageSelected(uri: Uri, file: File) {
        _state.update { it.copy(imageUri = uri, imageFile = file, isUploading = true, error = null) }
        viewModelScope.launch {
            _state.update { it.copy(isDetecting = true) }
            try {
                val result = detectWatermarkUseCase(uri)
                _state.update {
                    it.copy(
                        detectedPlatform = result.platform,
                        platformConfidence = result.confidence,
                        selectedPlatform = result.platform,
                        isDetecting = false,
                        isUploading = false,
                        canProcess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isDetecting = false,
                        isUploading = false,
                        detectedPlatform = Platform.UNKNOWN,
                        selectedPlatform = Platform.UNKNOWN,
                        canProcess = true,
                        error = "检测失败，请手动选择平台"
                    )
                }
            }
        }
    }

    fun onPlatformSelected(platform: Platform) {
        _state.update { it.copy(selectedPlatform = platform) }
    }

    fun getProcessRoute(): String? {
        val s = _state.value
        val file = s.imageFile ?: return null
        val platform = s.selectedPlatform ?: return null
        return Screen.Processing.createRoute(file.absolutePath, platform.name)
    }

    fun clearImage() {
        _state.value = HomeUiState(freeCredits = _state.value.freeCredits)
    }
}
