package com.prabh.feeds

sealed class PostModel(
    val itemType: Int,
    open val userName: String = "",
    open val timeStamp: String = "",
    open val userImage: String = "",
    open val isLiked: Boolean = false
) {

    data class Caption(
        override val userName: String,
        override val timeStamp: String,
        override val userImage: String,
        override val isLiked: Boolean,
        val data: String
    ): PostModel(0)

    data class Image(
        override val userName: String,
        override val timeStamp: String,
        override val userImage: String,
        override val isLiked: Boolean,
        val caption: String,
        val imageUrl: String
    ): PostModel(1)

    data class Video(
        override val userName: String,
        override val timeStamp: String,
        override val userImage: String,
        override val isLiked: Boolean,
        val caption: String,
        val thumbnailUrl: String,
        val videoUrl: String
    ): PostModel(2)

}