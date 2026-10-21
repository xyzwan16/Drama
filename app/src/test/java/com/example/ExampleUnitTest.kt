package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSearchFilterByTitleAndGenre() {
    val sampleDramas = listOf(
      "CEO Dingin Jatuh Cinta" to "Romantis",
      "Miliarder Menyamar Jadi OB" to "Miliarder",
      "Kembalinya Sang Ratu Bisnis" to "Balas Dendam",
      "Misteri Apartemen 404" to "Film Pendek"
    )

    // Filter by title query
    val titleQuery = "CEO"
    val filteredByTitle = sampleDramas.filter { (title, genre) ->
      title.contains(titleQuery, ignoreCase = true) || genre.contains(titleQuery, ignoreCase = true)
    }
    assertEquals(1, filteredByTitle.size)
    assertEquals("CEO Dingin Jatuh Cinta", filteredByTitle.first().first)

    // Filter by genre query
    val genreQuery = "Romantis"
    val filteredByGenre = sampleDramas.filter { (title, genre) ->
      title.contains(genreQuery, ignoreCase = true) || genre.contains(genreQuery, ignoreCase = true)
    }
    assertEquals(1, filteredByGenre.size)
    assertEquals("Romantis", filteredByGenre.first().second)

    // Case-insensitive search
    val lowerQuery = "miliarder"
    val filteredLower = sampleDramas.filter { (title, genre) ->
      title.contains(lowerQuery, ignoreCase = true) || genre.contains(lowerQuery, ignoreCase = true)
    }
    assertEquals(1, filteredLower.size)
  }

  @Test
  fun testGenreChipFilterToggle() {
    val dramaCatalog = listOf(
      Triple("CEO Dingin Jatuh Cinta", "Romance", listOf("Romance", "Romantis")),
      Triple("Pendekar Kota", "Action", listOf("Action", "Aksi")),
      Triple("Bos Galak Ternyata Kocak", "Comedy", listOf("Comedy", "Komedi", "Romcom"))
    )

    // Toggle to Romance
    val romanceDramas = dramaCatalog.filter { it.second == "Romance" || it.third.contains("Romance") }
    assertEquals(1, romanceDramas.size)
    assertEquals("CEO Dingin Jatuh Cinta", romanceDramas.first().first)

    // Toggle to Action
    val actionDramas = dramaCatalog.filter { it.second == "Action" || it.third.contains("Action") }
    assertEquals(1, actionDramas.size)
    assertEquals("Pendekar Kota", actionDramas.first().first)

    // Toggle to Comedy
    val comedyDramas = dramaCatalog.filter { it.second == "Comedy" || it.third.contains("Comedy") }
    assertEquals(1, comedyDramas.size)
    assertEquals("Bos Galak Ternyata Kocak", comedyDramas.first().first)
  }

  @Test
  fun testMultiCountryDramaCatalogAndFiltering() {
    data class MiniDrama(val title: String, val country: String, val flag: String, val genre: String)

    val dramas = listOf(
      MiniDrama("Kebangkitan Sang Dewa Perang", "China", "🇨🇳", "Action"),
      MiniDrama("Kaisar Bisnis & Istri Pengganti", "China", "🇨🇳", "Romance"),
      MiniDrama("Rahasia Reinkarnasi Anak Magang", "Korea", "🇰🇷", "Revenge"),
      MiniDrama("Cinta Tersembunyi Sang Chaebol", "Korea", "🇰🇷", "Comedy"),
      MiniDrama("Pembalasan Sang Pewaris Mutiara", "Thailand", "🇹🇭", "Revenge"),
      MiniDrama("Kamera Misterius di Shibuya", "Jepang", "🇯🇵", "Thriller"),
      MiniDrama("Pesona Sang Alpha Triliuner", "Amerika", "🇺🇸", "Romance"),
      MiniDrama("CEO Dingin Jatuh Cinta", "Indonesia", "🇮🇩", "Romance")
    )

    // Filter by China
    val chinaDramas = dramas.filter { it.country.equals("China", ignoreCase = true) }
    assertEquals(2, chinaDramas.size)
    assertTrue(chinaDramas.all { it.flag == "🇨🇳" })

    // Filter by Korea
    val koreaDramas = dramas.filter { it.country.equals("Korea", ignoreCase = true) }
    assertEquals(2, koreaDramas.size)
    assertTrue(koreaDramas.all { it.flag == "🇰🇷" })

    // Filter by Thailand
    val thaiDramas = dramas.filter { it.country.equals("Thailand", ignoreCase = true) }
    assertEquals(1, thaiDramas.size)
    assertEquals("Pembalasan Sang Pewaris Mutiara", thaiDramas.first().title)

    // Filter by Jepang
    val jepangDramas = dramas.filter { it.country.equals("Jepang", ignoreCase = true) }
    assertEquals(1, jepangDramas.size)
    assertEquals("🇯🇵", jepangDramas.first().flag)

    // Filter by Amerika
    val usDramas = dramas.filter { it.country.equals("Amerika", ignoreCase = true) }
    assertEquals(1, usDramas.size)
    assertEquals("🇺🇸", usDramas.first().flag)

    // All countries count
    val distinctCountries = dramas.map { it.country }.distinct()
    assertEquals(6, distinctCountries.size)
  }
}
