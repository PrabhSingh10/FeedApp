package com.prabh.feeds.ui.activity

import com.prabh.feeds.PostModel

sealed class UiState {
    data object Loading: UiState()
    data class Success(val data: List<PostModel>): UiState()
}