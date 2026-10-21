package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DramaRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MainNavigationTab(val label: String) {
    HOME("Beranda"),
    WATCH("Nonton"),
    NOTIFICATIONS("Pemberitahuan"),
    PROFILE("Koleksi")
}

data class PlayerUiState(
    val currentDrama: Drama? = null,
    val currentEpisode: Episode? = null,
    val isPlaying: Boolean = true,
    val currentSecond: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isMuted: Boolean = false,
    val currentSubtitle: SubtitleItem? = null,
    val showEpisodeDrawer: Boolean = false,
    val showCommentsDrawer: Boolean = false,
    val showSubtitleSettings: Boolean = false
)

class DramaViewModel(application: Application) : AndroidViewModel(application) {

    val repository = DramaRepository(application.applicationContext)

    val dramas: StateFlow<List<Drama>> = repository.dramas
    val subscribedDramaIds: StateFlow<Set<String>> = repository.subscribedDramaIds
    val bookmarkedDramaIds: StateFlow<Set<String>> = repository.bookmarkedDramaIds
    val likedEpisodeIds: StateFlow<Set<String>> = repository.likedEpisodeIds
    val watchHistory: StateFlow<Map<String, Int>> = repository.watchHistory
    val notifications: StateFlow<List<DramaNotification>> = repository.notifications
    val comments: StateFlow<Map<String, List<DramaComment>>> = repository.comments
    val subtitleConfig: StateFlow<SubtitleConfig> = repository.subtitleConfig

    // Navigation state
    private val _currentTab = MutableStateFlow(MainNavigationTab.HOME)
    val currentTab: StateFlow<MainNavigationTab> = _currentTab.asStateFlow()

    private val _selectedDramaForDetail = MutableStateFlow<Drama?>(null)
    val selectedDramaForDetail: StateFlow<Drama?> = _selectedDramaForDetail.asStateFlow()

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Semua")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _selectedCountry = MutableStateFlow("Semua")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _isWebsiteViewMode = MutableStateFlow(true)
    val isWebsiteViewMode: StateFlow<Boolean> = _isWebsiteViewMode.asStateFlow()

    fun toggleWebsiteViewMode() {
        _isWebsiteViewMode.value = !_isWebsiteViewMode.value
    }

    fun setWebsiteViewMode(enabled: Boolean) {
        _isWebsiteViewMode.value = enabled
    }

    fun selectCountry(country: String) {
        _selectedCountry.value = country
    }

    // Video Player state
    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    private var playbackJob: Job? = null

    val unreadNotificationsCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val filteredDramas: StateFlow<List<Drama>> = combine(
        dramas,
        _searchQuery,
        _selectedGenre,
        _selectedCountry
    ) { allDramas, query, genre, country ->
        allDramas.filter { drama ->
            val q = query.trim()
            val matchesQuery = q.isEmpty() ||
                drama.title.contains(q, ignoreCase = true) ||
                drama.titleAlt.contains(q, ignoreCase = true) ||
                drama.genre.contains(q, ignoreCase = true) ||
                drama.country.contains(q, ignoreCase = true) ||
                drama.tags.any { it.contains(q, ignoreCase = true) } ||
                drama.synopsis.contains(q, ignoreCase = true) ||
                drama.cast.any { it.contains(q, ignoreCase = true) }

            val matchesGenre = when (genre) {
                "Semua", "All" -> true
                "Series" -> drama.isSeries
                "Film Pendek", "Short Movie" -> !drama.isSeries
                "Episode Baru", "New Episodes" -> drama.hasNewEpisode
                "Romance", "Romantis" -> drama.genre.contains("Romance", true) || drama.genre.contains("Romantis", true) || drama.tags.any { it.contains("Romance", true) || it.contains("Romantis", true) }
                "Action", "Aksi" -> drama.genre.contains("Action", true) || drama.genre.contains("Aksi", true) || drama.tags.any { it.contains("Action", true) || it.contains("Aksi", true) }
                "Comedy", "Komedi" -> drama.genre.contains("Comedy", true) || drama.genre.contains("Komedi", true) || drama.tags.any { it.contains("Comedy", true) || it.contains("Komedi", true) }
                "Billionaire", "Miliarder" -> drama.genre.contains("Miliarder", true) || drama.genre.contains("Billionaire", true) || drama.tags.any { it.contains("Miliarder", true) || it.contains("Billionaire", true) }
                "Revenge", "Balas Dendam" -> drama.genre.contains("Balas Dendam", true) || drama.genre.contains("Revenge", true) || drama.tags.any { it.contains("Balas Dendam", true) || it.contains("Revenge", true) }
                "Thriller", "Misteri" -> drama.genre.contains("Thriller", true) || drama.genre.contains("Misteri", true) || drama.tags.any { it.contains("Thriller", true) || it.contains("Misteri", true) }
                else -> drama.genre.equals(genre, ignoreCase = true) || drama.tags.any { it.equals(genre, ignoreCase = true) }
            }

            val matchesCountry = when (country) {
                "Semua", "All" -> true
                "Korea" -> drama.country.contains("Korea", ignoreCase = true)
                "Amerika" -> drama.country.contains("Amerika", ignoreCase = true) || drama.country.contains("USA", ignoreCase = true)
                else -> drama.country.equals(country, ignoreCase = true)
            }

            matchesQuery && matchesGenre && matchesCountry
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Initialize player with top trending drama
        viewModelScope.launch {
            dramas.first { it.isNotEmpty() }.let { list ->
                val topDrama = list.first()
                val topEpisode = topDrama.episodes.find { it.episodeNumber == 19 } ?: topDrama.episodes.first()
                setPlayerDramaAndEpisode(topDrama, topEpisode, autoPlay = false)
            }
        }
    }

    fun selectTab(tab: MainNavigationTab) {
        _currentTab.value = tab
        if (tab == MainNavigationTab.WATCH) {
            play()
        }
    }

    fun openDramaDetail(drama: Drama) {
        _selectedDramaForDetail.value = drama
    }

    fun closeDramaDetail() {
        _selectedDramaForDetail.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGenre(genre: String) {
        _selectedGenre.value = genre
    }

    fun playDramaEpisode(drama: Drama, episode: Episode) {
        setPlayerDramaAndEpisode(drama, episode, autoPlay = true)
        _selectedDramaForDetail.value = null
        _currentTab.value = MainNavigationTab.WATCH
    }

    fun setPlayerDramaAndEpisode(drama: Drama, episode: Episode, autoPlay: Boolean = true) {
        playbackJob?.cancel()
        _playerState.update {
            it.copy(
                currentDrama = drama,
                currentEpisode = episode,
                currentSecond = 0,
                isPlaying = autoPlay,
                currentSubtitle = episode.subtitles.firstOrNull()
            )
        }
        repository.updateWatchProgress(drama.id, episode.episodeNumber)
        if (autoPlay) {
            startPlaybackTicker()
        }
    }

    fun togglePlayPause() {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        _playerState.update { it.copy(isPlaying = true) }
        startPlaybackTicker()
    }

    fun pause() {
        _playerState.update { it.copy(isPlaying = false) }
        playbackJob?.cancel()
    }

    fun seekTo(second: Int) {
        val ep = _playerState.value.currentEpisode ?: return
        val clamped = second.coerceIn(0, ep.durationSeconds)
        _playerState.update { it.copy(currentSecond = clamped) }
        updateSubtitleForCurrentTime(clamped)
    }

    fun skip10Seconds(forward: Boolean) {
        val current = _playerState.value.currentSecond
        val delta = if (forward) 10 else -10
        seekTo(current + delta)
    }

    fun cyclePlaybackSpeed() {
        val currentSpeed = _playerState.value.playbackSpeed
        val nextSpeed = when (currentSpeed) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        _playerState.update { it.copy(playbackSpeed = nextSpeed) }
    }

    fun toggleMute() {
        _playerState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleEpisodeDrawer(show: Boolean? = null) {
        _playerState.update { it.copy(showEpisodeDrawer = show ?: !it.showEpisodeDrawer) }
    }

    fun toggleCommentsDrawer(show: Boolean? = null) {
        _playerState.update { it.copy(showCommentsDrawer = show ?: !it.showCommentsDrawer) }
    }

    fun toggleSubtitleSettings(show: Boolean? = null) {
        _playerState.update { it.copy(showSubtitleSettings = show ?: !it.showSubtitleSettings) }
    }

    fun nextEpisode() {
        val drama = _playerState.value.currentDrama ?: return
        val currentEpNum = _playerState.value.currentEpisode?.episodeNumber ?: 1
        val nextEp = drama.episodes.find { it.episodeNumber == currentEpNum + 1 }
        if (nextEp != null) {
            setPlayerDramaAndEpisode(drama, nextEp, autoPlay = true)
        } else {
            // Loop or next drama
            val currentIndex = dramas.value.indexOfFirst { it.id == drama.id }
            val nextDrama = dramas.value.getOrNull(currentIndex + 1) ?: dramas.value.firstOrNull()
            if (nextDrama != null && nextDrama.episodes.isNotEmpty()) {
                setPlayerDramaAndEpisode(nextDrama, nextDrama.episodes.first(), autoPlay = true)
            }
        }
    }

    fun previousEpisode() {
        val drama = _playerState.value.currentDrama ?: return
        val currentEpNum = _playerState.value.currentEpisode?.episodeNumber ?: 1
        val prevEp = drama.episodes.find { it.episodeNumber == currentEpNum - 1 }
        if (prevEp != null) {
            setPlayerDramaAndEpisode(drama, prevEp, autoPlay = true)
        }
    }

    private fun startPlaybackTicker() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive && _playerState.value.isPlaying) {
                val delayTime = (1000 / _playerState.value.playbackSpeed).toLong()
                delay(delayTime)
                val curState = _playerState.value
                val curEp = curState.currentEpisode ?: break
                val nextSec = curState.currentSecond + 1

                if (nextSec >= curEp.durationSeconds) {
                    // Auto play next episode
                    nextEpisode()
                    break
                } else {
                    _playerState.update { it.copy(currentSecond = nextSec) }
                    updateSubtitleForCurrentTime(nextSec)
                }
            }
        }
    }

    private fun updateSubtitleForCurrentTime(second: Int) {
        val subtitles = _playerState.value.currentEpisode?.subtitles ?: emptyList()
        val matching = subtitles.find { second in it.startSecond..it.endSecond }
        _playerState.update { it.copy(currentSubtitle = matching) }
    }

    // Interactive repository actions
    fun toggleSubscription(dramaId: String) = repository.toggleSubscription(dramaId)
    fun toggleBookmark(dramaId: String) = repository.toggleBookmark(dramaId)
    fun toggleLikeEpisode(episodeId: String) = repository.toggleLikeEpisode(episodeId)
    fun markNotificationAsRead(id: String) = repository.markNotificationAsRead(id)
    fun markAllNotificationsAsRead() = repository.markAllNotificationsAsRead()
    fun addComment(dramaId: String, episodeNumber: Int, text: String) =
        repository.addComment(dramaId, episodeNumber, text)

    fun updateSubtitleConfig(language: SubtitleLanguage? = null, fontSizeSp: Int? = null, opacity: Float? = null) =
        repository.updateSubtitleConfig(language, fontSizeSp, opacity)

    fun simulateNewEpisode(dramaId: String) =
        repository.triggerSimulateNewEpisodeRelease(dramaId)
}
