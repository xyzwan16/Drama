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

data class CountryChipItem(
    val id: String,
    val label: String,
    val flag: String
)

val DEFAULT_COUNTRIES = listOf(
    CountryChipItem("Semua", "Semua Negara", "🌍"),
    CountryChipItem("China", "China", "🇨🇳"),
    CountryChipItem("Korea", "Korea", "🇰🇷"),
    CountryChipItem("Indonesia", "Indonesia", "🇮🇩"),
    CountryChipItem("Thailand", "Thailand", "🇹🇭"),
    CountryChipItem("Jepang", "Jepang", "🇯🇵"),
    CountryChipItem("Amerika", "Amerika", "🇺🇸")
)

@Composable
fun CountryFilterChipsRow(
    selectedCountry: String,
    onCountrySelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    countries: List<CountryChipItem> = DEFAULT_COUNTRIES
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("country_filter_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        countries.forEach { item ->
            val isSelected = selectedCountry.equals(item.id, ignoreCase = true) ||
                (item.id == "Korea" && selectedCountry.equals("Korea Selatan", ignoreCase = true)) ||
                (item.id == "Amerika" && selectedCountry.equals("Amerika Serikat", ignoreCase = true))

            FilterChip(
                selected = isSelected,
                onClick = { onCountrySelect(item.id) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.flag, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldAccent,
                    selectedLabelColor = CinemaBlack,
                    containerColor = CinemaCardSurfaceElevated,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) GoldAccent else BorderSubtle,
                    selectedBorderColor = GoldAccent,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("country_chip_${item.id.lowercase()}")
            )
        }
    }
}
