package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class GenreChipItem(
    val id: String,
    val label: String,
    val iconEmoji: String
)

val DEFAULT_GENRES = listOf(
    GenreChipItem("Semua", "Semua", "✨"),
    GenreChipItem("Romance", "Romance", "❤️"),
    GenreChipItem("Action", "Action", "💥"),
    GenreChipItem("Comedy", "Comedy", "😂"),
    GenreChipItem("Billionaire", "Billionaire", "💎"),
    GenreChipItem("Revenge", "Revenge", "🗡️"),
    GenreChipItem("Thriller", "Thriller", "👁️"),
    GenreChipItem("Series", "Series", "📺"),
    GenreChipItem("Film Pendek", "Film Pendek", "🎬")
)

@Composable
fun GenreFilterChipsRow(
    selectedGenre: String,
    onGenreSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    genres: List<GenreChipItem> = DEFAULT_GENRES
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("genre_filter_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        genres.forEach { item ->
            val isSelected = selectedGenre.equals(item.id, ignoreCase = true) ||
                (item.id == "Romance" && selectedGenre.equals("Romantis", ignoreCase = true)) ||
                (item.id == "Action" && selectedGenre.equals("Aksi", ignoreCase = true)) ||
                (item.id == "Comedy" && selectedGenre.equals("Komedi", ignoreCase = true)) ||
                (item.id == "Billionaire" && selectedGenre.equals("Miliarder", ignoreCase = true)) ||
                (item.id == "Revenge" && selectedGenre.equals("Balas Dendam", ignoreCase = true)) ||
                (item.id == "Thriller" && selectedGenre.equals("Misteri", ignoreCase = true))

            FilterChip(
                selected = isSelected,
                onClick = { onGenreSelect(item.id) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.iconEmoji,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrimsonPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = CinemaCardSurface,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) CrimsonPrimary else BorderSubtle,
                    selectedBorderColor = CrimsonPrimary,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.5.dp
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("genre_chip_${item.id.lowercase()}")
            )
        }
    }
}
