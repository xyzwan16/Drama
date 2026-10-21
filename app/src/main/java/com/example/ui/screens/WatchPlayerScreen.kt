package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.PlayerUiState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchPlayerScreen(
    playerState: PlayerUiState,
    subscribedIds: Set<String>,
    bookmarkedIds: Set<String>,
    likedEpisodeIds: Set<String>,
    commentsMap: Map<String, List<DramaComment>>,
    subtitleConfig: SubtitleConfig,
    onTogglePlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onSkip10: (Boolean) -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleMute: () -> Unit,
    onNextEpisode: () -> Unit,
    onPrevEpisode: () -> Unit,
    onSelectEpisode: (Episode) -> Unit,
    onToggleSubscription: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onAddComment: (String, Int, String) -> Unit,
    onUpdateSubtitleConfig: (SubtitleLanguage?, Int?, Float?) -> Unit,
    onToggleEpisodeDrawer: (Boolean?) -> Unit,
    onToggleCommentsDrawer: (Boolean?) -> Unit,
    onToggleSubtitleSettings: (Boolean?) -> Unit,
    isWebsiteMode: Boolean = false,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val drama = playerState.currentDrama
    val episode = playerState.currentEpisode
    val isSubscribed = drama != null && subscribedIds.contains(drama.id)
    val isBookmarked = drama != null && bookmarkedIds.contains(drama.id)
    val episodeKey = episode?.id ?: ""
    val isLiked = likedEpisodeIds.contains(episodeKey)

    var showHeartBurst by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (showHeartBurst) 1.5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { showHeartBurst = false },
        label = "heart_burst"
    )

    // Animated glow pulse for cinema scene
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val commentListKey = "${drama?.id}_${episode?.episodeNumber}"
    val episodeComments = commentsMap[commentListKey] ?: emptyList()

    if (drama == null || episode == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CinemaBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CrimsonPrimary)
        }
        return
    }

    if (isWebsiteMode) {
        WebCinemaPlayerView(
            drama = drama,
            episode = episode,
            playerState = playerState,
            isSubscribed = isSubscribed,
            isBookmarked = isBookmarked,
            isLiked = isLiked,
            pulseAlpha = pulseAlpha,
            episodeComments = episodeComments,
            subtitleConfig = subtitleConfig,
            onTogglePlayPause = onTogglePlayPause,
            onSeek = onSeek,
            onSkip10 = onSkip10,
            onCycleSpeed = onCycleSpeed,
            onToggleMute = onToggleMute,
            onNextEpisode = onNextEpisode,
            onPrevEpisode = onPrevEpisode,
            onSelectEpisode = onSelectEpisode,
            onToggleSubscription = onToggleSubscription,
            onToggleBookmark = onToggleBookmark,
            onToggleLike = onToggleLike,
            onAddComment = onAddComment,
            onUpdateSubtitleConfig = onUpdateSubtitleConfig,
            onToggleSubtitleSettings = onToggleSubtitleSettings,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!isLiked) onToggleLike(episodeKey)
                        showHeartBurst = true
                    },
                    onTap = {
                        onTogglePlayPause()
                    }
                )
            }
            .testTag("watch_player_screen")
    ) {
        // Simulated Video Canvas Scene with Poster Background & Cinematic Atmospheric Shading
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = drama.posterRes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.15f)
            )

            // Dynamic Tint & Atmospheric Vignette
            val scenePrimary = Color(episode.sceneVisual.primaryColorHex)
            val sceneSecondary = Color(episode.sceneVisual.secondaryColorHex)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CinemaBlack.copy(alpha = 0.65f),
                                scenePrimary.copy(alpha = 0.45f * pulseAlpha),
                                CinemaBlack.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Center Scene Ambient Overlay (Simulating Video Motion / Lighting)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                sceneSecondary.copy(alpha = 0.15f * pulseAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Double tap bursting heart
            if (showHeartBurst) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier
                        .size(110.dp)
                        .scale(heartScale)
                        .align(Alignment.Center)
                )
            }
        }

        // Top Navigation Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column {
                    Text(
                        text = drama.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Episode ${episode.episodeNumber} dari ${drama.totalEpisodes}",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Top Quick Buttons: Subtitle switch & Quality
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Subtitle Quick Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onToggleSubtitleSettings(true) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = null,
                            tint = GoldYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = subtitleConfig.language.code,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // HD Quality Pill
                Surface(
                    color = CrimsonDark.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "1080P",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Center Play/Pause Indicator if paused
        if (!playerState.isPlaying) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Putar",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Right Side Action Column (TikTok/Reels style)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onToggleLike(episodeKey) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("player_like_button")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Suka",
                        tint = if (isLiked) CrimsonPrimary else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = if (isLiked) "14.3K" else "14.2K",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onToggleCommentsDrawer(true) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("player_comments_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Komentar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "${episodeComments.size + 128}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Watchlist / Bookmark Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onToggleBookmark(drama.id) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("player_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Simpan",
                        tint = if (isBookmarked) GoldYellow else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = if (isBookmarked) "Tersimpan" else "Simpan",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Lonceng Notifikasi Pembaruan Episode (CRITICAL USER FEATURE)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onToggleSubscription(drama.id) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isSubscribed) CrimsonPrimary else Color.Black.copy(alpha = 0.5f))
                        .testTag("player_notification_bell_button")
                ) {
                    Icon(
                        imageVector = if (isSubscribed) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = "Notifikasi Episode",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = if (isSubscribed) "Aktif" else "Notif",
                    color = if (isSubscribed) GoldYellow else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Speed cycle button
            IconButton(
                onClick = onCycleSpeed,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("player_speed_button")
            ) {
                Text(
                    text = "${playerState.playbackSpeed}x",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Episode List Trigger
            IconButton(
                onClick = { onToggleEpisodeDrawer(true) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CinemaCardSurfaceElevated)
                    .testTag("player_episode_list_trigger")
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Daftar Episode",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Controls, Subtitles & Info Container
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .padding(horizontal = 14.dp)
        ) {
            // SUBTITLE INDONESIA BAR (CRITICAL)
            if (subtitleConfig.language != SubtitleLanguage.OFF && playerState.currentSubtitle != null) {
                val sub = playerState.currentSubtitle
                val displayText = if (subtitleConfig.language == SubtitleLanguage.INDONESIAN) sub.textId else sub.textEn

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = subtitleConfig.backgroundOpacity))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("subtitle_display_bar")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${sub.speaker}: \"$displayText\"",
                            color = Color.White,
                            fontSize = subtitleConfig.fontSizeSp.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = (subtitleConfig.fontSizeSp + 6).sp
                        )

                        // Secondary subtitle line (Sub Indo badge)
                        Text(
                            text = "[ ${subtitleConfig.language.displayName} ]",
                            color = GoldYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Episode Title & Synopsis preview snippet
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CrimsonPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "EPISODE ${episode.episodeNumber}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = CinemaDarkSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(text = drama.countryFlag, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = drama.country,
                                color = GoldYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = episode.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = episode.previewSnippet,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Video Scrubber / Slider Bar
            val currentSec = playerState.currentSecond
            val totalSec = episode.durationSeconds
            val progressFraction = if (totalSec > 0) currentSec.toFloat() / totalSec.toFloat() else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSeconds(currentSec),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Slider(
                    value = progressFraction,
                    onValueChange = { frac ->
                        onSeek((frac * totalSec).toInt())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPrimary,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = BorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("player_progress_slider")
                )

                Text(
                    text = formatSeconds(totalSec),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom Transport Controls: Skip 10s, Prev Ep, Next Ep, Subtitle options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onSkip10(false) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Mundur 10 Detik",
                            tint = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onPrevEpisode,
                        enabled = episode.episodeNumber > 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Episode Sebelumnya",
                            tint = if (episode.episodeNumber > 1) TextPrimary else TextMuted
                        )
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CrimsonPrimary)
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Jeda" else "Putar",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onNextEpisode,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Episode Berikutnya",
                            tint = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { onSkip10(true) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Maju 10 Detik",
                            tint = TextPrimary
                        )
                    }
                }

                // Audio Mute toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (playerState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Suara",
                        tint = TextPrimary
                    )
                }
            }
        }
    }

    // Modal Sheet 1: Episode Selector Drawer
    if (playerState.showEpisodeDrawer) {
        ModalBottomSheet(
            onDismissRequest = { onToggleEpisodeDrawer(false) },
            containerColor = CinemaDarkSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pilih Episode Drama",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${drama.title} • Total ${drama.totalEpisodes} Episode",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    IconButton(onClick = { onToggleEpisodeDrawer(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 340.dp)
                ) {
                    items(drama.episodes) { ep ->
                        val isCurrent = ep.episodeNumber == episode.episodeNumber
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isCurrent -> CrimsonPrimary
                                        ep.isNew -> CrimsonDark.copy(alpha = 0.5f)
                                        else -> CinemaCardSurface
                                    }
                                )
                                .clickable {
                                    onSelectEpisode(ep)
                                    onToggleEpisodeDrawer(false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${ep.episodeNumber}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (ep.isNew) {
                                    Text(
                                        text = "BARU",
                                        color = GoldYellow,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                } else if (!ep.isFree) {
                                    Text(
                                        text = "VIP",
                                        color = GoldAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet 2: Comments Drawer
    if (playerState.showCommentsDrawer) {
        var newCommentText by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { onToggleCommentsDrawer(false) },
            containerColor = CinemaDarkSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Komentar Episode ${episode.episodeNumber} (${episodeComments.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onToggleCommentsDrawer(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(episodeComments) { comment ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.avatarInitials,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = comment.userName,
                                        color = GoldAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = comment.timeAgo,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = comment.text,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Comment Input Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Tulis tanggapan untuk episode ini...", color = TextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CinemaCardSurface,
                            unfocusedContainerColor = CinemaCardSurface,
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(drama.id, episode.episodeNumber, newCommentText.trim())
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CrimsonPrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
                    }
                }
            }
        }
    }

    // Modal Sheet 3: Subtitle Settings Modal
    if (playerState.showSubtitleSettings) {
        ModalBottomSheet(
            onDismissRequest = { onToggleSubtitleSettings(false) },
            containerColor = CinemaDarkSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengaturan Subtitle (Teks)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onToggleSubtitleSettings(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bahasa Terjemahan",
                    color = GoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                SubtitleLanguage.values().forEach { lang ->
                    val isSelected = subtitleConfig.language == lang
                    Surface(
                        color = if (isSelected) CrimsonPrimary.copy(alpha = 0.2f) else CinemaCardSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onUpdateSubtitleConfig(lang, null, null)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lang.displayName,
                                color = if (isSelected) CrimsonLight else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CrimsonPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ukuran Huruf Subtitle (${subtitleConfig.fontSizeSp}sp)",
                    color = GoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(14 to "Kecil", 16 to "Normal", 18 to "Besar", 22 to "Ekstra").forEach { (size, label) ->
                        val isSelected = subtitleConfig.fontSizeSp == size
                        Button(
                            onClick = { onUpdateSubtitleConfig(null, size, null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) CrimsonPrimary else CinemaCardSurface,
                                contentColor = if (isSelected) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}

@Composable
fun WebCinemaPlayerView(
    drama: Drama,
    episode: Episode,
    playerState: PlayerUiState,
    isSubscribed: Boolean,
    isBookmarked: Boolean,
    isLiked: Boolean,
    pulseAlpha: Float,
    episodeComments: List<DramaComment>,
    subtitleConfig: SubtitleConfig,
    onTogglePlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onSkip10: (Boolean) -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleMute: () -> Unit,
    onNextEpisode: () -> Unit,
    onPrevEpisode: () -> Unit,
    onSelectEpisode: (Episode) -> Unit,
    onToggleSubscription: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onAddComment: (String, Int, String) -> Unit,
    onUpdateSubtitleConfig: (SubtitleLanguage?, Int?, Float?) -> Unit,
    onToggleSubtitleSettings: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    var newCommentText by remember { mutableStateOf("") }
    val episodeKey = episode.id

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBlack),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // 1. Web Breadcrumbs Bar
        item {
            Surface(
                color = CinemaDarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏠 Beranda",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(text = " › ", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "🎬 Streaming Web",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(text = " › ", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "${drama.countryFlag} ${drama.country}",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = " › ", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "${drama.title} (Ep ${episode.episodeNumber})",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 2. Cinema Theater Video Player Frame
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .height(260.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .testTag("web_cinema_theater_frame")
            ) {
                // Video Poster Backdrop with ambient gradient
                AsyncImage(
                    model = drama.posterRes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.1f)
                )

                // Atmospheric cinematic shading
                val scenePrimary = Color(episode.sceneVisual.primaryColorHex)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CinemaBlack.copy(alpha = 0.5f),
                                    scenePrimary.copy(alpha = 0.35f * pulseAlpha),
                                    CinemaBlack.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Big Center Play button when paused
                if (!playerState.isPlaying) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .align(Alignment.Center)
                            .testTag("web_player_center_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Putar Video",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Subtitle Display Box (CRITICAL - Prominent on web player)
                if (subtitleConfig.language != SubtitleLanguage.OFF && playerState.currentSubtitle != null) {
                    val sub = playerState.currentSubtitle
                    val displayText = if (subtitleConfig.language == SubtitleLanguage.INDONESIAN) sub.textId else sub.textEn

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 54.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = subtitleConfig.backgroundOpacity))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("web_player_subtitle_box")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${sub.speaker}: \"$displayText\"",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                            Text(
                                text = "[ ${subtitleConfig.language.displayName} ]",
                                color = GoldYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    }
                }

                // Top theater badges (Country, Resolution, Sub Indo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = CrimsonPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "EPISODE ${episode.episodeNumber}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = CinemaDarkSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = drama.countryFlag, fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = drama.country,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = CinemaDarkSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "1080P HD",
                                color = GoldYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Bottom Web Video Controls Bar
                Surface(
                    color = Color.Black.copy(alpha = 0.88f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                        // Slider Progress
                        Slider(
                            value = playerState.currentSecond.toFloat(),
                            onValueChange = { onSeek(it.toInt()) },
                            valueRange = 0f..episode.durationSeconds.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = CrimsonPrimary,
                                activeTrackColor = CrimsonPrimary,
                                inactiveTrackColor = BorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                        )

                        // Bottom Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onTogglePlayPause,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Putar atau Jeda",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onSkip10(false) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "Mundur 10 detik",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onSkip10(true) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "Maju 10 detik",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "${formatSeconds(playerState.currentSecond)} / ${formatSeconds(episode.durationSeconds)}",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Subtitle toggle button
                                TextButton(
                                    onClick = { onToggleSubtitleSettings(true) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(subtitleConfig.language.code, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Playback speed button
                                TextButton(
                                    onClick = onCycleSpeed,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("${playerState.playbackSpeed}x", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Mute button
                                IconButton(
                                    onClick = onToggleMute,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (playerState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Suara",
                                        tint = if (playerState.isMuted) CrimsonLight else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Web Drama Meta Header & Quick Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = drama.title,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${drama.titleAlt} • ${drama.countryFlag} ${drama.country} • ⭐ ${drama.rating} (${drama.viewsCount}x diputar)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Prev / Next Episode Web Nav
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = onPrevEpisode,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("‹ Prev", fontSize = 10.sp)
                        }
                        Button(
                            onClick = onNextEpisode,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Next ›", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Pills (Follow Episode, Bookmark, Like)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onToggleSubscription(drama.id) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSubscribed) CrimsonPrimary else CinemaCardSurfaceElevated,
                            contentColor = if (isSubscribed) Color.White else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(34.dp)
                            .testTag("web_notify_sub_button")
                    ) {
                        Icon(
                            imageVector = if (isSubscribed) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSubscribed) "Notifikasi Aktif" else "Ikuti Episode Baru",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = { onToggleBookmark(drama.id) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isBookmarked) GoldYellow.copy(alpha = 0.2f) else CinemaCardSurfaceElevated,
                            contentColor = if (isBookmarked) GoldYellow else TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBookmarked) "Tersimpan" else "Simpan", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = { onToggleLike(episodeKey) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isLiked) CrimsonDark.copy(alpha = 0.3f) else CinemaCardSurfaceElevated,
                            contentColor = if (isLiked) LiveRed else TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val likes = 1250 + episode.episodeNumber * 84 + (if (isLiked) 1 else 0)
                        Text("$likes", fontSize = 11.sp)
                    }
                }
            }
        }

        // 4. Web Episode Selector Rail (Pilih Episode)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Episode (${drama.releasedEpisodes}/${drama.totalEpisodes})",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${drama.releaseSchedule} • Sub Indo",
                        color = CrimsonLight,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(drama.episodes) { ep ->
                        val isCurrent = ep.episodeNumber == episode.episodeNumber
                        Surface(
                            color = if (isCurrent) CrimsonPrimary else CinemaCardSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, GoldAccent) else null,
                            modifier = Modifier
                                .width(90.dp)
                                .clickable { onSelectEpisode(ep) }
                                .testTag("web_ep_btn_${ep.episodeNumber}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "EPISODE ${ep.episodeNumber}",
                                    color = if (isCurrent) Color.White else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isCurrent) "▶ Diputar" else formatSeconds(ep.durationSeconds),
                                    color = if (isCurrent) GoldYellow else TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Sinopsis & Info Drama
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Sinopsis Cerita",
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = drama.synopsis,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Pemeran: ${drama.cast.joinToString(", ")}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 6. Web Comments & Discussion Section (Directly inline on website page!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Diskusi & Komentar Penonton (${episodeComments.size})",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bahas alur cerita Episode ${episode.episodeNumber} bersama penonton lain",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Comment input box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Tulis komentar tentang episode ini...", fontSize = 12.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CinemaCardSurface,
                            unfocusedContainerColor = CinemaCardSurface,
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("web_comment_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(drama.id, episode.episodeNumber, newCommentText.trim())
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CrimsonPrimary)
                            .testTag("web_comment_submit_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Kirim Komentar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of Comments
                episodeComments.forEach { comment ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CinemaCardSurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.userName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = comment.userName, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = comment.timeAgo, color = TextMuted, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = comment.text, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
