package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainNavigationTab
import com.example.ui.theme.*

@Composable
fun WebsiteTopNavBar(
    currentTab: MainNavigationTab,
    unreadCount: Int,
    searchQuery: String,
    selectedGenre: String = "Semua",
    selectedCountry: String = "Semua",
    isWebsiteMode: Boolean,
    currentUrl: String = "https://dramaku.id/beranda",
    onTabSelect: (MainNavigationTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onGenreSelect: (String) -> Unit = {},
    onCountrySelect: (String) -> Unit = {},
    onToggleViewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Surface(
        color = CinemaDarkSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Web Browser Address Bar & Window Header (Simulation of Full Web Environment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1018))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Browser window traffic light dots (close, minimize, expand)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Browser navigation arrows (Back, Forward, Refresh)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onTabSelect(MainNavigationTab.HOME) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali ke Beranda",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        IconButton(
                            onClick = { /* simulated web forward */ },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Maju",
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        IconButton(
                            onClick = { /* simulated reload */ },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Muat Ulang Halaman Web",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dynamic URL Address Bar Pill
                    Surface(
                        color = Color(0xFF1B1D2B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Koneksi Aman HTTPS",
                                tint = SuccessGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentUrl,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = CrimsonDark.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "WEB PORTAL",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Mode Switch Button (Web vs Mobile)
                FilledTonalButton(
                    onClick = onToggleViewMode,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isWebsiteMode) CrimsonDark.copy(alpha = 0.5f) else CinemaCardSurfaceElevated,
                        contentColor = if (isWebsiteMode) GoldYellow else TextSecondary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isWebsiteMode) "🌐 Mode Web" else "📱 Mode App",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main Website Header & Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onTabSelect(MainNavigationTab.HOME) }
                        .testTag("website_brand_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(listOf(CrimsonPrimary, CrimsonDark))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DRAMA",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "KU",
                                color = CrimsonPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = CrimsonPrimary,
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Text(
                                    text = "WEB",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Web Navigation Menu Items
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WebNavLink(
                        label = "Beranda",
                        isSelected = currentTab == MainNavigationTab.HOME,
                        onClick = { onTabSelect(MainNavigationTab.HOME) }
                    )
                    WebNavLink(
                        label = "Nonton",
                        isSelected = currentTab == MainNavigationTab.WATCH,
                        onClick = { onTabSelect(MainNavigationTab.WATCH) }
                    )
                    WebNavLink(
                        label = "Pemberitahuan",
                        badgeCount = unreadCount,
                        isSelected = currentTab == MainNavigationTab.NOTIFICATIONS,
                        onClick = { onTabSelect(MainNavigationTab.NOTIFICATIONS) }
                    )
                    WebNavLink(
                        label = "Koleksi",
                        isSelected = currentTab == MainNavigationTab.PROFILE,
                        onClick = { onTabSelect(MainNavigationTab.PROFILE) }
                    )
                }

                // Profile Avatar Indicator
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(CrimsonDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VIP",
                        color = GoldYellow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Integrated Top Web Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Cari judul drama atau genre (Romantis, Miliarder, Aksi, Balas Dendam...)",
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isNotEmpty()) CrimsonPrimary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("web_clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CinemaCardSurface,
                    unfocusedContainerColor = CinemaCardSurface,
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = CrimsonPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
                    .height(50.dp)
                    .testTag("web_search_input_field")
            )

            // Horizontal row of filter chips for website mode
            GenreFilterChipsRow(
                selectedGenre = selectedGenre,
                onGenreSelect = onGenreSelect,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Horizontal row of country filter chips (China, Korea, Indonesia, Thailand, Jepang, Amerika)
            CountryFilterChipsRow(
                selectedCountry = selectedCountry,
                onCountrySelect = onCountrySelect,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun WebNavLink(
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) CrimsonPrimary.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) CrimsonPrimary else TextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = LiveRed,
                    shape = CircleShape
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
