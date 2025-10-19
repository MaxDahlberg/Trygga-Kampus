package com.example.tryggakampus.domain.repository


interface StoryCommentRepository {
    suspend fun getCommentsForStory()
    suspend fun postComment()
    suspend fun fetchComments()
    suspend fun deleteComment()
}
