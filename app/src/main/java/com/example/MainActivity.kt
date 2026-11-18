package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.launchGoogleSignIn
import com.example.data.local.LocalCacheDatabase
import com.example.data.local.UserPreferencesState
import com.example.data.local.UserPreferencesStore
import com.example.data.remote.YouTubeRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AnimatedSplashScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.ProfileAndSettingsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ResourcesAndToolsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.theme.KabirTechLabTheme
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val VIDEOS = "videos"
    const val PLAYLISTS = "playlists"
    const val QUIZ = "quiz"
    const val COMMUNITY = "community"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val RESOURCES = "resources"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val ADMIN = "admin"
    const val VIDEO_PLAYER = "video/{videoId}"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"

    fun videoPlayer(videoId: String) = "video/$videoId"
    fun playlistDetail(playlistId: String) = "playlist/$playlistId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KabirTechLabRoot()
        }
    }
}

@Composable
fun KabirTechLabRoot() {
    val context = LocalContext.current
    val prefsStore = remember(context) { UserPreferencesStore(context.applicationContext) }
    val prefs by prefsStore.preferences.collectAsStateWithLifecycle(initialValue = UserPreferencesState())
    val lang = AppLanguage.fromCode(prefs.languageCode)

    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = scope,
            onAuthSuccess = {},
            onUnauthenticated = {}
        )
    }

    KabirTechLabTheme(
        themeMode = prefs.themeMode,
        fontSizePreference = prefs.fontSize
    ) {
        when {
            showSplash -> {
                AnimatedSplashScreen(
                    lang = lang,
                    onFinished = { showSplash = false }
                )
            }
            !prefs.onboardingDone -> {
                OnboardingScreen(
                    initialLanguage = lang,
                    onComplete = { languageCode, categories ->
                        scope.launch {
                            prefsStore.completeOnboarding(languageCode, categories)
                        }
                    }
                )
            }
            currentUser == null && !prefs.guestPreviewChosen -> {
                AuthScreen(
                    lang = lang,
                    onAuthSuccess = {},
                    onContinueGuest = {
                        scope.launch { prefsStore.setGuestPreviewChosen(true) }
                    }
                )
            }
            else -> {
                val uid = currentUser?.uid
                key(uid ?: "guest") {
                    MainAppShell(
                        currentUserId = uid,
                        currentUserEmail = currentUser?.email,
                        currentUserDisplayName = currentUser?.displayName
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppShell(
    currentUserId: String?,
    currentUserEmail: String?,
    currentUserDisplayName: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: MainViewModel = viewModel(
        key = currentUserId ?: "guest",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = LocalCacheDatabase.getInstance(app)
                val prefs = UserPreferencesStore(app)
                val yt = YouTubeRepository()
                val fs = if (currentUserId != null) FirestoreRepository(app) else null
                MainViewModel(
                    cacheDao = db.channelCacheDao(),
                    prefsStore = prefs,
                    youtubeRepo = yt,
                    firestoreRepo = fs,
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail,
                    currentUserDisplayName = currentUserDisplayName
                )
            }
        }
    )

    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = AppLanguage.fromCode(prefs.languageCode)
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val filteredVideos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val savedIds by viewModel.savedVideoIds.collectAsStateWithLifecycle()
    val likedIds by viewModel.likedVideoIds.collectAsStateWithLifecycle()
    val history by viewModel.watchHistory.collectAsStateWithLifecycle()
    val notes by viewModel.localNotes.collectAsStateWithLifecycle()
    val offlineResources by viewModel.offlineResources.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val durationFilter by viewModel.durationFilter.collectAsStateWithLifecycle()
    val isGridMode by viewModel.gridMode.collectAsStateWithLifecycle()
    val isLoading by viewModel.feedLoading.collectAsStateWithLifecycle()
    val feedError by viewModel.feedError.collectAsStateWithLifecycle()
    val subscriberCount by viewModel.subscriberCount.collectAsStateWithLifecycle()
    val totalViews by viewModel.totalViews.collectAsStateWithLifecycle()
    val announcements by viewModel.announcementsState.collectAsStateWithLifecycle()
    val postsState by viewModel.communityPostsState.collectAsStateWithLifecycle()
    val pollsState by viewModel.pollsState.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val quizQuestions by viewModel.allQuizQuestions.collectAsStateWithLifecycle()
    val allResources by viewModel.allResources.collectAsStateWithLifecycle()
    val requests by viewModel.myVideoRequests.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.actionBannerMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBannerMessage()
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomTabRoutes = setOf(Routes.HOME, Routes.VIDEOS, Routes.PLAYLISTS, Routes.QUIZ, Routes.COMMUNITY)
    val showBottomBar = currentRoute in bottomTabRoutes

    val triggerSignIn: () -> Unit = {
        launchGoogleSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = scope,
            onAuthSuccess = {},
            onAuthCancelled = {},
            onAuthError = { err ->
                scope.launch { snackbarHostState.showSnackbar(err) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    val navItems = listOf(
                        Triple(Routes.HOME, AppStrings.navHome(lang), Icons.Rounded.Home to Icons.Outlined.Home),
                        Triple(Routes.VIDEOS, AppStrings.navVideos(lang), Icons.Rounded.PlayCircle to Icons.Outlined.PlayCircle),
                        Triple(Routes.PLAYLISTS, AppStrings.navPlaylists(lang), Icons.Rounded.PlaylistPlay to Icons.Outlined.PlaylistPlay),
                        Triple(Routes.QUIZ, AppStrings.navQuiz(lang), Icons.Rounded.Quiz to Icons.Outlined.Quiz),
                        Triple(Routes.COMMUNITY, AppStrings.navCommunity(lang), Icons.Rounded.Forum to Icons.Outlined.Forum)
                    )
                    navItems.forEach { (route, label, icons) ->
                        val selected = currentRoute == route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (selected) icons.first else icons.second, contentDescription = label) },
                            label = { Text(label, maxLines = 1) },
                            modifier = Modifier.testTag("nav_$route")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomeScreen(
                        lang = lang,
                        videos = videos,
                        history = history,
                        savedIds = savedIds,
                        selectedCategory = selectedCategory,
                        announcements = announcements,
                        isLoading = isLoading,
                        errorMessage = feedError,
                        dataSaver = prefs.dataSaver,
                        subscriberCount = subscriberCount,
                        onToggleLanguage = {
                            viewModel.setLanguage(if (lang == AppLanguage.BANGLA) "en" else "bn")
                        },
                        onSelectCategory = viewModel::selectCategory,
                        onOpenVideo = { id -> navController.navigate(Routes.videoPlayer(id)) },
                        onToggleSave = viewModel::toggleSaveVideo,
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                        onOpenProfile = { navController.navigate(Routes.PROFILE) },
                        onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                        onOpenResources = { navController.navigate(Routes.RESOURCES) },
                        onRetry = viewModel::refreshChannelFeed
                    )
                }

                composable(Routes.VIDEOS) {
                    VideosScreen(
                        lang = lang,
                        videos = filteredVideos,
                        totalAvailableCount = videos.size,
                        savedIds = savedIds,
                        selectedCategory = selectedCategory,
                        sortOption = sortOption,
                        durationFilter = durationFilter,
                        isGridMode = isGridMode,
                        isLoading = isLoading,
                        dataSaver = prefs.dataSaver,
                        onSelectCategory = viewModel::selectCategory,
                        onSelectSort = viewModel::setSortOption,
                        onSelectDuration = viewModel::setDurationFilter,
                        onToggleGridMode = viewModel::toggleGridMode,
                        onLoadMore = viewModel::loadMoreVideos,
                        onOpenVideo = { id -> navController.navigate(Routes.videoPlayer(id)) },
                        onToggleSave = viewModel::toggleSaveVideo
                    )
                }

                composable(Routes.PLAYLISTS) {
                    PlaylistsScreen(
                        lang = lang,
                        playlists = playlists,
                        history = history,
                        onOpenPlaylist = { id -> navController.navigate(Routes.playlistDetail(id)) }
                    )
                }

                composable(
                    route = Routes.PLAYLIST_DETAIL,
                    arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                ) { backStack ->
                    val plId = backStack.arguments?.getString("playlistId").orEmpty()
                    val playlist = playlists.firstOrNull { it.id == plId } ?: playlists.first()
                    PlaylistDetailScreen(
                        lang = lang,
                        playlist = playlist,
                        allVideos = videos,
                        history = history,
                        onBack = { navController.popBackStack() },
                        onOpenVideo = { id -> navController.navigate(Routes.videoPlayer(id)) }
                    )
                }

                composable(
                    route = Routes.VIDEO_PLAYER,
                    arguments = listOf(navArgument("videoId") { type = NavType.StringType })
                ) { backStack ->
                    val vidId = backStack.arguments?.getString("videoId").orEmpty()
                    val video = videos.firstOrNull { it.id == vidId } ?: videos.first()
                    VideoPlayerScreen(
                        lang = lang,
                        video = video,
                        allVideos = videos,
                        resources = allResources,
                        isLiked = likedIds.contains(video.id),
                        isSaved = savedIds.contains(video.id),
                        dataSaver = prefs.dataSaver,
                        onBack = { navController.popBackStack() },
                        onRecordWatch = { v -> viewModel.recordVideoWatch(v) },
                        onToggleLike = viewModel::toggleLikeVideo,
                        onToggleSave = viewModel::toggleSaveVideo,
                        onAddNote = viewModel::saveNote,
                        onSaveResourceOffline = viewModel::saveResourceOffline,
                        onOpenVideo = { id -> navController.navigate(Routes.videoPlayer(id)) }
                    )
                }

                composable(Routes.QUIZ) {
                    QuizScreen(
                        lang = lang,
                        questions = quizQuestions,
                        xpPoints = prefs.localXp,
                        streakDays = prefs.localStreak,
                        answeredQuizIds = prefs.answeredQuizIds,
                        leaderboard = leaderboard,
                        onAnswerQuestion = viewModel::answerQuiz,
                        onDailyCheckIn = viewModel::dailyCheckIn
                    )
                }

                composable(Routes.COMMUNITY) {
                    CommunityScreen(
                        lang = lang,
                        isSignedIn = viewModel.isSignedIn,
                        currentUserId = viewModel.currentUserId,
                        isAdmin = viewModel.isAdminUser,
                        postsState = postsState,
                        pollsState = pollsState,
                        observeReplies = viewModel::observePostReplies,
                        onRequireSignIn = triggerSignIn,
                        onCreatePost = viewModel::createCommunityPost,
                        onLikePost = viewModel::likePost,
                        onReportPost = viewModel::reportPost,
                        onDeletePost = viewModel::deletePost,
                        onAddReply = viewModel::addReply,
                        onVotePoll = viewModel::votePoll,
                        onSubmitJoin = viewModel::submitCommunityJoin,
                        onSubmitVideoRequest = { topic, details, cb ->
                            viewModel.submitVideoRequestOrFeedback("video_request", topic, details, cb)
                        }
                    )
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        lang = lang,
                        allVideos = videos,
                        savedIds = savedIds,
                        recentSearches = recentSearches,
                        dataSaver = prefs.dataSaver,
                        onBack = { navController.popBackStack() },
                        onSearchSubmitted = viewModel::addRecentSearch,
                        onClearRecent = viewModel::clearRecentSearches,
                        onOpenVideo = { id -> navController.navigate(Routes.videoPlayer(id)) },
                        onToggleSave = viewModel::toggleSaveVideo
                    )
                }

                composable(Routes.LIBRARY) {
                    LibraryScreen(
                        lang = lang,
                        allVideos = videos,
                        savedIds = savedIds,
                        history = history,
                        notes = notes,
                        offlineResources = offlineResources,
                        dataSaver = prefs.dataSaver,
                        onBack = { navController.popBackStack() },
                        onOpenVideo = { id -> navController.navigate(Routes.videoPlayer(id)) },
                        onToggleSave = viewModel::toggleSaveVideo,
                        onDeleteNote = viewModel::deleteNote,
                        onRemoveOfflineResource = viewModel::removeOfflineResource
                    )
                }

                composable(Routes.RESOURCES) {
                    ResourcesAndToolsScreen(
                        lang = lang,
                        videos = videos,
                        resources = allResources,
                        onBack = { navController.popBackStack() },
                        onSaveOffline = viewModel::saveResourceOffline
                    )
                }

                composable(Routes.NOTIFICATIONS) {
                    NotificationsScreen(
                        lang = lang,
                        prefs = prefs,
                        notifications = notifications,
                        onBack = { navController.popBackStack() },
                        onTogglePreference = viewModel::updateNotificationSetting,
                        onOpenVideo = { id -> navController.navigate(Routes.videoPlayer(id)) }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileAndSettingsScreen(
                        lang = lang,
                        isSignedIn = viewModel.isSignedIn,
                        isAdmin = viewModel.isAdminUser,
                        displayName = currentUserDisplayName,
                        email = currentUserEmail,
                        prefs = prefs,
                        watchedVideosCount = history.size,
                        xpPoints = prefs.localXp,
                        streakDays = prefs.localStreak,
                        onBack = { navController.popBackStack() },
                        onSetLanguage = viewModel::setLanguage,
                        onSetThemeMode = viewModel::setThemeMode,
                        onSetFontSize = viewModel::setFontSize,
                        onSetDataSaver = viewModel::setDataSaver,
                        onSetFavoriteCategories = viewModel::setFavoriteCategories,
                        onClearCache = viewModel::clearLocalCache,
                        onToggleBell = viewModel::toggleBellSubscription,
                        onSubmitFeedback = { topic, details, cb ->
                            viewModel.submitVideoRequestOrFeedback("feedback", topic, details, cb)
                        },
                        onOpenAdminPanel = { navController.navigate(Routes.ADMIN) }
                    )
                }

                composable(Routes.ADMIN) {
                    AdminPanelScreen(
                        lang = lang,
                        isAdminUser = viewModel.isAdminUser,
                        videos = videos,
                        subscriberCount = subscriberCount,
                        totalViews = totalViews,
                        requests = requests,
                        onBack = { navController.popBackStack() },
                        onPublishAnnouncement = viewModel::adminPublishAnnouncement,
                        onCreatePoll = viewModel::adminCreatePoll,
                        onCreateQuiz = viewModel::adminCreateQuiz,
                        onAddResource = viewModel::adminAddResource
                    )
                }
            }
        }
    }
}
