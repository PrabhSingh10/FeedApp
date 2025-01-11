package com.prabh.feeds.data

import com.prabh.feeds.PostModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FeedRepository {

    private var dataList = listOf(
        PostModel.Caption(
            data = "This is the caption 1",
            userName = "User 1",
            userImage = "https://media.gettyimages.com/id/1483869287/photo/happy-businesswoman-working-at-the-office-and-holding-a-tablet.jpg?s=612x612&w=gi&k=20&c=j0vMVsvSVIaMG01AJOLb3pI7QyX3R_0KNuvHt-bPIDw=",
            timeStamp = "3 hours ago",
            isLiked = false
        ),
        PostModel.Image(
            caption = "This is the caption 2",
            imageUrl = "https://fastly.picsum.photos/id/299/536/354.jpg?hmac=2FJNPad_HUlUNruo-u6zDMYHmIsC1hQfMl5SQJkJrt0",
            userName = "User 2",
            userImage = "https://media.gettyimages.com/id/1483869287/photo/happy-businesswoman-working-at-the-office-and-holding-a-tablet.jpg?s=612x612&w=gi&k=20&c=j0vMVsvSVIaMG01AJOLb3pI7QyX3R_0KNuvHt-bPIDw=",
            timeStamp = "3 hours ago",
            isLiked = false
        ),
        PostModel.Video(
            caption = "This is the caption 3",
            videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://images.pexels.com/photos/4343780/pexels-photo-4343780.jpeg?auto=compress&cs=tinysrgb&w=800",
            userName = "User 1",
            userImage = "https://media.gettyimages.com/id/1483869287/photo/happy-businesswoman-working-at-the-office-and-holding-a-tablet.jpg?s=612x612&w=gi&k=20&c=j0vMVsvSVIaMG01AJOLb3pI7QyX3R_0KNuvHt-bPIDw=",
            timeStamp = "3 hours ago",
            isLiked = true
        ),
        PostModel.Video(
            caption = "This is the caption 4",
            videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://images.pexels.com/photos/4343780/pexels-photo-4343780.jpeg?auto=compress&cs=tinysrgb&w=800",
            userName = "User 2",
            userImage = "https://media.gettyimages.com/id/1483869287/photo/happy-businesswoman-working-at-the-office-and-holding-a-tablet.jpg?s=612x612&w=gi&k=20&c=j0vMVsvSVIaMG01AJOLb3pI7QyX3R_0KNuvHt-bPIDw=",
            timeStamp = "3 hours ago",
            isLiked = false
        ),
        PostModel.Video(
            caption = "This is the caption 5",
            videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.pexels.com/photos/4343780/pexels-photo-4343780.jpeg?auto=compress&cs=tinysrgb&w=800",
            userName = "User 3",
            userImage = "https://media.gettyimages.com/id/1483869287/photo/happy-businesswoman-working-at-the-office-and-holding-a-tablet.jpg?s=612x612&w=gi&k=20&c=j0vMVsvSVIaMG01AJOLb3pI7QyX3R_0KNuvHt-bPIDw=",
            timeStamp = "3 hours ago",
            isLiked = true
        )
    )

    suspend fun getFeedData(): List<PostModel> {
        delay(1000)
        return dataList
    }

    suspend fun updateLike(id: Int, isLiked: Boolean): List<PostModel> {
        return withContext(Dispatchers.Default) {
            dataList = dataList.map {
                if (id == it.id) {
                    when (it) {
                        is PostModel.Caption -> it.copy(isLiked = isLiked)
                        is PostModel.Image -> it.copy(isLiked = isLiked)
                        is PostModel.Video -> it.copy(isLiked = isLiked)
                    }
                } else {
                    it
                }
            }
            dataList
        }
    }

    suspend fun updateComments(id: Int, comment: String): List<PostModel> {
        return withContext(Dispatchers.Default) {
            dataList = dataList.map {
                if (id == it.id) {
                    when (it) {
                        is PostModel.Caption -> {
                            val list = it.commentList.toMutableList()
                            list.add(comment)
                            it.copy(commentList = list)
                        }
                        is PostModel.Image -> {
                            val list = it.commentList.toMutableList()
                            list.add(comment)
                            it.copy(commentList = list)
                        }
                        is PostModel.Video -> {
                            val list = it.commentList.toMutableList()
                            list.add(comment)
                            it.copy(commentList = list)
                        }
                    }
                } else {
                    it
                }
            }
            dataList
        }
    }

}