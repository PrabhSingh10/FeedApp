package com.prabh.feeds.adapter

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
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
import com.prabh.feeds.ui.activity.ImageActivity

@UnstableApi
class PostRecyclerViewAdapter(val isLiked: (Int, Boolean) -> Unit, val addComment: (Int, String) -> Unit) :
    ListAdapter<PostModel, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem == newItem
        }

    }) {

    private var currentPlayer: Player? = null

    override fun getItemViewType(position: Int): Int {
        return currentList[position].itemType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view =
                    PostTextItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CaptionViewHolder(view, isLiked, addComment)
            }

            1 -> {
                val view =
                    PostImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ImageViewHolder(view, isLiked, addComment)
            }

            2 -> {
                val view =
                    PostVideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VideoViewHolder(view, isLiked, addComment) {
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

class CaptionViewHolder(
    private val binding: PostTextItemBinding,
    private val isLiked: (Int, Boolean) -> Unit,
    private val addComment: (Int, String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(item: PostModel.Caption) {
        binding.userInfoSection.updateUserInfoSection(item)
        binding.likeCommentSection.updateLikeCommentSection(item, isLiked, addComment)
        binding.postCaption.text = item.data

        binding.postCaption.setOnLongClickListener {
            binding.postCaption.copyCaption(userName = item.userName, caption = item.data)
            true
        }
    }
}

class ImageViewHolder(
    private val binding: PostImageItemBinding,
    private val isLiked: (Int, Boolean) -> Unit,
    private val addComment: (Int, String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(item: PostModel.Image) {
        binding.userInfoSection.updateUserInfoSection(item)
        binding.likeCommentSection.updateLikeCommentSection(item, isLiked, addComment)
        binding.postCaption.text = item.caption
        Glide.with(binding.postImage.context)
            .load(item.imageUrl)
            .into(binding.postImage)

        binding.postCaption.setOnLongClickListener {
            binding.postCaption.copyCaption(userName = item.userName, caption = item.caption)
            true
        }

        binding.postImage.setOnClickListener {
            val intent = Intent(binding.root.context, ImageActivity::class.java)
            intent.putExtra("imageUrl", item.imageUrl)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                (binding.root.context as Activity),
                binding.postImage,
                "image"
            )
            binding.root.context.startActivity(intent, options.toBundle())
        }
    }
}

@OptIn(UnstableApi::class)
class VideoViewHolder(
    private val binding: PostVideoItemBinding,
    private val isLiked: (Int, Boolean) -> Unit,
    private val addComment: (Int, String) -> Unit,
    val playerInitializer: (Player) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var player: ExoPlayer? = null

    fun onBind(item: PostModel.Video) {
        binding.userInfoSection.updateUserInfoSection(item)
        binding.likeCommentSection.updateLikeCommentSection(item, isLiked, addComment) {
            player?.stop()
            player?.release()
        }
        binding.postCaption.text = item.caption

        binding.postVideoThumbnail.isVisible = true
        binding.playThumbnail.isVisible = true
        binding.postVideo.isVisible = false

        Glide.with(binding.postVideoThumbnail.context)
            .load(item.thumbnailUrl)
            .into(binding.postVideoThumbnail)

        binding.postCaption.setOnLongClickListener {
            binding.postCaption.copyCaption(userName = item.userName, caption = item.caption)
            true
        }

        binding.postVideoThumbnail.setOnClickListener {
            binding.postVideoThumbnail.isVisible = false
            binding.playThumbnail.isVisible = false
            binding.postVideo.isVisible = true

            setupVideoPlayer(binding.postVideo, videoUrl = item.videoUrl)
        }

    }

    private fun setupVideoPlayer(playerView: PlayerView, videoUrl: String) {

        player = ExoPlayer.Builder(playerView.context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultHttpDataSource.Factory()))
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
    userName.text = item.userName
    timeStamp.text = item.timeStamp
    Glide.with(root.context)
        .load(item.userImage)
        .into(profilePic)
}

fun TextView.copyCaption(userName: String, caption: String) {
    val clipBoard = ContextCompat.getSystemService(this.context, ClipboardManager::class.java)
    val clip = ClipData.newPlainText("Feed caption", "[$userName]: $caption")
    clipBoard?.setPrimaryClip(clip)
    Toast.makeText(this.context, "Caption Copied", Toast.LENGTH_SHORT).show()
}

fun LikeCommentSectionBinding.updateLikeCommentSection(
    item: PostModel,
    isLiked: (Int, Boolean) -> Unit,
    addNewComment: (Int, String) -> Unit,
    onClick: () -> Unit = {}
) {

    val drawable = if (item.isLiked) {
        ContextCompat.getDrawable(
            this.root.context,
            R.drawable.thumbs_up
        )
    } else {
        ContextCompat.getDrawable(
            this.root.context,
            R.drawable.thumbs_up_empty
        )
    }
    commentList.isVisible = false

    likeTv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

    likeTv.setOnClickListener {
        onClick()
        isLiked(item.id, item.isLiked.not())
    }

    commentTv.setOnClickListener {
        if (item.commentList.isEmpty()) {
            Toast.makeText(commentTv.context, "No Comments", Toast.LENGTH_SHORT).show()
        } else {
            commentList.isVisible = commentList.isVisible.not()
        }
    }

    addComment.setOnClickListener {
        if (addCommentText.text.isEmpty()) {
            Toast.makeText(addCommentText.context, "Empty Comment", Toast.LENGTH_SHORT).show()
        } else {
            onClick()
            addNewComment(item.id, addCommentText.text.toString())
            Toast.makeText(addCommentText.context, "Comment Added", Toast.LENGTH_SHORT).show()
            addCommentText.setText("")
        }
    }

    commentList.text = item.commentList.joinToString("\n")

}