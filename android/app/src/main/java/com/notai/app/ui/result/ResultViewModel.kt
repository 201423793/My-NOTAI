package com.notai.app.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notai.app.data.local.entity.HistoryEntity
import com.notai.app.data.repository.FavoriteRepository
import com.notai.app.data.repository.HistoryRepository
import com.notai.app.domain.usecase.SaveToGalleryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ResultUiState(
    val history: HistoryEntity? = null,
    val isFavorited: Boolean = false,
    val isLoading: Boolean = true,
    val savedMessage: String? = null
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val saveToGalleryUseCase: SaveToGalleryUseCase
) : ViewModel() {

    private val historyId: Long = savedStateHandle["historyId"] ?: 0L

    private val _state = MutableStateFlow(ResultUiState())
    val state: StateFlow<ResultUiState> = _state.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            val history = historyRepository.getById(historyId)
            _state.value = ResultUiState(
                history = history,
                isFavorited = history?.isFavorited ?: false,
                isLoading = false
            )
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val newState = favoriteRepository.toggle(historyId)
            _state.value = _state.value.copy(isFavorited = newState)
        }
    }

    fun saveToGallery() {
        viewModelScope.launch {
            try {
                val history = _state.value.history ?: return@launch
                val file = File(history.resultUri)
                saveToGalleryUseCase(file)
                _state.value = _state.value.copy(savedMessage = "已保存到相册")
            } catch (e: Exception) {
                _state.value = _state.value.copy(savedMessage = "保存失败: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(savedMessage = null)
    }
}
