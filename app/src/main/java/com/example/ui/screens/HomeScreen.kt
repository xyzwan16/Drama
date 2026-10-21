package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ui.components.DramaCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    dramas: List<Drama>,
    allDramas: List<Drama>,
    subscribedIds: Set<String>,
    selectedGenre: String,
    selectedCountry: String = "Semua",
    searchQuery: String = "",
    isWebsiteMode: Boolean = true,
    onGenreSelect: (String) -> Unit,
    onCountrySelect: (String) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onDramaClick: (Drama) -> Unit,
    onPlayClick: (Drama, Episode) -> Unit,
    onToggleSubscription: (String) -> Unit,
    onSimulateNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val heroDrama = allDramas.firstOrNull()
    val updatedDramas = allDramas.filter { it.hasNewEpisode }
    val internationalDramas = allDramas.filter { it.country != "Indonesia" }
    val isSearching = searchQuery.isNotBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Web Portal Welcome Showcase Bar (when isWebsiteMode is true)
        if (isWebsiteMode && !isSearching && selectedGenre == "Semua" && selectedCountry == "Semua") {
            item {
                Surface(
                    color = Color(0xFF141624),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("website_portal_announcement")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = CrimsonPrimary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PORTAL WEB RESMI",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🌐 dramaku.id/streaming",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "VIP Server • HD 1080P",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Nonton Drama Pendek Sub Indo Online",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Platform streaming drama pendek terlengkap dari China 🇨🇳, Korea 🇰🇷, Thailand 🇹🇭, Jepang 🇯🇵, & Indonesia 🇮🇩 dengan pembaruan episode otomatis.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf(
                                "🇨🇳 Drama China" to "China",
                                "🇰🇷 K-Drama" to "Korea",
                                "🇮🇩 Serial Indonesia" to "Indonesia",
                                "🇹🇭 Lakorn Thai" to "Thailand",
                                "🇯🇵 J-Drama" to "Jepang"
                            ).forEach { (label, countryCode) ->
                                AssistChip(
                                    onClick = { onCountrySelect(countryCode) },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = CinemaCardSurfaceElevated,
                                        labelColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        // Search Feedback Banner when searching
        if (isSearching) {
            item {
                Surface(
                    color = CinemaCardSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_results_summary")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hasil Pencarian: \"$searchQuery\"",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${dramas.size} drama ditemukan (berdasarkan judul atau genre)",
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                        }

                        TextButton(
                            onClick = onClearSearch,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("reset_search_button")
                        ) {
                            Text("Reset", color = CrimsonLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }



        // Hero Banner (Featured Trending Drama) - Only shown on default discovery mode
        if (!isSearching && heroDrama != null && selectedGenre == "Semua") {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(280.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onDramaClick(heroDrama) }
                        .testTag("hero_banner")
                ) {
                    AsyncImage(
                        model = heroDrama.posterRes,
                        contentDescription = heroDrama.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Cinematic Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        CinemaBlack.copy(alpha = 0.95f)
                                    ),
                                    startY = 50f
                                )
                            )
                    )

                    // Top Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = LiveRed,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🔥 TRENDING #1",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SUB INDO LENGKAP",
                                color = GoldYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Bottom Details & Action
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = heroDrama.title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Episode ${heroDrama.releasedEpisodes} Baru Rilis • ${heroDrama.releaseSchedule}",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val ep = heroDrama.episodes.find { it.episodeNumber == heroDrama.releasedEpisodes }
                                        ?: heroDrama.episodes.first()
                                    onPlayClick(heroDrama, ep)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("hero_play_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tonton Ep ${heroDrama.releasedEpisodes}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            FilledTonalButton(
                                onClick = { onToggleSubscription(heroDrama.id) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CinemaCardSurfaceElevated,
                                    contentColor = if (subscribedIds.contains(heroDrama.id)) GoldAccent else TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("hero_notify_button")
                            ) {
                                Icon(
                                    imageVector = if (subscribedIds.contains(heroDrama.id)) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (subscribedIds.contains(heroDrama.id)) "Diikuti" else "Notifikasi",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Pembaruan Episode Terbaru (Horizontal Rail) - Only on home discovery
        if (!isSearching && updatedDramas.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LiveRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pembaruan Episode Terbaru",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Hari Ini",
                            color = CrimsonLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(updatedDramas) { drama ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable {
                                        val ep = drama.episodes.find { it.episodeNumber == drama.releasedEpisodes }
                                            ?: drama.episodes.first()
                                        onPlayClick(drama, ep)
                                    }
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = drama.posterRes,
                                            contentDescription = drama.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Surface(
                                            color = LiveRed,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "EP ${drama.releasedEpisodes} BARU",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                                    )
                                                )
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "Sub Indo Ready",
                                                color = GoldYellow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = drama.title,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Drama Mancanegara Populer (Sub Indo) - China, Korea, Thailand, Jepang, Amerika
        if (!isSearching && internationalDramas.isNotEmpty() && selectedCountry == "Semua") {
            item {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌍", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Drama Mancanegara Populer",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = CrimsonDark.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SUB INDO",
                                color = GoldYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(internationalDramas) { drama ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { onDramaClick(drama) }
                                    .testTag("intl_drama_${drama.id}")
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = drama.posterRes,
                                            contentDescription = drama.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Country badge with flag
                                        Surface(
                                            color = CinemaDarkSurface.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = drama.countryFlag, fontSize = 10.sp)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = drama.country,
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Sub Indo indicator at bottom
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                                    )
                                                )
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "⭐ ${drama.rating} • ${drama.genre}",
                                                color = GoldAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = drama.title,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${drama.country} • Ep ${drama.releasedEpisodes}/${drama.totalEpisodes}",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Test Notification Simulation Interactive Card - Only on home discovery
        if (!isSearching) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinemaCardSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("notification_simulation_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CrimsonPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fitur Notifikasi Rilis Episode",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dapatkan pemberitahuan otomatis di status bar ponsel ketika episode drama favorit rilis.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onSimulateNotification("miliarder_ob")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("test_push_notification_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Uji Notifikasi Episode Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section: Grid Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = when {
                        isSearching -> "Hasil Pencarian Judul & Genre"
                        selectedCountry != "Semua" && selectedGenre != "Semua" -> "Drama $selectedCountry: $selectedGenre"
                        selectedCountry != "Semua" -> "Koleksi Drama $selectedCountry (Sub Indo)"
                        selectedGenre != "Semua" -> "Kategori: $selectedGenre"
                        else -> "Semua Series & Film Pendek"
                    },
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSearching) {
                        "${dramas.size} drama sesuai kata kunci"
                    } else if (selectedCountry != "Semua") {
                        "${dramas.size} drama dari $selectedCountry dengan Subtitle Indonesia"
                    } else {
                        "Lengkap dengan Subtitle Indonesia & Kualitas HD"
                    },
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Two-column grid using chunked
        val chunkedDramas = dramas.chunked(2)
        items(chunkedDramas) { rowDramas ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowDramas.forEach { drama ->
                    Box(modifier = Modifier.weight(1f)) {
                        DramaCard(
                            drama = drama,
                            isSubscribed = subscribedIds.contains(drama.id),
                            onCardClick = { onDramaClick(drama) },
                            onToggleSubscription = { onToggleSubscription(drama.id) }
                        )
                    }
                }
                if (rowDramas.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Empty state when no drama matches search
        if (dramas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("search_empty_state")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when {
                                isSearching -> "Drama tidak ditemukan"
                                selectedCountry != "Semua" -> "Tidak ada drama untuk $selectedCountry"
                                else -> "Tidak ada drama di kategori ini"
                            },
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                isSearching -> "Tidak ada drama yang cocok dengan \"$searchQuery\". Coba cari berdasarkan kata kunci atau negara lain."
                                selectedCountry != "Semua" -> "Belum ada drama untuk negara $selectedCountry dalam kategori $selectedGenre."
                                else -> "Silakan pilih kategori lain atau reset filter."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick suggestion chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf("China 🇨🇳", "Korea 🇰🇷", "Thailand 🇹🇭", "Romantis", "Miliarder").forEach { tag ->
                                AssistChip(
                                    onClick = {
                                        when (tag) {
                                            "China 🇨🇳" -> onCountrySelect("China")
                                            "Korea 🇰🇷" -> onCountrySelect("Korea")
                                            "Thailand 🇹🇭" -> onCountrySelect("Thailand")
                                            else -> onGenreSelect(tag)
                                        }
                                    },
                                    label = { Text(tag, fontSize = 11.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = CinemaCardSurfaceElevated,
                                        labelColor = GoldAccent
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(
                                        enabled = true,
                                        borderColor = BorderSubtle
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        if (isSearching || selectedCountry != "Semua" || selectedGenre != "Semua") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    onClearSearch()
                                    onCountrySelect("Semua")
                                    onGenreSelect("Semua")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reset Filter & Tampilkan Semua", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Web Streaming Portal Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 16.dp)
                    .background(CinemaDarkSurface)
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("DRAMA", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("KU", color = CrimsonPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = CrimsonDark.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "WEB STREAMING PORTAL",
                            color = GoldYellow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pusat streaming drama pendek, series viral & film mini dengan Subtitle Indonesia resmi.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("Beranda Web", color = TextSecondary, fontSize = 11.sp)
                    Text("Jadwal Rilis", color = TextSecondary, fontSize = 11.sp)
                    Text("Notifikasi Episode", color = TextSecondary, fontSize = 11.sp)
                    Text("Bantuan & Subtitle", color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "© 2026 Dramaku ID Web. Dilengkapi notifikasi rilis episode otomatis.",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
