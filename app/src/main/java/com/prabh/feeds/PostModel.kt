package com.prabh.feeds

import kotlin.random.Random

sealed class PostModel(
    val itemType: Int,
    val id: Int = Random.nextInt(),
    open val userName: String = "",
    open val timeStamp: String = "",
    open val userImage: String = "",
    open val isLiked: Boolean = false,
    open val commentList: List<String> = emptyList()
) {

    data class Caption(
        override val userName: String,
        override val timeStamp: String,
        override val userImage: String,
        override val isLiked: Boolean,
        override val commentList: List<String> = emptyList(),
        val data: String
    ): PostModel(0)

    data class Image(
        override val userName: String,
        override val timeStamp: String,
        override val userImage: String,
        override val isLiked: Boolean,
        override val commentList: List<String> = emptyList(),
        val caption: String,
        val imageUrl: String
    ): PostModel(1)

    data class Video(
        override val userName: String,
        override val timeStamp: String,
        override val userImage: String,
        override val isLiked: Boolean,
        override val commentList: List<String> = emptyList(),
        val caption: String,
        val thumbnailUrl: String,
        val videoUrl: String
    ): PostModel(2)

}