package com.prabh.feeds.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prabh.feeds.PostModel
import com.prabh.feeds.R
import com.prabh.feeds.databinding.LikeCommentSectionBinding
import com.prabh.feeds.databinding.PostImageItemBinding
import com.prabh.feeds.databinding.PostTextItemBinding
import com.prabh.feeds.databinding.PostVideoItemBinding
import com.prabh.feeds.databinding.UserInfoSectionBinding
import java.io.File

@UnstableApi
class PostRecyclerViewAdapter(private val fileDir: File): ListAdapter<PostModel, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<PostModel>() {
    override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem == newItem
    }

}) {

    private var currentPlayer: Player? = null

    private val feedVideoCache: SimpleCache by lazy {
        getCacheInstance()
    }

    private fun getCacheInstance(): SimpleCache {
        val cacheDir = File(fileDir, "video_cache")
        val maxBytes: Long = 10 * 1024 * 1024
        val evictCache = LeastRecentlyUsedCacheEvictor(maxBytes)
        return SimpleCache(cacheDir, evictCache)
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].itemType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view =
                    PostTextItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CaptionViewHolder(view)
            }

            1 -> {
                val view =
                    PostImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ImageViewHolder(view)
            }

            2 -> {
                val view =
                    PostVideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VideoViewHolder(view, videoCache = feedVideoCache) {
                    if (currentPlayer == it) {
                        return@VideoViewHolder
                    }
                    currentPlayer?.stop()
                    currentPlayer = it
                }
            }

            else -> {
                throw IllegalArgumentException("Unknown View Type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = currentList[position]
        when (holder) {
            is CaptionViewHolder -> {
                holder.onBind(currentItem as PostModel.Caption)
            }
            is ImageViewHolder -> {
                holder.onBind(currentItem as PostModel.Image)
            }
            is VideoViewHolder -> {
                holder.onBind(currentItem as PostModel.Video)
            }
        }
    }

}

class CaptionViewHolder(private val binding: PostTextItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun onBind(item: PostModel.Caption) {
        binding.userInfoSection.updateUserInfoSection(item)
        binding.likeCommentSection.updateLikeCommentSection(item)
        binding.postCaption.text = item.data
    }
}

class ImageViewHolder(private val binding: PostImageItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun onBind(item: PostModel.Image) {
        binding.userInfoSection.updateUserInfoSection(item)
        binding.likeCommentSection.updateLikeCommentSection(item)
        binding.postCaption.text = item.caption
        Glide.with(binding.postImage.context)
            .load(item.imageUrl)
            .into(binding.postImage)
    }
}

@OptIn(UnstableApi::class)
class VideoViewHolder(
    val binding: PostVideoItemBinding,
    val videoCache: SimpleCache,
    val playerInitializer: (Player) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var player: ExoPlayer? = null

    fun onBind(item: PostModel.Video) {
        binding.userInfoSection.updateUserInfoSection(item)
        binding.likeCommentSection.updateLikeCommentSection(item)
        binding.postCaption.text = item.caption

        binding.postVideoThumbnail.isVisible = true
        binding.playThumbnail.isVisible = true
        binding.postVideo.isVisible = false

        Glide.with(binding.postVideoThumbnail.context)
            .load(item.thumbnailUrl)
            .into(binding.postVideoThumbnail)

        binding.postVideoThumbnail.setOnClickListener {
            binding.postVideoThumbnail.isVisible = false
            binding.playThumbnail.isVisible = false
            binding.postVideo.isVisible = true

            setupVideoPlayer(binding.postVideo, videoUrl = item.videoUrl)
        }

    }

    private fun setupVideoPlayer(playerView: PlayerView, videoUrl: String) {

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(videoCache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        player = ExoPlayer.Builder(playerView.context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(videoUrl)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()

        player?.apply {
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
            playerView.player = this
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        playerInitializer(this@apply)
                    }
                }
            })
        }

    }

}

fun UserInfoSectionBinding.updateUserInfoSection(item: PostModel) {
    with(this@updateUserInfoSection) {
        userName.text = item.userName
        timeStamp.text = item.timeStamp
        Glide.with(root.context)
            .load(item.userImage)
            .into(profilePic)
    }
}

fun LikeCommentSectionBinding.updateLikeCommentSection(item: PostModel) {
    with(this@updateLikeCommentSection) {
        if (item.isLiked) {
            likeTv.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    this.root.context,
                    R.drawable.thumbs_up
                ), null, null, null
            )
        } else {
            likeTv.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    this.root.context,
                    R.drawable.thumbs_up_empty
                ), null, null, null
            )
        }
    }
}