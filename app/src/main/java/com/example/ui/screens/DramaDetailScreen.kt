package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Drama
import com.example.model.Episode
import com.example.ui.theme.*

@Composable
fun DramaDetailScreen(
    drama: Drama,
    isSubscribed: Boolean,
    isBookmarked: Boolean,
    currentWatchEpisode: Int?,
    onBack: () -> Unit,
    onPlayEpisode: (Drama, Episode) -> Unit,
    onToggleSubscription: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSimulateNewEpisode: () -> Unit,
    isWebsiteMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Web Breadcrumb when in website mode
        if (isWebsiteMode) {
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
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { onBack() }
                        )
                        Text(text = " › ", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "Koleksi ${drama.countryFlag} ${drama.country}",
                            color = GoldAccent,
                            fontSize = 11.sp
                        )
                        Text(text = " › ", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = drama.title,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Hero Poster Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                AsyncImage(
                    model = drama.posterRes,
                    contentDescription = drama.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dramatic Dark Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    CinemaBlack.copy(alpha = 0.8f),
                                    CinemaBlack
                                )
                            )
                        )
                )

                // Top Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                // Header Info Bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = CrimsonPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SUB INDO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Country badge
                        Surface(
                            color = CinemaCardSurfaceElevated,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = drama.countryFlag, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = drama.country,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (drama.hasNewEpisode) {
                            Surface(
                                color = LiveRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "EPISODE BARU",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            color = CinemaCardSurface,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = drama.genre,
                                color = GoldYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = drama.title,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 28.sp
                    )

                    Text(
                        text = drama.titleAlt,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${drama.rating}", color = GoldYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(text = "• ${drama.viewsCount} Ditonton", color = TextSecondary, fontSize = 12.sp)

                        Text(
                            text = if (drama.isSeries) "• Ep ${drama.releasedEpisodes}/${drama.totalEpisodes}" else "• Film Pendek",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val targetEp = currentWatchEpisode ?: drama.releasedEpisodes
                    val epToPlay = drama.episodes.find { it.episodeNumber == targetEp } ?: drama.episodes.first()

                    Button(
                        onClick = { onPlayEpisode(drama, epToPlay) },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("detail_play_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentWatchEpisode != null) "Lanjut Ep $targetEp" else "Mulai Menonton",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = onToggleSubscription,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSubscribed) CrimsonDark.copy(alpha = 0.4f) else CinemaCardSurfaceElevated,
                            contentColor = if (isSubscribed) GoldYellow else TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("detail_subscribe_button")
                    ) {
                        Icon(
                            imageVector = if (isSubscribed) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSubscribed) "Diikuti" else "Notif",
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CinemaCardSurfaceElevated)
                            .testTag("detail_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Simpan",
                            tint = if (isBookmarked) GoldYellow else TextPrimary
                        )
                    }
                }

                // Schedule Info & Test Drop Banner
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinemaCardSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = CrimsonLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Jadwal Rilis Episode", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(drama.releaseSchedule, color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        TextButton(
                            onClick = onSimulateNewEpisode,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Simulasi Rilis", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Synopsis
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sinopsis Cerita",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = drama.synopsis,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                // Cast list
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Pemeran Utama",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    drama.cast.forEach { actor ->
                        Surface(
                            color = CinemaCardSurface,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = actor,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Episode List Header
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Episode (${drama.releasedEpisodes}/${drama.totalEpisodes})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Semua Sub Indo",
                        color = GoldYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Episodes Grid
        items(drama.episodes) { ep ->
            val isCurrent = ep.episodeNumber == currentWatchEpisode
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) CinemaCardSurfaceElevated else CinemaCardSurface
                ),
                shape = RoundedCornerShape(10.dp),
                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onPlayEpisode(drama, ep) }
                    .testTag("detail_episode_item_${ep.episodeNumber}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) CrimsonPrimary else CinemaDarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = "${ep.episodeNumber}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ep.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (ep.isNew) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = LiveRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "BARU",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = ep.previewSnippet,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (ep.isFree) "Gratis" else "VIP",
                            color = if (ep.isFree) SuccessGreen else GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${ep.durationSeconds}s",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
