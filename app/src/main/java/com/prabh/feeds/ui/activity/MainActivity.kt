package com.prabh.feeds.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.prabh.feeds.PostModel
import com.prabh.feeds.adapter.PostRecyclerViewAdapter
import com.prabh.feeds.databinding.MainActivityLayoutBinding
import com.prabh.feeds.ui.viewModel.FeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class MainActivity : ComponentActivity() {

    private lateinit var binding: MainActivityLayoutBinding

    private val vm by viewModels<FeedViewModel>()

    private val adapter by lazy {
        PostRecyclerViewAdapter(this.cacheDir)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            vm.uiState.collectLatest {
                when (it) {
                    is UiState.Loading -> {
                        binding.dataLoader.isVisible = true
                        binding.postRv.isVisible = false
                    }
                    is UiState.Success -> {
                        binding.dataLoader.isVisible = false
                        binding.postRv.isVisible = true
                        adapter.submitList(it.data)
                    }
                }
            }
        }
        binding.postRv.adapter = adapter
        binding.postRv.layoutManager = LinearLayoutManager(this)
    }

}