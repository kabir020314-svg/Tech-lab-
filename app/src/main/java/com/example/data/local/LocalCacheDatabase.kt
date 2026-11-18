package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_videos")
data class CachedVideoEntity(
    @PrimaryKey val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val category: String,
    val playlistId: String?,
    val partNumber: Int?,
    val thumbnailUrl: String,
    val publishedAtLabel: String,
    val publishedEpochMillis: Long,
    val viewCount: Long,
    val likeCount: Long,
    val durationSeconds: Int,
    val durationLabel: String,
    val trending: Boolean
)

@Entity(tableName = "cached_playlists")
data class CachedPlaylistEntity(
    @PrimaryKey val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val category: String,
    val seriesType: String,
    val thumbnailUrl: String,
    val totalVideos: Int,
    val videoIdsCsv: String
)

@Entity(tableName = "local_saved_videos")
data class LocalSavedVideoEntity(
    @PrimaryKey val videoId: String,
    val savedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_liked_videos")
data class LocalLikedVideoEntity(
    @PrimaryKey val videoId: String,
    val likedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_watch_history")
data class LocalHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val playlistId: String?,
    val progressPercent: Int,
    val lastWatchedMillis: Long
)

@Entity(tableName = "local_notes")
data class LocalNoteEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val videoTitle: String,
    val content: String,
    val timestampLabel: String?,
    val updatedAtMillis: Long
)

@Entity(tableName = "offline_resources")
data class OfflineResourceEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val titleBn: String,
    val titleEn: String,
    val resourceType: String,
    val url: String,
    val snippetOrNote: String,
    val savedAtMillis: Long
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val queryText: String,
    val searchedAtMillis: Long
)

@Dao
interface ChannelCacheDao {
    @Query("SELECT * FROM cached_videos ORDER BY publishedEpochMillis DESC")
    fun observeCachedVideos(): Flow<List<CachedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<CachedVideoEntity>)

    @Query("DELETE FROM cached_videos")
    suspend fun clearVideos()

    @Query("SELECT * FROM cached_playlists")
    fun observeCachedPlaylists(): Flow<List<CachedPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<CachedPlaylistEntity>)

    @Query("DELETE FROM cached_playlists")
    suspend fun clearPlaylists()

    @Query("SELECT * FROM local_saved_videos ORDER BY savedAtMillis DESC")
    fun observeSavedVideos(): Flow<List<LocalSavedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVideo(entity: LocalSavedVideoEntity)

    @Query("DELETE FROM local_saved_videos WHERE videoId = :videoId")
    suspend fun removeSavedVideo(videoId: String)

    @Query("SELECT * FROM local_liked_videos")
    fun observeLikedVideos(): Flow<List<LocalLikedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeVideo(entity: LocalLikedVideoEntity)

    @Query("DELETE FROM local_liked_videos WHERE videoId = :videoId")
    suspend fun unlikeVideo(videoId: String)

    @Query("SELECT * FROM local_watch_history ORDER BY lastWatchedMillis DESC")
    fun observeWatchHistory(): Flow<List<LocalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(entity: LocalHistoryEntity)

    @Query("SELECT * FROM local_notes ORDER BY updatedAtMillis DESC")
    fun observeNotes(): Flow<List<LocalNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: LocalNoteEntity)

    @Query("DELETE FROM local_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("SELECT * FROM offline_resources ORDER BY savedAtMillis DESC")
    fun observeOfflineResources(): Flow<List<OfflineResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOfflineResource(entity: OfflineResourceEntity)

    @Query("DELETE FROM offline_resources WHERE id = :id")
    suspend fun removeOfflineResource(id: String)

    @Query("SELECT * FROM recent_searches ORDER BY searchedAtMillis DESC LIMIT 10")
    fun observeRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentSearch(entity: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()
}

@Database(
    entities = [
        CachedVideoEntity::class,
        CachedPlaylistEntity::class,
        LocalSavedVideoEntity::class,
        LocalLikedVideoEntity::class,
        LocalHistoryEntity::class,
        LocalNoteEntity::class,
        OfflineResourceEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LocalCacheDatabase : RoomDatabase() {
    abstract fun channelCacheDao(): ChannelCacheDao

    companion object {
        @Volatile
        private var INSTANCE: LocalCacheDatabase? = null

        fun getInstance(context: Context): LocalCacheDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LocalCacheDatabase::class.java,
                    "kabir_tech_lab_cache.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
