package com.notai.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notai.app.data.local.entity.HistoryEntity
import com.notai.app.data.repository.FavoriteRepository
import com.notai.app.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val items: List<HistoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            historyRepository.getAll(0).collect { items ->
                _state.value = HistoryUiState(
                    items = items,
                    isLoading = false,
                    isEmpty = items.isEmpty()
                )
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            historyRepository.delete(id)
            loadHistory()
        }
    }
}
