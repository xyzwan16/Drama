package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserBookmarkDto(
    @DocumentId val id: String = "",
    val dramaId: String = "",
    val title: String = "",
    @ServerTimestamp val addedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserSubscriptionDto(
    @DocumentId val id: String = "",
    val dramaId: String = "",
    val title: String = "",
    val releaseSchedule: String = "",
    @ServerTimestamp val subscribedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserWatchProgressDto(
    @DocumentId val id: String = "",
    val dramaId: String = "",
    val lastEpisode: Int = 1,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommentDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val dramaId: String = "",
    val episodeNumber: Int = 1,
    val text: String = "",
    val likesCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)
