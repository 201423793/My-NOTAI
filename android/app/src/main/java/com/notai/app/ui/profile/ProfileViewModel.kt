package com.notai.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notai.app.data.repository.FavoriteRepository
import com.notai.app.data.repository.HistoryRepository
import com.notai.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val processingCount: Int = 0,
    val freeCredits: Int = 10,
    val favoritesCount: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            userRepository.credits.collect { credits ->
                _state.value = _state.value.copy(freeCredits = credits)
            }
        }
        viewModelScope.launch {
            userRepository.processingCount.collect { count ->
                _state.value = _state.value.copy(processingCount = count)
            }
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(favoritesCount = favoriteRepository.getCount())
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
            loadStats()
        }
    }
}
