package com.prabh.feeds.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prabh.feeds.data.FeedRepository
import com.prabh.feeds.ui.activity.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel: ViewModel() {

    private val repo: FeedRepository = FeedRepository()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    init {
        getData()
    }

    private fun getData() {
        viewModelScope.launch {
            val data = repo.fetchFeedData()
            _uiState.update {
                UiState.Success(data)
            }
        }
    }

}