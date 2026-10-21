package com.example.model

data class SubtitleItem(
    val startSecond: Int,
    val endSecond: Int,
    val speaker: String,
    val textId: String, // Subtitle Bahasa Indonesia
    val textEn: String  // Subtitle English
)

data class DramaSceneVisual(
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val sceneDescription: String,
    val moodIcon: String
)

data class Episode(
    val id: String,
    val episodeNumber: Int,
    val title: String,
    val durationSeconds: Int,
    val isFree: Boolean,
    val isNew: Boolean,
    val previewSnippet: String,
    val subtitles: List<SubtitleItem>,
    val sceneVisual: DramaSceneVisual
)

data class Drama(
    val id: String,
    val title: String,
    val titleAlt: String,
    val synopsis: String,
    val posterRes: Int,
    val genre: String,
    val tags: List<String>,
    val isSeries: Boolean, // true = series pendek, false = film pendek / mini movie
    val totalEpisodes: Int,
    val releasedEpisodes: Int,
    val rating: Double,
    val viewsCount: String,
    val hasNewEpisode: Boolean,
    val releaseSchedule: String,
    val cast: List<String>,
    val episodes: List<Episode>,
    val country: String = "Indonesia",
    val countryFlag: String = "🇮🇩"
)

data class DramaNotification(
    val id: String,
    val dramaId: String,
    val dramaTitle: String,
    val episodeNumber: Int?,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean,
    val posterRes: Int,
    val type: NotificationType
)

enum class NotificationType {
    NEW_EPISODE,
    NEW_SERIES,
    SYSTEM_RECOMMENDATION
}

data class DramaComment(
    val id: String,
    val dramaId: String,
    val episodeNumber: Int,
    val userName: String,
    val avatarInitials: String,
    val text: String,
    val timeAgo: String,
    val likesCount: Int,
    val isLiked: Boolean = false
)

data class SubtitleConfig(
    val language: SubtitleLanguage = SubtitleLanguage.INDONESIAN,
    val fontSizeSp: Int = 16,
    val backgroundOpacity: Float = 0.75f
)

enum class SubtitleLanguage(val displayName: String, val code: String) {
    INDONESIAN("Indonesia (Sub Indo)", "ID"),
    ENGLISH("English (Sub Eng)", "EN"),
    OFF("Nonaktifkan Subtitle", "OFF")
}
