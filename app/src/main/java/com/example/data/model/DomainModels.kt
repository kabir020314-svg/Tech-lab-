package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppCategory(val key: String, val bnLabel: String, val enLabel: String) {
    ALL("all", "সব", "All"),
    TECH("Tech Tutorials", "টেক টিউটোরিয়াল", "Tech Tutorials"),
    STORIES("Stories", "বাংলা গল্প", "Stories"),
    TIPS("Tips", "টেক টিপস", "Tips"),
    SHORTS("Shorts", "শর্টস", "Shorts");

    companion object {
        fun fromKey(key: String): AppCategory =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: TECH
    }
}

enum class VideoSortOption(val bnLabel: String, val enLabel: String) {
    NEWEST("নতুন আগে", "Newest"),
    MOST_VIEWED("সর্বাধিক দেখা", "Most Viewed")
}

enum class VideoDurationFilter(val bnLabel: String, val enLabel: String) {
    ALL("সব সময়", "Any Length"),
    SHORT("ছোট (< ৪ মি.)", "Short (< 4m)"),
    MEDIUM("মাঝারি (৪-২০ মি.)", "Medium (4-20m)"),
    LONG("দীর্ঘ (> ২০ মি.)", "Long (> 20m)")
}

enum class ResourceType(val label: String) {
    PDF("PDF"),
    CODE("CODE"),
    TOOL("TOOL"),
    LINK("LINK");

    companion object {
        fun fromRaw(raw: String): ResourceType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: LINK
    }
}

data class VideoItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val category: String,
    val playlistId: String? = null,
    val partNumber: Int? = null,
    val thumbnailUrl: String,
    val publishedAtLabel: String,
    val publishedEpochMillis: Long,
    val viewCount: Long,
    val likeCount: Long,
    val durationSeconds: Int,
    val durationLabel: String,
    val trending: Boolean = false
)

data class PlaylistItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val category: String,
    val seriesType: String, // "playlist" or "story_series"
    val thumbnailUrl: String,
    val totalVideos: Int,
    val videoIds: List<String>
)

data class VideoResourceItem(
    val id: String,
    val videoId: String,
    val titleBn: String,
    val titleEn: String,
    val resourceType: ResourceType,
    val url: String,
    val snippetOrNote: String = ""
)

data class ToolDirectoryItem(
    val id: String,
    val name: String,
    val categoryBn: String,
    val categoryEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val url: String,
    val tag: String
)

data class TechTipItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val bodyBn: String,
    val bodyEn: String,
    val tag: String
)

data class QuizQuestionItem(
    val id: String,
    val questionBn: String,
    val questionEn: String,
    val optionsBn: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val explanationBn: String,
    val explanationEn: String,
    val xpReward: Int = 25
)

data class NotificationItem(
    val id: String,
    val type: String, // "upload", "tip", "admin"
    val titleBn: String,
    val titleEn: String,
    val bodyBn: String,
    val bodyEn: String,
    val timeLabel: String,
    val targetVideoId: String? = null
)

data class BadgeItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val requiredXp: Int
)

// --- Firestore Models (Strictly typed with @Keep, @IgnoreExtraProperties, @DocumentId, no-arg defaults) ---

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val preferredLanguage: String = "bn",
    val favoriteCategories: List<String> = emptyList(),
    val xpPoints: Long = 0L,
    val streakDays: Long = 1L,
    val watchedCount: Long = 0L,
    val watchMinutes: Long = 0L,
    val badges: List<String> = emptyList(),
    val premiumMember: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SavedVideoDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val videoId: String = "",
    val title: String = "",
    val category: String = "",
    val thumbnailUrl: String = "",
    val durationLabel: String = "",
    @ServerTimestamp val savedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VideoNoteDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val videoId: String = "",
    val videoTitle: String = "",
    val content: String = "",
    val timestampLabel: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WatchHistoryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val videoId: String = "",
    val title: String = "",
    val playlistId: String? = null,
    val progressPercent: Long = 0L,
    @ServerTimestamp val lastWatchedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CommunityPostDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "",
    val visibility: String = "public",
    val likesCount: Long = 0L,
    val repliesCount: Long = 0L,
    val reported: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CommunityReplyDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    val authorName: String = "",
    val text: String = "",
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PollDoc(
    @DocumentId val id: String = "",
    val questionBn: String = "",
    val questionEn: String = "",
    val options: List<String> = emptyList(),
    val voteCounts: List<Long> = emptyList(),
    val visibility: String = "public",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AnnouncementDoc(
    @DocumentId val id: String = "",
    val titleBn: String = "",
    val titleEn: String = "",
    val bodyBn: String = "",
    val bodyEn: String = "",
    val active: Boolean = true,
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LeaderboardEntryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val xpPoints: Long = 0L,
    val streakDays: Long = 0L,
    val badgesCount: Long = 0L,
    val visibility: String = "public",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VideoRequestDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val requesterName: String = "",
    val requestType: String = "video_request",
    val topic: String = "",
    val details: String = "",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class QuizQuestionDoc(
    @DocumentId val id: String = "",
    val questionBn: String = "",
    val questionEn: String = "",
    val optionsBn: List<String> = emptyList(),
    val optionsEn: List<String> = emptyList(),
    val correctIndex: Long = 0L,
    val explanationBn: String = "",
    val explanationEn: String = "",
    val xpReward: Long = 25L,
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VideoResourceDoc(
    @DocumentId val id: String = "",
    val videoId: String = "",
    val titleBn: String = "",
    val titleEn: String = "",
    val resourceType: String = "LINK",
    val url: String = "",
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null
)
