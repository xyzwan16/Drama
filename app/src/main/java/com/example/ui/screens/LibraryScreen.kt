package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser
import com.example.model.Drama
import com.example.model.Episode
import com.example.model.SubtitleConfig
import com.example.model.SubtitleLanguage
import com.example.ui.theme.*

@Composable
fun LibraryScreen(
    allDramas: List<Drama>,
    bookmarkedIds: Set<String>,
    subscribedIds: Set<String>,
    watchHistory: Map<String, Int>,
    subtitleConfig: SubtitleConfig,
    currentUser: FirebaseUser? = null,
    isSigningIn: Boolean = false,
    onGoogleSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDramaClick: (Drama) -> Unit,
    onPlayClick: (Drama, Episode) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleSubscription: (String) -> Unit,
    onUpdateSubtitleConfig: (SubtitleLanguage?, Int?, Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Tersimpan, 1: Riwayat, 2: Series Diikuti, 3: Pengaturan
    val tabs = listOf("Tersimpan", "Riwayat", "Diikuti", "Pengaturan")

    val bookmarkedDramas = remember(allDramas, bookmarkedIds) {
        allDramas.filter { bookmarkedIds.contains(it.id) }
    }

    val subscribedDramas = remember(allDramas, subscribedIds) {
        allDramas.filter { subscribedIds.contains(it.id) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // User Profile & Google Sign-In Header
        item {
            if (currentUser != null) {
                Surface(
                    color = CinemaDarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val photoUrl = currentUser.photoUrl?.toString()
                        if (!photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            val initials = (currentUser.displayName ?: "DK")
                                .split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .uppercase()
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (initials.isNotEmpty()) initials else "DK",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.displayName ?: "Penonton Dramaku",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = GoldAccent,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "VIP MEMBER",
                                        color = CinemaBlack,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (!currentUser.email.isNullOrBlank()) {
                                Text(
                                    text = currentUser.email ?: "",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "☁️ Firebase Sync Aktif",
                                    color = SuccessGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = " • ${subscribedIds.size} Diikuti • ${bookmarkedIds.size} Tersimpan",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("sign_out_button")
                        ) {
                            Text("Keluar", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                // Unauthenticated State with Interactive Google Sign-In Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinemaCardSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("google_auth_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sinkronisasi Koleksi Cloud",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Masuk untuk menyimpan riwayat dan series favorit Anda di Firebase Firestore.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onGoogleSignIn,
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CinemaBlack,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Menghubungkan...",
                                    color = CinemaBlack,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Icon",
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign in with Google",
                                    color = Color(0xFF1F1F1F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sub Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CinemaDarkSurface,
                contentColor = CrimsonPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CrimsonPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) CrimsonPrimary else TextSecondary
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tab 0: Tersimpan (Watchlist)
        if (selectedTab == 0) {
            if (bookmarkedDramas.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        icon = Icons.Default.BookmarkBorder,
                        title = "Belum Ada Drama yang Disimpan",
                        desc = "Tekan ikon bookmark pada drama untuk menyimpannya ke koleksi Anda."
                    )
                }
            } else {
                items(bookmarkedDramas) { drama ->
                    LibraryDramaRowItem(
                        drama = drama,
                        statusText = "Koleksi Favorit • ${drama.genre}",
                        actionButtonText = "Tonton",
                        onDramaClick = { onDramaClick(drama) },
                        onActionClick = {
                            val ep = drama.episodes.firstOrNull() ?: return@LibraryDramaRowItem
                            onPlayClick(drama, ep)
                        },
                        onRemove = { onToggleBookmark(drama.id) }
                    )
                }
            }
        }

        // Tab 1: Riwayat Menonton
        if (selectedTab == 1) {
            val watchedList = allDramas.filter { watchHistory.containsKey(it.id) }
            if (watchedList.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        icon = Icons.Default.History,
                        title = "Belum Ada Riwayat Tontonan",
                        desc = "Mulai menonton drama pendek untuk melihat riwayat episode terakhir di sini."
                    )
                }
            } else {
                items(watchedList) { drama ->
                    val lastEp = watchHistory[drama.id] ?: 1
                    LibraryDramaRowItem(
                        drama = drama,
                        statusText = "Sedang menonton Episode $lastEp dari ${drama.totalEpisodes}",
                        actionButtonText = "Lanjut Ep $lastEp",
                        onDramaClick = { onDramaClick(drama) },
                        onActionClick = {
                            val ep = drama.episodes.find { it.episodeNumber == lastEp } ?: drama.episodes.first()
                            onPlayClick(drama, ep)
                        }
                    )
                }
            }
        }

        // Tab 2: Series Diikuti (Notification Subscriptions)
        if (selectedTab == 2) {
            item {
                Surface(
                    color = CinemaCardSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Anda akan menerima notifikasi status bar seketika episode baru dari series di bawah ini tayang.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            if (subscribedDramas.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        icon = Icons.Default.NotificationsNone,
                        title = "Belum Mengikuti Series",
                        desc = "Aktifkan lonceng notifikasi pada drama favorit untuk berlangganan pemberitahuan episode."
                    )
                }
            } else {
                items(subscribedDramas) { drama ->
                    LibraryDramaRowItem(
                        drama = drama,
                        statusText = "Jadwal: ${drama.releaseSchedule}",
                        actionButtonText = "Tonton Ep ${drama.releasedEpisodes}",
                        onDramaClick = { onDramaClick(drama) },
                        onActionClick = {
                            val ep = drama.episodes.find { it.episodeNumber == drama.releasedEpisodes } ?: drama.episodes.first()
                            onPlayClick(drama, ep)
                        },
                        onRemove = { onToggleSubscription(drama.id) }
                    )
                }
            }
        }

        // Tab 3: Pengaturan Subtitle & Notifikasi
        if (selectedTab == 3) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Preferensi Subtitle & Audio",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CinemaCardSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Bahasa Subtitle Standar",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            SubtitleLanguage.values().forEach { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUpdateSubtitleConfig(lang, null, null) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        color = if (subtitleConfig.language == lang) CrimsonLight else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (subtitleConfig.language == lang) FontWeight.Bold else FontWeight.Normal
                                    )
                                    RadioButton(
                                        selected = subtitleConfig.language == lang,
                                        onClick = { onUpdateSubtitleConfig(lang, null, null) },
                                        colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tentang Aplikasi",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CinemaCardSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Dramaku - Nonton Drama Pendek Sub Indo",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Versi 1.0.0 • Dilengkapi notifikasi pembaruan episode terbaru series & movie, subtitle Bahasa Indonesia presisi tinggi, dan pemutar video vertikal 9:16.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryDramaRowItem(
    drama: Drama,
    statusText: String,
    actionButtonText: String,
    onDramaClick: () -> Unit,
    onActionClick: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinemaCardSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onDramaClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(55.dp)
                    .height(78.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = drama.posterRes,
                    contentDescription = drama.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drama.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = statusText,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(actionButtonText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (onRemove != null) {
                        OutlinedButton(
                            onClick = onRemove,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Hapus", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
