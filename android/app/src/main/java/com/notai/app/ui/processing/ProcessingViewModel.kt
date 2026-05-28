package com.notai.app.ui.processing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notai.app.domain.model.Platform
import com.notai.app.domain.usecase.RemoveWatermarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProcessingUiState(
    val statusText: String = "正在分析水印...",
    val isProcessing: Boolean = true,
    val error: String? = null,
    val successHistoryId: Long? = null
)

@HiltViewModel
class ProcessingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val removeWatermarkUseCase: RemoveWatermarkUseCase
) : ViewModel() {

    private val fileUri: String = savedStateHandle["fileUri"] ?: ""
    private val platformName: String = savedStateHandle["platform"] ?: ""

    private val _state = MutableStateFlow(ProcessingUiState())
    val state: StateFlow<ProcessingUiState> = _state.asStateFlow()

    init {
        process()
    }

    private fun process() {
        viewModelScope.launch {
            _state.value = ProcessingUiState(statusText = "正在去除水印...")
            try {
                val file = File(fileUri)
                val platform = Platform.fromId(platformName)
                val result = removeWatermarkUseCase(file, platform, platform.defaultRegion)
                result.fold(
                    onSuccess = { processingResult ->
                        _state.value = ProcessingUiState(
                            isProcessing = false,
                            successHistoryId = processingResult.historyId
                        )
                    },
                    onFailure = { e ->
                        _state.value = ProcessingUiState(
                            isProcessing = false,
                            error = e.message ?: "处理失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = ProcessingUiState(isProcessing = false, error = e.message ?: "处理失败")
            }
        }
    }
}
