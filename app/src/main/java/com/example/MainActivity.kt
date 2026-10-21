package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DramaViewModel
import com.example.ui.MainNavigationTab
import com.example.ui.components.DramakuTopBar
import com.example.ui.components.WebsiteTopNavBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private val viewModel: DramaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Android Notification Channel
        NotificationHelper.createNotificationChannel(this)

        // Handle intent from Android push notification click
        handleNotificationIntent(intent)

        setContent {
            MyApplicationTheme {
                DramakuApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val dramaId = intent?.getStringExtra("EXTRA_NAVIGATE_DRAMA_ID") ?: return
        val epNum = intent.getIntExtra("EXTRA_NAVIGATE_EPISODE", 1)

        val drama = viewModel.dramas.value.find { it.id == dramaId }
        if (drama != null) {
            val episode = drama.episodes.find { it.episodeNumber == epNum } ?: drama.episodes.first()
            viewModel.playDramaEpisode(drama, episode)
        }
    }
}

@Composable
fun DramakuApp(viewModel: DramaViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedDramaForDetail by viewModel.selectedDramaForDetail.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    val dramas by viewModel.filteredDramas.collectAsStateWithLifecycle()
    val allDramas by viewModel.dramas.collectAsStateWithLifecycle()
    val subscribedIds by viewModel.subscribedDramaIds.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedDramaIds.collectAsStateWithLifecycle()
    val likedEpisodeIds by viewModel.likedEpisodeIds.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val subtitleConfig by viewModel.subtitleConfig.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val isWebsiteMode by viewModel.isWebsiteViewMode.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()

    val currentWebUrl = when {
        selectedDramaForDetail != null -> "https://dramaku.id/drama/${selectedDramaForDetail?.id}"
        currentTab == MainNavigationTab.WATCH -> "https://dramaku.id/watch?v=${playerState.currentDrama?.id ?: "stream"}&ep=${playerState.currentEpisode?.episodeNumber ?: 1}"
        currentTab == MainNavigationTab.NOTIFICATIONS -> "https://dramaku.id/notifications"
        currentTab == MainNavigationTab.PROFILE -> "https://dramaku.id/koleksi"
        searchQuery.isNotBlank() -> "https://dramaku.id/search?q=${searchQuery.trim().replace(" ", "+")}"
        selectedCountry != "Semua" -> "https://dramaku.id/country/${selectedCountry.lowercase()}"
        selectedGenre != "Semua" -> "https://dramaku.id/genre/${selectedGenre.lowercase()}"
        else -> "https://dramaku.id/beranda"
    }

    // Handle system back navigation
    BackHandler(enabled = selectedDramaForDetail != null) {
        viewModel.closeDramaDetail()
    }

    BackHandler(enabled = selectedDramaForDetail == null && currentTab != MainNavigationTab.HOME) {
        viewModel.selectTab(MainNavigationTab.HOME)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack),
        containerColor = CinemaBlack,
        topBar = {
            if (isWebsiteMode) {
                WebsiteTopNavBar(
                    currentTab = currentTab,
                    unreadCount = unreadCount,
                    searchQuery = searchQuery,
                    selectedGenre = selectedGenre,
                    selectedCountry = selectedCountry,
                    isWebsiteMode = isWebsiteMode,
                    currentUrl = currentWebUrl,
                    onTabSelect = { tab ->
                        viewModel.closeDramaDetail()
                        viewModel.selectTab(tab)
                    },
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onGenreSelect = viewModel::selectGenre,
                    onCountrySelect = viewModel::selectCountry,
                    onToggleViewMode = viewModel::toggleWebsiteViewMode
                )
            } else if (selectedDramaForDetail == null && currentTab == MainNavigationTab.HOME) {
                DramakuTopBar(
                    unreadCount = unreadCount,
                    searchQuery = searchQuery,
                    selectedGenre = selectedGenre,
                    selectedCountry = selectedCountry,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onGenreSelect = viewModel::selectGenre,
                    onCountrySelect = viewModel::selectCountry,
                    onNotificationClick = {
                        viewModel.selectTab(MainNavigationTab.NOTIFICATIONS)
                    },
                    onToggleViewMode = viewModel::toggleWebsiteViewMode
                )
            }
        },
        bottomBar = {
            // Show bottom navigation on mobile app mode; on website mode, navigation is on the top navbar
            if (selectedDramaForDetail == null && !isWebsiteMode) {
                NavigationBar(
                    containerColor = CinemaDarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav_bar")
                ) {
                    // Tab 1: Beranda
                    NavigationBarItem(
                        selected = currentTab == MainNavigationTab.HOME,
                        onClick = { viewModel.selectTab(MainNavigationTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainNavigationTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Beranda"
                            )
                        },
                        label = {
                            Text(
                                text = "Beranda",
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == MainNavigationTab.HOME) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // Tab 2: Nonton (Reels Vertical Video Player)
                    NavigationBarItem(
                        selected = currentTab == MainNavigationTab.WATCH,
                        onClick = { viewModel.selectTab(MainNavigationTab.WATCH) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainNavigationTab.WATCH) Icons.Filled.PlayCircleFilled else Icons.Outlined.PlayCircleOutline,
                                contentDescription = "Nonton"
                            )
                        },
                        label = {
                            Text(
                                text = "Nonton",
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == MainNavigationTab.WATCH) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_watch")
                    )

                    // Tab 3: Pemberitahuan (Notifications Center with Badge)
                    NavigationBarItem(
                        selected = currentTab == MainNavigationTab.NOTIFICATIONS,
                        onClick = { viewModel.selectTab(MainNavigationTab.NOTIFICATIONS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = LiveRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainNavigationTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Pemberitahuan"
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Notifikasi",
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == MainNavigationTab.NOTIFICATIONS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_notifications")
                    )

                    // Tab 4: Koleksi (Library / Watchlist / Subtitles)
                    NavigationBarItem(
                        selected = currentTab == MainNavigationTab.PROFILE,
                        onClick = { viewModel.selectTab(MainNavigationTab.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainNavigationTab.PROFILE) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                contentDescription = "Koleksi"
                            )
                        },
                        label = {
                            Text(
                                text = "Koleksi",
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == MainNavigationTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val detailDrama = selectedDramaForDetail
            if (detailDrama != null) {
                DramaDetailScreen(
                    drama = detailDrama,
                    isSubscribed = subscribedIds.contains(detailDrama.id),
                    isBookmarked = bookmarkedIds.contains(detailDrama.id),
                    currentWatchEpisode = watchHistory[detailDrama.id],
                    isWebsiteMode = isWebsiteMode,
                    onBack = viewModel::closeDramaDetail,
                    onPlayEpisode = viewModel::playDramaEpisode,
                    onToggleSubscription = { viewModel.toggleSubscription(detailDrama.id) },
                    onToggleBookmark = { viewModel.toggleBookmark(detailDrama.id) },
                    onSimulateNewEpisode = { viewModel.simulateNewEpisode(detailDrama.id) }
                )
            } else {
                when (currentTab) {
                    MainNavigationTab.HOME -> {
                        HomeScreen(
                            dramas = dramas,
                            allDramas = allDramas,
                            subscribedIds = subscribedIds,
                            selectedGenre = selectedGenre,
                            selectedCountry = selectedCountry,
                            searchQuery = searchQuery,
                            isWebsiteMode = isWebsiteMode,
                            onGenreSelect = viewModel::selectGenre,
                            onCountrySelect = viewModel::selectCountry,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onClearSearch = { viewModel.setSearchQuery("") },
                            onDramaClick = viewModel::openDramaDetail,
                            onPlayClick = viewModel::playDramaEpisode,
                            onToggleSubscription = viewModel::toggleSubscription,
                            onSimulateNotification = viewModel::simulateNewEpisode
                        )
                    }

                    MainNavigationTab.WATCH -> {
                        WatchPlayerScreen(
                            playerState = playerState,
                            subscribedIds = subscribedIds,
                            bookmarkedIds = bookmarkedIds,
                            likedEpisodeIds = likedEpisodeIds,
                            commentsMap = comments,
                            subtitleConfig = subtitleConfig,
                            isWebsiteMode = isWebsiteMode,
                            onTogglePlayPause = viewModel::togglePlayPause,
                            onSeek = viewModel::seekTo,
                            onSkip10 = viewModel::skip10Seconds,
                            onCycleSpeed = viewModel::cyclePlaybackSpeed,
                            onToggleMute = viewModel::toggleMute,
                            onNextEpisode = viewModel::nextEpisode,
                            onPrevEpisode = viewModel::previousEpisode,
                            onSelectEpisode = { ep ->
                                val drama = playerState.currentDrama
                                if (drama != null) {
                                    viewModel.setPlayerDramaAndEpisode(drama, ep, autoPlay = true)
                                }
                            },
                            onToggleSubscription = viewModel::toggleSubscription,
                            onToggleBookmark = viewModel::toggleBookmark,
                            onToggleLike = viewModel::toggleLikeEpisode,
                            onAddComment = viewModel::addComment,
                            onUpdateSubtitleConfig = viewModel::updateSubtitleConfig,
                            onToggleEpisodeDrawer = viewModel::toggleEpisodeDrawer,
                            onToggleCommentsDrawer = viewModel::toggleCommentsDrawer,
                            onToggleSubtitleSettings = viewModel::toggleSubtitleSettings
                        )
                    }

                    MainNavigationTab.NOTIFICATIONS -> {
                        NotificationsScreen(
                            notifications = notifications,
                            allDramas = allDramas,
                            subscribedIds = subscribedIds,
                            onNotificationClick = { notif ->
                                viewModel.markNotificationAsRead(notif.id)
                                val drama = allDramas.find { it.id == notif.dramaId }
                                if (drama != null) {
                                    val epNum = notif.episodeNumber ?: drama.releasedEpisodes
                                    val episode = drama.episodes.find { it.episodeNumber == epNum } ?: drama.episodes.first()
                                    viewModel.playDramaEpisode(drama, episode)
                                }
                            },
                            onMarkAllRead = viewModel::markAllNotificationsAsRead,
                            onSimulateNewEpisode = viewModel::simulateNewEpisode
                        )
                    }

                    MainNavigationTab.PROFILE -> {
                        LibraryScreen(
                            allDramas = allDramas,
                            bookmarkedIds = bookmarkedIds,
                            subscribedIds = subscribedIds,
                            watchHistory = watchHistory,
                            subtitleConfig = subtitleConfig,
                            onDramaClick = viewModel::openDramaDetail,
                            onPlayClick = viewModel::playDramaEpisode,
                            onToggleBookmark = viewModel::toggleBookmark,
                            onToggleSubscription = viewModel::toggleSubscription,
                            onUpdateSubtitleConfig = viewModel::updateSubtitleConfig
                        )
                    }
                }
            }
        }
    }
}
