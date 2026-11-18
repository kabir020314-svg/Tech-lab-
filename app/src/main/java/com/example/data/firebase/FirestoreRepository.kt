package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.AnnouncementDoc
import com.example.data.model.CommunityPostDoc
import com.example.data.model.CommunityReplyDoc
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.PollDoc
import com.example.data.model.QuizQuestionDoc
import com.example.data.model.SavedVideoDoc
import com.example.data.model.UserProfileDoc
import com.example.data.model.VideoNoteDoc
import com.example.data.model.VideoRequestDoc
import com.example.data.model.VideoResourceDoc
import com.example.data.model.WatchHistoryDoc
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfileDoc?> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { it.toObject(UserProfileDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(
        userId: String,
        displayName: String,
        language: String,
        favoriteCategories: List<String>
    ): Result<Unit> = try {
        val uid = requireUserId()
        check(uid == userId)
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        val safeName = displayName.trim().ifBlank { "Lab Member" }.take(80)
        val safeLang = if (language == "en") "en" else "bn"
        if (!snap.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "displayName" to safeName,
                "preferredLanguage" to safeLang,
                "favoriteCategories" to favoriteCategories.take(10),
                "xpPoints" to 50L,
                "streakDays" to 1L,
                "watchedCount" to 0L,
                "watchMinutes" to 0L,
                "badges" to listOf("First Explorer"),
                "premiumMember" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } else {
            docRef.update(
                mapOf(
                    "displayName" to safeName,
                    "preferredLanguage" to safeLang,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "users/$userId")
        Result.failure(e)
    }

    suspend fun updateProfileStats(
        userId: String,
        xpDelta: Long = 0L,
        streakDays: Long? = null,
        watchedCountDelta: Long = 0L,
        watchMinutesDelta: Long = 0L,
        favoriteCategories: List<String>? = null,
        preferredLanguage: String? = null
    ): Result<Unit> = try {
        val uid = requireUserId()
        check(uid == userId)
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            ensureUserProfile(uid, auth.currentUser?.displayName ?: "Lab Member", preferredLanguage ?: "bn", favoriteCategories ?: emptyList())
        }
        val currentXp = snap.getLong("xpPoints") ?: 50L
        val currentWatched = snap.getLong("watchedCount") ?: 0L
        val currentMinutes = snap.getLong("watchMinutes") ?: 0L
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val newXp = (currentXp + xpDelta).coerceAtLeast(0L)
        if (xpDelta != 0L) updates["xpPoints"] = newXp
        if (streakDays != null) updates["streakDays"] = streakDays
        if (watchedCountDelta != 0L) updates["watchedCount"] = (currentWatched + watchedCountDelta).coerceAtLeast(0L)
        if (watchMinutesDelta != 0L) updates["watchMinutes"] = (currentMinutes + watchMinutesDelta).coerceAtLeast(0L)
        if (favoriteCategories != null) updates["favoriteCategories"] = favoriteCategories.take(10)
        if (preferredLanguage != null) updates["preferredLanguage"] = if (preferredLanguage == "en") "en" else "bn"

        docRef.update(updates).await()

        // Sync public leaderboard document
        val displayName = (snap.getString("displayName") ?: auth.currentUser?.displayName ?: "Lab Member").take(80)
        val streak = streakDays ?: (snap.getLong("streakDays") ?: 1L)
        val badgesCount = when {
            newXp >= 500 -> 4L
            newXp >= 200 -> 3L
            newXp >= 100 -> 2L
            else -> 1L
        }
        db.collection("leaderboard").document(uid).set(
            mapOf(
                "userId" to uid,
                "displayName" to displayName,
                "xpPoints" to newXp,
                "streakDays" to streak,
                "badgesCount" to badgesCount,
                "visibility" to "public",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "users/$userId")
        Result.failure(e)
    }

    // --- Saved Videos, Notes & Watch History ---

    fun observeSavedVideos(userId: String): Flow<List<SavedVideoDoc>> = flow {
        val path = "users/$userId/saved_videos"
        emitAll(
            db.collection("users").document(userId).collection("saved_videos")
                .snapshots()
                .map { it.toObjects(SavedVideoDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun toggleSavedVideo(
        videoId: String,
        title: String,
        category: String,
        thumbnailUrl: String,
        durationLabel: String,
        save: Boolean
    ): Result<Unit> = try {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("saved_videos").document(videoId)
        if (save) {
            ref.set(
                mapOf(
                    "userId" to uid,
                    "videoId" to videoId.take(64),
                    "title" to title.take(200).ifBlank { "Video" },
                    "category" to category.take(64).ifBlank { "Tech Tutorials" },
                    "thumbnailUrl" to thumbnailUrl.take(500),
                    "durationLabel" to durationLabel.take(32),
                    "savedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            ref.delete().await()
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/saved_videos/$videoId")
        Result.failure(e)
    }

    fun observeVideoNotes(userId: String): Flow<List<VideoNoteDoc>> = flow {
        val path = "users/$userId/video_notes"
        emitAll(
            db.collection("users").document(userId).collection("video_notes")
                .snapshots()
                .map { it.toObjects(VideoNoteDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveVideoNote(
        noteId: String,
        videoId: String,
        videoTitle: String,
        content: String,
        timestampLabel: String?
    ): Result<Unit> = try {
        val uid = requireUserId()
        val trimmedContent = content.trim().take(2000)
        require(trimmedContent.isNotEmpty()) { "Note content cannot be empty" }
        val ref = db.collection("users").document(uid).collection("video_notes").document(noteId)
        val payload = mapOf(
            "userId" to uid,
            "videoId" to videoId.take(64),
            "videoTitle" to videoTitle.take(200),
            "content" to trimmedContent,
            "timestampLabel" to timestampLabel?.take(32)?.takeIf { it.isNotBlank() },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
        ref.set(payload).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "users/${auth.currentUser?.uid}/video_notes/$noteId")
        Result.failure(e)
    }

    suspend fun deleteVideoNote(noteId: String): Result<Unit> = try {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("video_notes").document(noteId).delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "users/${auth.currentUser?.uid}/video_notes/$noteId")
        Result.failure(e)
    }

    suspend fun upsertWatchHistory(
        videoId: String,
        title: String,
        playlistId: String?,
        progressPercent: Int
    ): Result<Unit> = try {
        val uid = requireUserId()
        val payload = mapOf(
            "userId" to uid,
            "videoId" to videoId.take(64),
            "title" to title.take(200),
            "playlistId" to playlistId?.take(64)?.takeIf { it.isNotBlank() },
            "progressPercent" to progressPercent.coerceIn(0, 100).toLong(),
            "lastWatchedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
        db.collection("users").document(uid).collection("watch_history").document(videoId).set(payload).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/watch_history/$videoId")
        Result.failure(e)
    }

    // --- Public Filtered Collections (Community, Polls, Announcements, Quizzes, Resources, Leaderboard) ---

    fun observeCommunityPosts(): Flow<List<CommunityPostDoc>> = flow {
        val path = "community_posts"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(CommunityPostDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createCommunityPost(
        authorName: String,
        title: String,
        body: String,
        category: String
    ): Result<Unit> = try {
        val uid = requireUserId()
        val docRef = db.collection("community_posts").document()
        val safeTitle = title.trim().take(160)
        val safeBody = body.trim().take(2000)
        require(safeTitle.length >= 2 && safeBody.length >= 2)
        docRef.set(
            mapOf(
                "userId" to uid,
                "authorName" to authorName.trim().ifBlank { "Member" }.take(80),
                "title" to safeTitle,
                "body" to safeBody,
                "category" to category.ifBlank { "Tech Tutorials" }.take(64),
                "visibility" to "public",
                "likesCount" to 0L,
                "repliesCount" to 0L,
                "reported" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "community_posts")
        Result.failure(e)
    }

    suspend fun likeCommunityPost(postId: String, currentLikes: Long): Result<Unit> = try {
        requireUserId()
        db.collection("community_posts").document(postId)
            .update(
                mapOf(
                    "likesCount" to currentLikes + 1L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "community_posts/$postId")
        Result.failure(e)
    }

    suspend fun reportCommunityPost(postId: String): Result<Unit> = try {
        requireUserId()
        db.collection("community_posts").document(postId)
            .update(
                mapOf(
                    "reported" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "community_posts/$postId")
        Result.failure(e)
    }

    suspend fun deleteCommunityPost(postId: String): Result<Unit> = try {
        requireUserId()
        db.collection("community_posts").document(postId).delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "community_posts/$postId")
        Result.failure(e)
    }

    fun observeReplies(postId: String): Flow<List<CommunityReplyDoc>> = flow {
        val path = "community_posts/$postId/replies"
        emitAll(
            db.collection("community_posts").document(postId).collection("replies")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(CommunityReplyDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addReply(postId: String, authorName: String, text: String, currentRepliesCount: Long): Result<Unit> = try {
        val uid = requireUserId()
        val safeText = text.trim().take(1000)
        require(safeText.isNotEmpty())
        val replyRef = db.collection("community_posts").document(postId).collection("replies").document()
        val postRef = db.collection("community_posts").document(postId)
        val batch = db.batch()
        batch.set(
            replyRef,
            mapOf(
                "userId" to uid,
                "postId" to postId,
                "authorName" to authorName.trim().ifBlank { "Member" }.take(80),
                "text" to safeText,
                "visibility" to "public",
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        batch.update(
            postRef,
            mapOf(
                "repliesCount" to currentRepliesCount + 1L,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.commit().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "community_posts/$postId/replies")
        Result.failure(e)
    }

    suspend fun submitCommunityJoin(fullName: String, contactInfo: String, interests: List<String>): Result<Unit> = try {
        val uid = requireUserId()
        require(fullName.trim().length >= 2 && contactInfo.trim().length >= 3)
        db.collection("community_joins").document().set(
            mapOf(
                "userId" to uid,
                "fullName" to fullName.trim().take(100),
                "contactInfo" to contactInfo.trim().take(160),
                "interests" to interests.take(10),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "community_joins")
        Result.failure(e)
    }

    suspend fun submitVideoRequestOrFeedback(
        requesterName: String,
        requestType: String,
        topic: String,
        details: String
    ): Result<Unit> = try {
        val uid = requireUserId()
        require(topic.trim().length >= 2 && details.trim().length >= 2)
        val safeType = if (requestType == "feedback") "feedback" else "video_request"
        db.collection("video_requests").document().set(
            mapOf(
                "userId" to uid,
                "requesterName" to requesterName.trim().ifBlank { "Viewer" }.take(100),
                "requestType" to safeType,
                "topic" to topic.trim().take(180),
                "details" to details.trim().take(2000),
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "video_requests")
        Result.failure(e)
    }

    fun observePolls(): Flow<List<PollDoc>> = flow {
        val path = "polls"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(PollDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun votePoll(poll: PollDoc, optionIndex: Int): Result<Unit> = try {
        val uid = requireUserId()
        require(optionIndex in poll.options.indices)
        val pollRef = db.collection("polls").document(poll.id)
        val voteRef = pollRef.collection("votes").document(uid)
        val existingVote = voteRef.get().await()
        if (existingVote.exists()) {
            return Result.failure(IllegalStateException("Already voted on this poll"))
        }
        val updatedCounts = poll.voteCounts.toMutableList()
        while (updatedCounts.size < poll.options.size) updatedCounts.add(0L)
        updatedCounts[optionIndex] = updatedCounts[optionIndex] + 1L

        val batch = db.batch()
        batch.set(
            voteRef,
            mapOf(
                "userId" to uid,
                "optionIndex" to optionIndex.toLong(),
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        batch.update(
            pollRef,
            mapOf(
                "voteCounts" to updatedCounts,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.commit().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "polls/${poll.id}")
        Result.failure(e)
    }

    fun observeAnnouncements(): Flow<List<AnnouncementDoc>> = flow {
        val path = "announcements"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(AnnouncementDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntryDoc>> = flow {
        val path = "leaderboard"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snap ->
                    snap.toObjects(LeaderboardEntryDoc::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.xpPoints }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeRemoteQuizzes(): Flow<List<QuizQuestionDoc>> = flow {
        val path = "quizzes"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(QuizQuestionDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeRemoteResources(): Flow<List<VideoResourceDoc>> = flow {
        val path = "video_resources"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { it.toObjects(VideoResourceDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeMyVideoRequests(userId: String, isAdmin: Boolean): Flow<List<VideoRequestDoc>> = flow {
        val path = "video_requests"
        val query = if (isAdmin) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("userId", userId)
        }
        emitAll(
            query.snapshots()
                .map { it.toObjects(VideoRequestDoc::class.java, ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val fsErr = unwrapFirestoreFailure(error)
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // --- Admin Actions ---

    suspend fun adminPublishAnnouncement(titleBn: String, titleEn: String, bodyBn: String, bodyEn: String): Result<Unit> = try {
        requireUserId()
        db.collection("announcements").document("pinned_banner").set(
            mapOf(
                "titleBn" to titleBn.trim().take(160),
                "titleEn" to titleEn.trim().take(160),
                "bodyBn" to bodyBn.trim().take(500),
                "bodyEn" to bodyEn.trim().take(500),
                "active" to true,
                "visibility" to "public",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "announcements/pinned_banner")
        Result.failure(e)
    }

    suspend fun adminCreatePoll(questionBn: String, questionEn: String, options: List<String>): Result<Unit> = try {
        requireUserId()
        val cleanOpts = options.map { it.trim() }.filter { it.isNotBlank() }.take(6)
        require(cleanOpts.size >= 2)
        db.collection("polls").document().set(
            mapOf(
                "questionBn" to questionBn.trim().take(240),
                "questionEn" to questionEn.trim().take(240),
                "options" to cleanOpts,
                "voteCounts" to List(cleanOpts.size) { 0L },
                "visibility" to "public",
                "active" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "polls")
        Result.failure(e)
    }

    suspend fun adminCreateQuiz(
        questionBn: String,
        questionEn: String,
        optionsBn: List<String>,
        optionsEn: List<String>,
        correctIndex: Int,
        explanationBn: String,
        explanationEn: String
    ): Result<Unit> = try {
        requireUserId()
        require(optionsBn.size == 4 && optionsEn.size == 4 && correctIndex in 0..3)
        db.collection("quizzes").document().set(
            mapOf(
                "questionBn" to questionBn.trim().take(300),
                "questionEn" to questionEn.trim().take(300),
                "optionsBn" to optionsBn,
                "optionsEn" to optionsEn,
                "correctIndex" to correctIndex.toLong(),
                "explanationBn" to explanationBn.trim().take(500),
                "explanationEn" to explanationEn.trim().take(500),
                "xpReward" to 30L,
                "visibility" to "public",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "quizzes")
        Result.failure(e)
    }

    suspend fun adminAddVideoResource(
        videoId: String,
        titleBn: String,
        titleEn: String,
        resourceType: String,
        url: String
    ): Result<Unit> = try {
        requireUserId()
        db.collection("video_resources").document().set(
            mapOf(
                "videoId" to videoId.trim().take(64),
                "titleBn" to titleBn.trim().take(180),
                "titleEn" to titleEn.trim().take(180),
                "resourceType" to resourceType,
                "url" to url.trim().take(500),
                "visibility" to "public",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, "video_resources")
        Result.failure(e)
    }
}
