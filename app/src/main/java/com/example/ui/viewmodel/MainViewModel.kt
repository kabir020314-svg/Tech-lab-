package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.AppConfig
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.UiState
import com.example.data.firebase.unwrapFirestoreFailure
import com.example.data.local.CachedPlaylistEntity
import com.example.data.local.CachedVideoEntity
import com.example.data.local.ChannelCacheDao
import com.example.data.local.LocalHistoryEntity
import com.example.data.local.LocalLikedVideoEntity
import com.example.data.local.LocalNoteEntity
import com.example.data.local.LocalSavedVideoEntity
import com.example.data.local.OfflineResourceEntity
import com.example.data.local.RecentSearchEntity
import com.example.data.local.UserPreferencesState
import com.example.data.local.UserPreferencesStore
import com.example.data.model.AnnouncementDoc
import com.example.data.model.AppCategory
import com.example.data.model.CommunityPostDoc
import com.example.data.model.CommunityReplyDoc
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.NotificationItem
import com.example.data.model.PlaylistItem
import com.example.data.model.PollDoc
import com.example.data.model.QuizQuestionItem
import com.example.data.model.ResourceType
import com.example.data.model.UserProfileDoc
import com.example.data.model.VideoDurationFilter
import com.example.data.model.VideoItem
import com.example.data.model.VideoRequestDoc
import com.example.data.model.VideoResourceItem
import com.example.data.model.VideoSortOption
import com.example.data.remote.SampleChannelData
import com.example.data.remote.YouTubeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(
    private val cacheDao: ChannelCacheDao,
    private val prefsStore: UserPreferencesStore,
    private val youtubeRepo: YouTubeRepository,
    private val firestoreRepo: FirestoreRepository?,
    val currentUserId: String?,
    val currentUserEmail: String?,
    val currentUserDisplayName: String?
) : ViewModel() {

    val isSignedIn: Boolean get() = !currentUserId.isNullOrBlank()
    val isAdminUser: Boolean get() = isSignedIn && currentUserEmail.equals(AppConfig.ADMIN_EMAIL, ignoreCase = true)

    val preferences: StateFlow<UserPreferencesState> = prefsStore.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesState())

    private val _feedLoading = MutableStateFlow(true)
    val feedLoading: StateFlow<Boolean> = _feedLoading.asStateFlow()

    private val _feedError = MutableStateFlow<String?>(null)
    val feedError: StateFlow<String?> = _feedError.asStateFlow()

    private val _isLiveYouTubeApi = MutableStateFlow(false)
    val isLiveYouTubeApi: StateFlow<Boolean> = _isLiveYouTubeApi.asStateFlow()

    private val _subscriberCount = MutableStateFlow(148500L)
    val subscriberCount: StateFlow<Long> = _subscriberCount.asStateFlow()

    private val _totalViews = MutableStateFlow(5240000L)
    val totalViews: StateFlow<Long> = _totalViews.asStateFlow()

    private val _visibleVideoLimit = MutableStateFlow(6)
    val visibleVideoLimit: StateFlow<Int> = _visibleVideoLimit.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(VideoSortOption.NEWEST)
    val sortOption: StateFlow<VideoSortOption> = _sortOption.asStateFlow()

    private val _durationFilter = MutableStateFlow(VideoDurationFilter.ALL)
    val durationFilter: StateFlow<VideoDurationFilter> = _durationFilter.asStateFlow()

    private val _gridMode = MutableStateFlow(false)
    val gridMode: StateFlow<Boolean> = _gridMode.asStateFlow()

    private val _actionBannerMessage = MutableStateFlow<String?>(null)
    val actionBannerMessage: StateFlow<String?> = _actionBannerMessage.asStateFlow()

    // Cached + sample videos
    val videos: StateFlow<List<VideoItem>> = cacheDao.observeCachedVideos()
        .map { entities ->
            if (entities.isEmpty()) SampleChannelData.videos
            else entities.map { it.toDomain() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleChannelData.videos)

    val playlists: StateFlow<List<PlaylistItem>> = cacheDao.observeCachedPlaylists()
        .map { entities ->
            if (entities.isEmpty()) SampleChannelData.playlists
            else entities.map { it.toDomain() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleChannelData.playlists)

    val savedVideoIds: StateFlow<Set<String>> = cacheDao.observeSavedVideos()
        .map { list -> list.map { it.videoId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val likedVideoIds: StateFlow<Set<String>> = cacheDao.observeLikedVideos()
        .map { list -> list.map { it.videoId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val watchHistory: StateFlow<List<LocalHistoryEntity>> = cacheDao.observeWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localNotes: StateFlow<List<LocalNoteEntity>> = cacheDao.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineResources: StateFlow<List<OfflineResourceEntity>> = cacheDao.observeOfflineResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<String>> = cacheDao.observeRecentSearches()
        .map { items -> items.map { it.queryText } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _notifications = MutableStateFlow(SampleChannelData.defaultNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // --- Firestore Live Streams (strictly gated on currentUserId != null) ---

    val userProfileState: StateFlow<UiState<UserProfileDoc?>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeUserProfile(currentUserId)
                .map<UserProfileDoc?, UiState<UserProfileDoc?>> { UiState.Success(it) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    emit(UiState.Error((fsErr ?: error).message ?: "Failed to load profile"))
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
        } else {
            MutableStateFlow(UiState.Success(null))
        }

    val communityPostsState: StateFlow<UiState<List<CommunityPostDoc>>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeCommunityPosts()
                .map<List<CommunityPostDoc>, UiState<List<CommunityPostDoc>>> { list ->
                    UiState.Success(list.sortedByDescending { it.createdAt?.seconds ?: 0L })
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    emit(UiState.Error((fsErr ?: error).message ?: "Failed to load community posts"))
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
        } else {
            MutableStateFlow(UiState.Success(defaultCommunityPostsPreview()))
        }

    val pollsState: StateFlow<UiState<List<PollDoc>>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observePolls()
                .map<List<PollDoc>, UiState<List<PollDoc>>> { UiState.Success(it) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    emit(UiState.Error((fsErr ?: error).message ?: "Failed to load polls"))
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
        } else {
            MutableStateFlow(UiState.Success(defaultPollsPreview()))
        }

    val announcementsState: StateFlow<List<AnnouncementDoc>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeAnnouncements()
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    Log.w("MainVM", "Announcement error: ${error.message}")
                    emit(emptyList())
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } else {
            MutableStateFlow(listOf(defaultAnnouncementPreview()))
        }

    val leaderboardState: StateFlow<List<LeaderboardEntryDoc>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeLeaderboard()
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    emit(emptyList())
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultLeaderboardPreview())
        } else {
            MutableStateFlow(defaultLeaderboardPreview())
        }

    val allQuizQuestions: StateFlow<List<QuizQuestionItem>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeRemoteQuizzes()
                .map { remote ->
                    val mapped = remote.map {
                        QuizQuestionItem(
                            id = it.id,
                            questionBn = it.questionBn,
                            questionEn = it.questionEn,
                            optionsBn = it.optionsBn,
                            optionsEn = it.optionsEn,
                            correctIndex = it.correctIndex.toInt(),
                            explanationBn = it.explanationBn,
                            explanationEn = it.explanationEn,
                            xpReward = it.xpReward.toInt()
                        )
                    }
                    (mapped + SampleChannelData.quizzes).distinctBy { it.id }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    emit(SampleChannelData.quizzes)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleChannelData.quizzes)
        } else {
            MutableStateFlow(SampleChannelData.quizzes)
        }

    val allResources: StateFlow<List<VideoResourceItem>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeRemoteResources()
                .map { remote ->
                    val mapped = remote.map {
                        VideoResourceItem(
                            id = it.id,
                            videoId = it.videoId,
                            titleBn = it.titleBn,
                            titleEn = it.titleEn,
                            resourceType = ResourceType.fromRaw(it.resourceType),
                            url = it.url
                        )
                    }
                    (mapped + SampleChannelData.videoResources).distinctBy { it.id }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    emit(SampleChannelData.videoResources)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleChannelData.videoResources)
        } else {
            MutableStateFlow(SampleChannelData.videoResources)
        }

    val myVideoRequests: StateFlow<List<VideoRequestDoc>> =
        if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeMyVideoRequests(currentUserId, isAdminUser)
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    emit(emptyList())
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } else {
            MutableStateFlow(emptyList())
        }

    val filteredVideos: StateFlow<List<VideoItem>> = combine(
        videos,
        _selectedCategory,
        _sortOption,
        _durationFilter,
        _visibleVideoLimit
    ) { all, category, sort, duration, limit ->
        val byCat = if (category == AppCategory.ALL) all else all.filter { it.category.equals(category.key, true) }
        val byDur = when (duration) {
            VideoDurationFilter.ALL -> byCat
            VideoDurationFilter.SHORT -> byCat.filter { it.durationSeconds < 240 }
            VideoDurationFilter.MEDIUM -> byCat.filter { it.durationSeconds in 240..1200 }
            VideoDurationFilter.LONG -> byCat.filter { it.durationSeconds > 1200 }
        }
        val sorted = when (sort) {
            VideoSortOption.NEWEST -> byDur.sortedByDescending { it.publishedEpochMillis }
            VideoSortOption.MOST_VIEWED -> byDur.sortedByDescending { it.viewCount }
        }
        sorted.take(limit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleChannelData.videos)

    init {
        refreshChannelFeed()
        syncUserProfileOnSignIn()
    }

    private fun syncUserProfileOnSignIn() {
        if (currentUserId != null && firestoreRepo != null) {
            viewModelScope.launch {
                val pref = prefsStore.preferences.first()
                firestoreRepo.ensureUserProfile(
                    userId = currentUserId,
                    displayName = currentUserDisplayName ?: "Lab Member",
                    language = pref.languageCode,
                    favoriteCategories = pref.favoriteCategories.toList()
                )
            }
        }
    }

    fun refreshChannelFeed() {
        viewModelScope.launch {
            _feedLoading.value = true
            _feedError.value = null
            try {
                delay(350) // brief skeleton polish
                val result = youtubeRepo.fetchChannelFeed()
                _isLiveYouTubeApi.value = result.isLiveApi
                _subscriberCount.value = result.subscriberCount
                _totalViews.value = result.totalViews
                cacheDao.insertVideos(result.videos.map { it.toEntity() })
                cacheDao.insertPlaylists(result.playlists.map { it.toEntity() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _feedError.value = e.localizedMessage ?: "Failed to refresh channel feed"
            } finally {
                _feedLoading.value = false
            }
        }
    }

    fun loadMoreVideos() {
        _visibleVideoLimit.value = _visibleVideoLimit.value + 6
    }

    fun selectCategory(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun setSortOption(option: VideoSortOption) {
        _sortOption.value = option
    }

    fun setDurationFilter(filter: VideoDurationFilter) {
        _durationFilter.value = filter
    }

    fun toggleGridMode() {
        _gridMode.value = !_gridMode.value
    }

    fun clearBannerMessage() {
        _actionBannerMessage.value = null
    }

    // --- Preferences & Onboarding ---

    fun setLanguage(code: String) {
        viewModelScope.launch {
            prefsStore.setLanguage(code)
            if (currentUserId != null && firestoreRepo != null) {
                firestoreRepo.updateProfileStats(currentUserId, preferredLanguage = code)
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefsStore.setThemeMode(mode) }
    }

    fun setFontSize(size: String) {
        viewModelScope.launch { prefsStore.setFontSize(size) }
    }

    fun setDataSaver(enabled: Boolean) {
        viewModelScope.launch { prefsStore.setDataSaver(enabled) }
    }

    fun completeOnboarding(language: String, categories: Set<String>) {
        viewModelScope.launch { prefsStore.completeOnboarding(language, categories) }
    }

    fun setFavoriteCategories(categories: Set<String>) {
        viewModelScope.launch {
            prefsStore.setFavoriteCategories(categories)
            if (currentUserId != null && firestoreRepo != null) {
                firestoreRepo.updateProfileStats(currentUserId, favoriteCategories = categories.toList())
            }
        }
    }

    fun setGuestPreviewChosen(chosen: Boolean) {
        viewModelScope.launch { prefsStore.setGuestPreviewChosen(chosen) }
    }

    fun updateNotificationSetting(key: String, enabled: Boolean) {
        viewModelScope.launch { prefsStore.updateNotificationSetting(key, enabled) }
    }

    fun toggleBellSubscription() {
        viewModelScope.launch {
            val next = !preferences.value.bellSubscribed
            prefsStore.setBellSubscribed(next)
            _actionBannerMessage.value = if (next) "Bell reminder enabled!" else "Bell reminder turned off"
        }
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            cacheDao.clearVideos()
            cacheDao.clearPlaylists()
            cacheDao.clearRecentSearches()
            refreshChannelFeed()
            _actionBannerMessage.value = "Cache cleared & refreshed"
        }
    }

    // --- Video Actions (Save, Like, Watch History, Notes, Resources) ---

    fun toggleSaveVideo(video: VideoItem) {
        viewModelScope.launch {
            val currentlySaved = savedVideoIds.value.contains(video.id)
            if (currentlySaved) {
                cacheDao.removeSavedVideo(video.id)
            } else {
                cacheDao.saveVideo(LocalSavedVideoEntity(video.id))
            }
            if (currentUserId != null && firestoreRepo != null) {
                firestoreRepo.toggleSavedVideo(
                    videoId = video.id,
                    title = video.titleEn,
                    category = video.category,
                    thumbnailUrl = video.thumbnailUrl,
                    durationLabel = video.durationLabel,
                    save = !currentlySaved
                )
            }
        }
    }

    fun toggleLikeVideo(video: VideoItem) {
        viewModelScope.launch {
            if (likedVideoIds.value.contains(video.id)) {
                cacheDao.unlikeVideo(video.id)
            } else {
                cacheDao.likeVideo(LocalLikedVideoEntity(video.id))
            }
        }
    }

    fun recordVideoWatch(video: VideoItem, progressPercent: Int = 45) {
        viewModelScope.launch {
            cacheDao.upsertHistory(
                LocalHistoryEntity(
                    videoId = video.id,
                    title = video.titleBn,
                    playlistId = video.playlistId,
                    progressPercent = progressPercent,
                    lastWatchedMillis = System.currentTimeMillis()
                )
            )
            prefsStore.addLocalXp(10)
            if (currentUserId != null && firestoreRepo != null) {
                firestoreRepo.upsertWatchHistory(video.id, video.titleEn, video.playlistId, progressPercent)
                firestoreRepo.updateProfileStats(
                    userId = currentUserId,
                    xpDelta = 10L,
                    watchedCountDelta = 1L,
                    watchMinutesDelta = (video.durationSeconds / 60L).coerceAtLeast(1L)
                )
            }
        }
    }

    fun saveNote(videoId: String, videoTitle: String, content: String, timestampLabel: String? = null) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val id = UUID.randomUUID().toString().replace("-", "").take(16)
            cacheDao.upsertNote(
                LocalNoteEntity(
                    id = id,
                    videoId = videoId,
                    videoTitle = videoTitle,
                    content = content.trim(),
                    timestampLabel = timestampLabel?.takeIf { it.isNotBlank() },
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            if (currentUserId != null && firestoreRepo != null) {
                firestoreRepo.saveVideoNote(id, videoId, videoTitle, content, timestampLabel)
            }
            _actionBannerMessage.value = "Note saved"
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            cacheDao.deleteNote(noteId)
            if (currentUserId != null && firestoreRepo != null) {
                firestoreRepo.deleteVideoNote(noteId)
            }
        }
    }

    fun saveResourceOffline(resource: VideoResourceItem) {
        viewModelScope.launch {
            cacheDao.saveOfflineResource(
                OfflineResourceEntity(
                    id = resource.id,
                    videoId = resource.videoId,
                    titleBn = resource.titleBn,
                    titleEn = resource.titleEn,
                    resourceType = resource.resourceType.name,
                    url = resource.url,
                    snippetOrNote = resource.snippetOrNote,
                    savedAtMillis = System.currentTimeMillis()
                )
            )
            _actionBannerMessage.value = "Saved for offline access"
        }
    }

    fun removeOfflineResource(id: String) {
        viewModelScope.launch { cacheDao.removeOfflineResource(id) }
    }

    fun addRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            cacheDao.addRecentSearch(RecentSearchEntity(clean, System.currentTimeMillis()))
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { cacheDao.clearRecentSearches() }
    }

    // --- Quiz & Gamification ---

    fun answerQuiz(question: QuizQuestionItem, selectedIndex: Int) {
        viewModelScope.launch {
            val isCorrect = selectedIndex == question.correctIndex
            val xp = if (isCorrect) question.xpReward else 5
            prefsStore.addLocalXp(xp, question.id)
            if (currentUserId != null && firestoreRepo != null) {
                firestoreRepo.updateProfileStats(currentUserId, xpDelta = xp.toLong())
            }
        }
    }

    fun dailyCheckIn() {
        viewModelScope.launch {
            val today = System.currentTimeMillis() / 86_400_000L
            val awarded = prefsStore.recordDailyCheckIn(today)
            if (awarded) {
                if (currentUserId != null && firestoreRepo != null) {
                    firestoreRepo.updateProfileStats(
                        userId = currentUserId,
                        xpDelta = 15L,
                        streakDays = preferences.value.localStreak.toLong()
                    )
                }
                _actionBannerMessage.value = "+15 XP Daily Check-in!"
            } else {
                _actionBannerMessage.value = "Already checked in today!"
            }
        }
    }

    // --- Community, Polls, Requests & Admin ---

    fun observePostReplies(postId: String): Flow<List<CommunityReplyDoc>> {
        return if (currentUserId != null && firestoreRepo != null) {
            firestoreRepo.observeReplies(postId).catch { error ->
                val fsErr = unwrapFirestoreFailure(error)
                if (fsErr == null && error is CancellationException) throw error
                emit(emptyList())
            }
        } else {
            flowOf(emptyList())
        }
    }

    fun createCommunityPost(title: String, body: String, category: String, onDone: (Boolean) -> Unit) {
        val repo = firestoreRepo ?: run { onDone(false); return }
        viewModelScope.launch {
            val res = repo.createCommunityPost(currentUserDisplayName ?: "Lab Member", title, body, category)
            _actionBannerMessage.value = if (res.isSuccess) "Post published!" else res.exceptionOrNull()?.localizedMessage
            onDone(res.isSuccess)
        }
    }

    fun likePost(post: CommunityPostDoc) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch { repo.likeCommunityPost(post.id, post.likesCount) }
    }

    fun reportPost(post: CommunityPostDoc) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch {
            val res = repo.reportCommunityPost(post.id)
            if (res.isSuccess) _actionBannerMessage.value = "Post reported for moderation"
        }
    }

    fun deletePost(postId: String) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch { repo.deleteCommunityPost(postId) }
    }

    fun addReply(post: CommunityPostDoc, text: String, onDone: (Boolean) -> Unit) {
        val repo = firestoreRepo ?: run { onDone(false); return }
        viewModelScope.launch {
            val res = repo.addReply(post.id, currentUserDisplayName ?: "Member", text, post.repliesCount)
            onDone(res.isSuccess)
        }
    }

    fun votePoll(poll: PollDoc, optionIndex: Int) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch {
            val res = repo.votePoll(poll, optionIndex)
            _actionBannerMessage.value = if (res.isSuccess) "Vote recorded!" else "Already voted or unavailable"
        }
    }

    fun submitCommunityJoin(fullName: String, contactInfo: String, interests: List<String>, onDone: (Boolean) -> Unit) {
        val repo = firestoreRepo ?: run { onDone(false); return }
        viewModelScope.launch {
            val res = repo.submitCommunityJoin(fullName, contactInfo, interests)
            _actionBannerMessage.value = if (res.isSuccess) "Joined Kabir Tech Lab Community!" else res.exceptionOrNull()?.localizedMessage
            onDone(res.isSuccess)
        }
    }

    fun submitVideoRequestOrFeedback(type: String, topic: String, details: String, onDone: (Boolean) -> Unit) {
        val repo = firestoreRepo ?: run { onDone(false); return }
        viewModelScope.launch {
            val res = repo.submitVideoRequestOrFeedback(currentUserDisplayName ?: "Viewer", type, topic, details)
            _actionBannerMessage.value = if (res.isSuccess) "Submitted successfully!" else res.exceptionOrNull()?.localizedMessage
            onDone(res.isSuccess)
        }
    }

    // Admin actions
    fun adminPublishAnnouncement(titleBn: String, titleEn: String, bodyBn: String, bodyEn: String) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch {
            val res = repo.adminPublishAnnouncement(titleBn, titleEn, bodyBn, bodyEn)
            _actionBannerMessage.value = if (res.isSuccess) "Announcement & push notice published!" else res.exceptionOrNull()?.localizedMessage
        }
    }

    fun adminCreatePoll(questionBn: String, questionEn: String, options: List<String>) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch {
            val res = repo.adminCreatePoll(questionBn, questionEn, options)
            _actionBannerMessage.value = if (res.isSuccess) "Poll created!" else res.exceptionOrNull()?.localizedMessage
        }
    }

    fun adminCreateQuiz(
        questionBn: String,
        questionEn: String,
        optionsBn: List<String>,
        optionsEn: List<String>,
        correctIndex: Int,
        explanationBn: String,
        explanationEn: String
    ) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch {
            val res = repo.adminCreateQuiz(questionBn, questionEn, optionsBn, optionsEn, correctIndex, explanationBn, explanationEn)
            _actionBannerMessage.value = if (res.isSuccess) "Quiz question added!" else res.exceptionOrNull()?.localizedMessage
        }
    }

    fun adminAddResource(videoId: String, titleBn: String, titleEn: String, type: String, url: String) {
        val repo = firestoreRepo ?: return
        viewModelScope.launch {
            val res = repo.adminAddVideoResource(videoId, titleBn, titleEn, type, url)
            _actionBannerMessage.value = if (res.isSuccess) "Resource attached to video!" else res.exceptionOrNull()?.localizedMessage
        }
    }

    // --- Mappers & Offline Previews ---

    private fun CachedVideoEntity.toDomain() = VideoItem(
        id = id,
        titleBn = titleBn,
        titleEn = titleEn,
        descriptionBn = descriptionBn,
        descriptionEn = descriptionEn,
        category = category,
        playlistId = playlistId,
        partNumber = partNumber,
        thumbnailUrl = thumbnailUrl,
        publishedAtLabel = publishedAtLabel,
        publishedEpochMillis = publishedEpochMillis,
        viewCount = viewCount,
        likeCount = likeCount,
        durationSeconds = durationSeconds,
        durationLabel = durationLabel,
        trending = trending
    )

    private fun VideoItem.toEntity() = CachedVideoEntity(
        id = id,
        titleBn = titleBn,
        titleEn = titleEn,
        descriptionBn = descriptionBn,
        descriptionEn = descriptionEn,
        category = category,
        playlistId = playlistId,
        partNumber = partNumber,
        thumbnailUrl = thumbnailUrl,
        publishedAtLabel = publishedAtLabel,
        publishedEpochMillis = publishedEpochMillis,
        viewCount = viewCount,
        likeCount = likeCount,
        durationSeconds = durationSeconds,
        durationLabel = durationLabel,
        trending = trending
    )

    private fun CachedPlaylistEntity.toDomain() = PlaylistItem(
        id = id,
        titleBn = titleBn,
        titleEn = titleEn,
        descriptionBn = descriptionBn,
        descriptionEn = descriptionEn,
        category = category,
        seriesType = seriesType,
        thumbnailUrl = thumbnailUrl,
        totalVideos = totalVideos,
        videoIds = videoIdsCsv.split(",").filter { it.isNotBlank() }
    )

    private fun PlaylistItem.toEntity() = CachedPlaylistEntity(
        id = id,
        titleBn = titleBn,
        titleEn = titleEn,
        descriptionBn = descriptionBn,
        descriptionEn = descriptionEn,
        category = category,
        seriesType = seriesType,
        thumbnailUrl = thumbnailUrl,
        totalVideos = totalVideos,
        videoIdsCsv = videoIds.joinToString(",")
    )

    private fun defaultAnnouncementPreview() = AnnouncementDoc(
        id = "preview_ann",
        titleBn = "স্বাগতম Kabir Tech Lab-এ!",
        titleEn = "Welcome to Kabir Tech Lab!",
        bodyBn = "নতুন বাংলা টেক গল্প 'ঢাকার অদৃশ্য কোডার' এর ৩টি পর্বই এখন লাইভ।",
        bodyEn = "All 3 episodes of 'The Invisible Coder of Dhaka' are live now!",
        active = true
    )

    private fun defaultCommunityPostsPreview() = listOf(
        CommunityPostDoc(
            id = "sample_post_1",
            userId = "creator",
            authorName = "Kabir Hasan",
            title = "পরবর্তী Android সিরিজে কোন প্রজেক্ট বানাতে চান?",
            body = "আপনারা কি Firebase চ্যাট অ্যাপ নাকি AI নোট অ্যাপের সম্পূর্ণ বাংলা টিউটোরিয়াল চান? নিচে কমেন্টে জানান!",
            category = "Tech Tutorials",
            likesCount = 34,
            repliesCount = 6
        ),
        CommunityPostDoc(
            id = "sample_post_2",
            userId = "member_1",
            authorName = "Nadia Islam",
            title = "ঢাকার অদৃশ্য কোডার পর্ব ৩ অসাধারণ ছিল!",
            body = "গল্পের সাউন্ড ডিজাইন আর সাসপেন্স দারুণ লেগেছে। নতুন সিজন কবে আসবে?",
            category = "Stories",
            likesCount = 21,
            repliesCount = 3
        )
    )

    private fun defaultPollsPreview() = listOf(
        PollDoc(
            id = "sample_poll_1",
            questionBn = "পরের মেগা টিউটোরিয়াল কোন বিষয়ে চান?",
            questionEn = "Which mega tutorial should we publish next?",
            options = listOf(
                "Jetpack Compose + Firebase",
                "Python AI Automation",
                "Bangla Cyber Thriller Story S2",
                "Freelancing & GitHub Portfolio"
            ),
            voteCounts = listOf(42L, 28L, 51L, 19L),
            active = true
        )
    )

    private fun defaultLeaderboardPreview() = listOf(
        LeaderboardEntryDoc(id = "l1", userId = "l1", displayName = "Arif Hossain", xpPoints = 480, streakDays = 9, badgesCount = 3),
        LeaderboardEntryDoc(id = "l2", userId = "l2", displayName = "Sadia Karim", xpPoints = 395, streakDays = 6, badgesCount = 3),
        LeaderboardEntryDoc(id = "l3", userId = "l3", displayName = "Tanvir Ahmed", xpPoints = 260, streakDays = 4, badgesCount = 2)
    )
}
