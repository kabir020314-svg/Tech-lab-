package com.example.data.remote

import android.util.Log
import com.example.config.AppConfig
import com.example.data.model.PlaylistItem
import com.example.data.model.VideoItem
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    val nextPageToken: String? = null,
    val items: List<YouTubeSearchItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItem(
    val id: YouTubeSearchId? = null,
    val snippet: YouTubeSnippet? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchId(
    val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideosResponse(
    val items: List<YouTubeVideoDetail> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoDetail(
    val id: String = "",
    val snippet: YouTubeSnippet? = null,
    val contentDetails: YouTubeContentDetails? = null,
    val statistics: YouTubeStatistics? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippet(
    val title: String = "",
    val description: String = "",
    val publishedAt: String = "",
    val thumbnails: YouTubeThumbnails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnails(
    val high: YouTubeThumbnailInfo? = null,
    val medium: YouTubeThumbnailInfo? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailInfo(
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class YouTubeContentDetails(
    val duration: String = "PT5M0S",
    val itemCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeStatistics(
    val viewCount: String? = null,
    val likeCount: String? = null,
    val subscriberCount: String? = null,
    val videoCount: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistsResponse(
    val items: List<YouTubePlaylistItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItem(
    val id: String = "",
    val snippet: YouTubeSnippet? = null,
    val contentDetails: YouTubeContentDetails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelResponse(
    val items: List<YouTubeChannelDetail> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelDetail(
    val id: String = "",
    val statistics: YouTubeStatistics? = null
)

interface YouTubeApiService {
    @GET("youtube/v3/channels")
    suspend fun getChannelStats(
        @Query("part") part: String = "statistics",
        @Query("id") channelId: String,
        @Query("key") apiKey: String
    ): YouTubeChannelResponse

    @GET("youtube/v3/search")
    suspend fun searchChannelVideos(
        @Query("part") part: String = "snippet",
        @Query("channelId") channelId: String,
        @Query("order") order: String = "date",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    @GET("youtube/v3/videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YouTubeVideosResponse

    @GET("youtube/v3/playlists")
    suspend fun getChannelPlaylists(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("channelId") channelId: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): YouTubePlaylistsResponse
}

data class ChannelFeedResult(
    val videos: List<VideoItem>,
    val playlists: List<PlaylistItem>,
    val subscriberCount: Long,
    val totalViews: Long,
    val isLiveApi: Boolean,
    val nextPageToken: String? = null
)

class YouTubeRepository {
    private val service: YouTubeApiService by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeApiService::class.java)
    }

    suspend fun fetchChannelFeed(pageToken: String? = null): ChannelFeedResult {
        if (!AppConfig.isYouTubeApiConfigured()) {
            return ChannelFeedResult(
                videos = SampleChannelData.videos,
                playlists = SampleChannelData.playlists,
                subscriberCount = 148500L,
                totalViews = 5240000L,
                isLiveApi = false,
                nextPageToken = null
            )
        }

        return try {
            val key = AppConfig.YOUTUBE_API_KEY
            val channelId = AppConfig.CHANNEL_ID
            val channelStats = service.getChannelStats(channelId = channelId, apiKey = key)
                .items.firstOrNull()?.statistics
            val searchResp = service.searchChannelVideos(
                channelId = channelId,
                pageToken = pageToken,
                apiKey = key
            )
            val ids = searchResp.items.mapNotNull { it.id?.videoId }.joinToString(",")
            val videos = if (ids.isNotBlank()) {
                service.getVideoDetails(videoIds = ids, apiKey = key).items.mapIndexed { idx, detail ->
                    val durationSec = parseIsoDurationSeconds(detail.contentDetails?.duration ?: "PT5M")
                    val title = detail.snippet?.title.orEmpty()
                    val cat = inferCategory(title, durationSec)
                    VideoItem(
                        id = detail.id,
                        titleBn = title,
                        titleEn = title,
                        descriptionBn = detail.snippet?.description.orEmpty(),
                        descriptionEn = detail.snippet?.description.orEmpty(),
                        category = cat,
                        thumbnailUrl = detail.snippet?.thumbnails?.high?.url
                            ?: detail.snippet?.thumbnails?.medium?.url
                            ?: "https://img.youtube.com/vi/${detail.id}/hqdefault.jpg",
                        publishedAtLabel = detail.snippet?.publishedAt?.take(10) ?: "Recent",
                        publishedEpochMillis = System.currentTimeMillis() - (idx * 86_400_000L),
                        viewCount = detail.statistics?.viewCount?.toLongOrNull() ?: 0L,
                        likeCount = detail.statistics?.likeCount?.toLongOrNull() ?: 0L,
                        durationSeconds = durationSec,
                        durationLabel = formatSeconds(durationSec),
                        trending = idx < 3
                    )
                }
            } else {
                SampleChannelData.videos
            }

            val playlistsResp = service.getChannelPlaylists(channelId = channelId, apiKey = key)
            val playlists = if (playlistsResp.items.isNotEmpty()) {
                playlistsResp.items.map { pl ->
                    val title = pl.snippet?.title.orEmpty()
                    val isStory = title.contains("গল্প") || title.contains("story", ignoreCase = true)
                    PlaylistItem(
                        id = pl.id,
                        titleBn = title,
                        titleEn = title,
                        descriptionBn = pl.snippet?.description.orEmpty(),
                        descriptionEn = pl.snippet?.description.orEmpty(),
                        category = if (isStory) "Stories" else "Tech Tutorials",
                        seriesType = if (isStory) "story_series" else "playlist",
                        thumbnailUrl = pl.snippet?.thumbnails?.high?.url
                            ?: pl.snippet?.thumbnails?.medium?.url.orEmpty(),
                        totalVideos = pl.contentDetails?.itemCount ?: 1,
                        videoIds = emptyList()
                    )
                }
            } else {
                SampleChannelData.playlists
            }

            ChannelFeedResult(
                videos = videos,
                playlists = playlists,
                subscriberCount = channelStats?.subscriberCount?.toLongOrNull() ?: 148500L,
                totalViews = channelStats?.viewCount?.toLongOrNull() ?: 5240000L,
                isLiveApi = true,
                nextPageToken = searchResp.nextPageToken
            )
        } catch (e: Exception) {
            Log.w("YouTubeRepo", "YouTube Data API fallback to local sample/cache: ${e.message}")
            ChannelFeedResult(
                videos = SampleChannelData.videos,
                playlists = SampleChannelData.playlists,
                subscriberCount = 148500L,
                totalViews = 5240000L,
                isLiveApi = false,
                nextPageToken = null
            )
        }
    }

    private fun inferCategory(title: String, durationSeconds: Int): String {
        val lower = title.lowercase()
        return when {
            durationSeconds <= 62 || lower.contains("#shorts") -> "Shorts"
            lower.contains("গল্প") || lower.contains("story") || lower.contains("পর্ব") -> "Stories"
            lower.contains("tip") || lower.contains("টিপস") || lower.contains("ট্রিক") -> "Tips"
            else -> "Tech Tutorials"
        }
    }

    private fun parseIsoDurationSeconds(iso: String): Int {
        val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
        val match = regex.matchEntire(iso) ?: return 300
        val h = match.groupValues[1].toIntOrNull() ?: 0
        val m = match.groupValues[2].toIntOrNull() ?: 0
        val s = match.groupValues[3].toIntOrNull() ?: 0
        return h * 3600 + m * 60 + s
    }

    private fun formatSeconds(totalSec: Int): String {
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }
}
