package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.*
import com.example.util.NotificationHelper
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DramaRepository(
    private val context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    ),
    private val auth: FirebaseAuth = Firebase.auth
) {

    private val _dramas = MutableStateFlow<List<Drama>>(emptyList())
    val dramas: StateFlow<List<Drama>> = _dramas.asStateFlow()

    private val _subscribedDramaIds = MutableStateFlow<Set<String>>(setOf("ceo_dingin", "miliarder_ob"))
    val subscribedDramaIds: StateFlow<Set<String>> = _subscribedDramaIds.asStateFlow()

    private val _bookmarkedDramaIds = MutableStateFlow<Set<String>>(setOf("ceo_dingin", "ratu_bisnis"))
    val bookmarkedDramaIds: StateFlow<Set<String>> = _bookmarkedDramaIds.asStateFlow()

    private val _likedEpisodeIds = MutableStateFlow<Set<String>>(setOf("ceo_dingin_ep_1", "miliarder_ob_ep_1"))
    val likedEpisodeIds: StateFlow<Set<String>> = _likedEpisodeIds.asStateFlow()

    private val _watchHistory = MutableStateFlow<Map<String, Int>>(mapOf("ceo_dingin" to 19, "miliarder_ob" to 24))
    val watchHistory: StateFlow<Map<String, Int>> = _watchHistory.asStateFlow()

    private val _notifications = MutableStateFlow<List<DramaNotification>>(emptyList())
    val notifications: StateFlow<List<DramaNotification>> = _notifications.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<DramaComment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<DramaComment>>> = _comments.asStateFlow()

    private val _subtitleConfig = MutableStateFlow(SubtitleConfig())
    val subtitleConfig: StateFlow<SubtitleConfig> = _subtitleConfig.asStateFlow()

    private var bookmarksListener: ListenerRegistration? = null
    private var subscriptionsListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null
    private var commentsListener: ListenerRegistration? = null

    init {
        initializeData()
        attachGlobalCommentsListener()
        // If a user is already signed in, attach listeners
        auth.currentUser?.let { user ->
            attachUserListeners(user.uid)
        }
    }

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun onUserSignedIn(user: FirebaseUser) {
        attachUserListeners(user.uid)
        saveUserProfile(user)
    }

    fun onUserSignedOut() {
        detachUserListeners()
    }

    fun attachUserListeners(userId: String) {
        detachUserListeners()

        val bookmarksPath = "users/$userId/bookmarks"
        bookmarksListener = db.collection("users").document(userId).collection("bookmarks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, bookmarksPath)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ids = snapshot.documents.map { it.id }.toSet()
                    if (ids.isNotEmpty()) {
                        _bookmarkedDramaIds.value = ids
                    }
                }
            }

        val subscriptionsPath = "users/$userId/subscriptions"
        subscriptionsListener = db.collection("users").document(userId).collection("subscriptions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, subscriptionsPath)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ids = snapshot.documents.map { it.id }.toSet()
                    if (ids.isNotEmpty()) {
                        _subscribedDramaIds.value = ids
                    }
                }
            }

        val historyPath = "users/$userId/history"
        historyListener = db.collection("users").document(userId).collection("history")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, historyPath)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val historyMap = mutableMapOf<String, Int>()
                    for (doc in snapshot.documents) {
                        val dramaId = doc.getString("dramaId") ?: doc.id
                        val lastEp = (doc.get("lastEpisode") as? Number)?.toInt() ?: 1
                        historyMap[dramaId] = lastEp
                    }
                    if (historyMap.isNotEmpty()) {
                        _watchHistory.value = historyMap
                    }
                }
            }
    }

    fun detachUserListeners() {
        bookmarksListener?.remove()
        bookmarksListener = null
        subscriptionsListener?.remove()
        subscriptionsListener = null
        historyListener?.remove()
        historyListener = null
    }

    private fun attachGlobalCommentsListener() {
        val path = "comments"
        commentsListener = db.collection(path)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val newCommentsMap = _comments.value.toMutableMap()
                    for (doc in snapshot.documents) {
                        val dramaId = doc.getString("dramaId") ?: continue
                        val epNumber = (doc.get("episodeNumber") as? Number)?.toInt() ?: 1
                        val key = "${dramaId}_$epNumber"
                        val comment = DramaComment(
                            id = doc.id,
                            dramaId = dramaId,
                            episodeNumber = epNumber,
                            userName = doc.getString("userName") ?: "Penonton Dramaku",
                            avatarInitials = (doc.getString("userName") ?: "PD").take(2).uppercase(),
                            text = doc.getString("text") ?: "",
                            timeAgo = "Baru saja",
                            likesCount = (doc.get("likesCount") as? Number)?.toInt() ?: 1,
                            isLiked = false
                        )
                        val existing = newCommentsMap[key] ?: emptyList()
                        if (existing.none { it.id == comment.id }) {
                            newCommentsMap[key] = listOf(comment) + existing
                        }
                    }
                    _comments.value = newCommentsMap
                }
            }
    }

    fun saveUserProfile(user: FirebaseUser) {
        val path = "users/${user.uid}"
        val profileData = mapOf(
            "id" to user.uid,
            "displayName" to (user.displayName ?: "Penggemar Dramaku"),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(user.uid)
            .set(profileData, SetOptions.merge())
            .addOnFailureListener { error ->
                handleFirestoreError(error, OperationType.WRITE, path)
            }
    }

    private fun initializeData() {
        val dramaList = listOf(
            Drama(
                id = "ceo_dingin",
                title = "CEO Dingin Jatuh Cinta",
                titleAlt = "Cold CEO's Secret Obsession",
                synopsis = "Kevin, pewaris konglomerat yang dikenal berdarah dingin dan tak tersentuh, tanpa sengaja bertemu kembali dengan Sarah, wanita yang meninggalkannya lima tahun lalu. Sarah kini bekerja sebagai asisten pribadinya tanpa mengetahui motif tersembunyi sang CEO.",
                posterRes = R.drawable.poster_ceo_dingin_1792425093218,
                genre = "Romance",
                tags = listOf("Romance", "Romantis", "CEO", "Balas Dendam", "Populer"),
                isSeries = true,
                totalEpisodes = 24,
                releasedEpisodes = 19,
                rating = 4.9,
                viewsCount = "8.4M",
                hasNewEpisode = true,
                releaseSchedule = "Setiap Hari 19.00 WIB",
                cast = listOf("Nicholas Saputra", "Hannah Al Rashid", "Reza Rahadian"),
                episodes = generateEpisodesForCeoDingin()
            ),
            Drama(
                id = "miliarder_ob",
                title = "Miliarder Menyamar Jadi OB",
                titleAlt = "Undercover Billionaire Janitor",
                synopsis = "Arga, triliuner muda pemilik Mega Corp, sengaja menyamar sebagai office boy rendahan demi menyelidiki kecurangan direksi dan menguji cinta sejatinya. Di sana ia dihina setiap hari oleh manajer arogan, hingga rahasianya mulai terbongkar di rapat umum pemegang saham.",
                posterRes = R.drawable.poster_miliarder_ob_1792425094049,
                genre = "Miliarder",
                tags = listOf("Miliarder", "Aksi", "Pembalasan", "Trending"),
                isSeries = true,
                totalEpisodes = 30,
                releasedEpisodes = 25,
                rating = 4.8,
                viewsCount = "12.1M",
                hasNewEpisode = true,
                releaseSchedule = "Selasa & Jumat 20.00 WIB",
                cast = listOf("Jefri Nichol", "Laura Basuki", "Chicco Jerikho"),
                episodes = generateEpisodesForMiliarderOB()
            ),
            Drama(
                id = "ratu_bisnis",
                title = "Kembalinya Sang Ratu Bisnis",
                titleAlt = "The Return of The Business Queen",
                synopsis = "Tiga tahun setelah dikhianati dan dibuang keluarga konglomeratnya ke luar negeri, Clarissa kembali dengan identitas baru sebagai investor pengendali Astra Holdings. Satu per satu keluarga yang menindasnya akan ia hadapkan pada konsekuensi kepalsuan mereka.",
                posterRes = R.drawable.poster_ratu_bisnis_1792425113858,
                genre = "Balas Dendam",
                tags = listOf("Balas Dendam", "Emansipasi", "Intrik Bisnis", "Populer"),
                isSeries = true,
                totalEpisodes = 18,
                releasedEpisodes = 14,
                rating = 4.9,
                viewsCount = "6.7M",
                hasNewEpisode = true,
                releaseSchedule = "Setiap Hari 18.30 WIB",
                cast = listOf("Dian Sastrowardoyo", "Ario Bayu", "Marsha Timothy"),
                episodes = generateEpisodesForRatuBisnis()
            ),
            Drama(
                id = "misteri_apartemen",
                title = "Misteri Apartemen 404",
                titleAlt = "Room 404: Whispers in the Dark",
                synopsis = "Sebuah film pendek misteri menegangkan tentang seorang mahasiswi seni yang menyewa apartemen murah di pusat kota Jakarta. Tiap jam 03.15 pagi, bel pintu selalu berbunyi dan pesan-pesan aneh mulai bermunculan di cermin kamar mandi.",
                posterRes = R.drawable.poster_ceo_dingin_1792425093218,
                genre = "Film Pendek",
                tags = listOf("Film Pendek", "Thriller", "Misteri", "Sub Indo"),
                isSeries = false,
                totalEpisodes = 1,
                releasedEpisodes = 1,
                rating = 4.7,
                viewsCount = "3.2M",
                hasNewEpisode = false,
                releaseSchedule = "Film Pendek Lengkap (45 Menit)",
                cast = listOf("Shenina Cinnamon", "Lutesha"),
                episodes = generateEpisodesForMisteriMovie()
            ),
            Drama(
                id = "pernikahan_rahasia",
                title = "Pernikahan Rahasia Pewaris",
                titleAlt = "Secret Marriage with the Heir",
                synopsis = "Demi menyelamatkan toko kue milik neneknya, Maya menandatangani kontrak pernikahan rahasia selama satu tahun dengan pewaris tunggal rumah sakit internasional. Namun syarat utama pernikahan itu adalah tidak boleh jatuh cinta.",
                posterRes = R.drawable.poster_ratu_bisnis_1792425113858,
                genre = "Romance",
                tags = listOf("Romance", "Comedy", "Romcom", "Romantis", "Kontrak Nikah"),
                isSeries = true,
                totalEpisodes = 22,
                releasedEpisodes = 18,
                rating = 4.6,
                viewsCount = "5.1M",
                hasNewEpisode = false,
                releaseSchedule = "Rabu & Sabtu 19.00 WIB",
                cast = listOf("Prilly Latuconsina", "Angga Yunanda"),
                episodes = generateEpisodesForCeoDingin()
            ),
            Drama(
                id = "pendekar_kota",
                title = "Pendekar Kota Metropolitan",
                titleAlt = "Urban Shadow Fighter",
                synopsis = "Setelah turun gunung pasca 10 tahun berlatih bela diri kuno, Danu mencari pembunuh ayahnya di gemerlap dunia mafia ibu kota. Dengan ilmu kanuragan modern, ia menumpas sindikat gelap satu per satu.",
                posterRes = R.drawable.poster_miliarder_ob_1792425094049,
                genre = "Action",
                tags = listOf("Action", "Aksi", "Bela Diri", "Fantasi Modern"),
                isSeries = true,
                totalEpisodes = 28,
                releasedEpisodes = 20,
                rating = 4.7,
                viewsCount = "7.8M",
                hasNewEpisode = false,
                releaseSchedule = "Kamis & Minggu 21.00 WIB",
                cast = listOf("Iko Uwais", "Joe Taslim"),
                episodes = generateEpisodesForMiliarderOB()
            ),
            Drama(
                id = "bos_kocak",
                title = "Bos Galak Ternyata Kocak",
                titleAlt = "My Stern Boss is a Secret Clown",
                synopsis = "Di kantor ia ditakuti seluruh karyawan, namun di media sosial ia adalah komedian bertopeng dengan jutaan pengikut. Seorang sekretaris magang tanpa sengaja memergoki sang direktur sedang latihan stand-up comedy di pantry kantor!",
                posterRes = R.drawable.poster_ratu_bisnis_1792425113858,
                genre = "Comedy",
                tags = listOf("Comedy", "Komedi", "Romance", "Romcom", "Populer"),
                isSeries = true,
                totalEpisodes = 16,
                releasedEpisodes = 12,
                rating = 4.8,
                viewsCount = "6.1M",
                hasNewEpisode = true,
                releaseSchedule = "Senin & Kamis 19.30 WIB",
                cast = listOf("Reza Rahadian", "Tara Basro"),
                episodes = generateEpisodesForCeoDingin(),
                country = "Indonesia",
                countryFlag = "🇮🇩"
            ),
            Drama(
                id = "c_dewa_perang",
                title = "Kebangkitan Sang Dewa Perang",
                titleAlt = "Return of The Supreme God of War",
                synopsis = "Panglima tertinggi yang menguasai jutaan prajurit menyembunyikan identitas aslinya demi merawat putri semata wayangnya. Ketika keluarga bangsawan kota menginjak harga dirinya, satu lencana emas membuat seluruh wali kota bertekuk lutut.",
                posterRes = R.drawable.poster_miliarder_ob_1792425094049,
                genre = "Action",
                tags = listOf("China", "C-Drama", "Action", "Dewa Perang", "Miliarder", "Sub Indo"),
                isSeries = true,
                totalEpisodes = 32,
                releasedEpisodes = 28,
                rating = 4.9,
                viewsCount = "15.4M",
                hasNewEpisode = true,
                releaseSchedule = "Setiap Hari 20.00 WIB",
                cast = listOf("Xiao Zhan", "Zhao Lusi"),
                episodes = generateEpisodesForDewaPerang(),
                country = "China",
                countryFlag = "🇨🇳"
            ),
            Drama(
                id = "c_istri_pengganti",
                title = "Kaisar Bisnis & Istri Pengganti",
                titleAlt = "The Tycoon's Secret Substitute",
                synopsis = "Lin Ran dipaksa menggantikan saudara tirinya menikah dengan Lu Tingxiao, kaisar bisnis yang dikabarkan lumpuh dan kejam. Tanpa disangka, sang kaisar hanya berpura-pura sakit demi membongkar pengkhianat di dewan direksi.",
                posterRes = R.drawable.poster_ceo_dingin_1792425093218,
                genre = "Romance",
                tags = listOf("China", "C-Drama", "Romance", "Billionaire", "Romantis", "Sub Indo"),
                isSeries = true,
                totalEpisodes = 26,
                releasedEpisodes = 22,
                rating = 4.8,
                viewsCount = "11.2M",
                hasNewEpisode = true,
                releaseSchedule = "Senin, Rabu, Jumat 19.00 WIB",
                cast = listOf("Wang Hedi", "Bai Lu"),
                episodes = generateEpisodesForCeoDingin(),
                country = "China",
                countryFlag = "🇨🇳"
            ),
            Drama(
                id = "k_intern_reinkarnasi",
                title = "Rahasia Reinkarnasi Anak Magang",
                titleAlt = "The Intern's Second Life in Gangnam",
                synopsis = "CEO muda yang dikhianati dewan direksinya terbangun 7 tahun di masa lalu dalam raga anak magang kantor. Berbekal ingatan masa depan tentang tren saham dan rahasia korupsi, ia membalas dendam dengan gemilang.",
                posterRes = R.drawable.poster_ratu_bisnis_1792425113858,
                genre = "Revenge",
                tags = listOf("Korea", "K-Drama", "Revenge", "Billionaire", "Balas Dendam", "Sub Indo"),
                isSeries = true,
                totalEpisodes = 20,
                releasedEpisodes = 16,
                rating = 4.9,
                viewsCount = "9.8M",
                hasNewEpisode = true,
                releaseSchedule = "Selasa & Sabtu 20.30 WIB",
                cast = listOf("Park Seo-joon", "Kim Da-mi"),
                episodes = generateEpisodesForKoreaIntern(),
                country = "Korea",
                countryFlag = "🇰🇷"
            ),
            Drama(
                id = "k_chaebol_love",
                title = "Cinta Tersembunyi Sang Chaebol",
                titleAlt = "Secret Cafe Romance with Chaebol",
                synopsis = "Pewaris generasi ketiga konglomerat Korea pura-pura menjadi barista kafe demi mendekati ilustrator komik webtoon yang menggambar kisah cinta mereka secara tak sengaja.",
                posterRes = R.drawable.poster_ceo_dingin_1792425093218,
                genre = "Comedy",
                tags = listOf("Korea", "K-Drama", "Comedy", "Romance", "Romcom", "Komedi", "Sub Indo"),
                isSeries = true,
                totalEpisodes = 18,
                releasedEpisodes = 18,
                rating = 4.7,
                viewsCount = "7.3M",
                hasNewEpisode = false,
                releaseSchedule = "Lengkap 18 Episode",
                cast = listOf("Song Kang", "Kim Yoo-jung"),
                episodes = generateEpisodesForCeoDingin(),
                country = "Korea",
                countryFlag = "🇰🇷"
            ),
            Drama(
                id = "th_pewaris_mutiara",
                title = "Pembalasan Sang Pewaris Mutiara",
                titleAlt = "Revenge of the Andaman Pearl Heiress",
                synopsis = "Dikhianati oleh ibu tiri dan saudara angkatnya, Prim kembali dengan kekayaan tak terduga sebagai pengendali kepulauan mutiara Andaman. Drama lakorn mini penuh intrik balas dendam tajam.",
                posterRes = R.drawable.poster_ratu_bisnis_1792425113858,
                genre = "Revenge",
                tags = listOf("Thailand", "Thai Drama", "Revenge", "Lakorn", "Balas Dendam", "Sub Indo"),
                isSeries = true,
                totalEpisodes = 24,
                releasedEpisodes = 20,
                rating = 4.8,
                viewsCount = "6.5M",
                hasNewEpisode = true,
                releaseSchedule = "Rabu & Minggu 20.00 WIB",
                cast = listOf("Baifern Pimchanok", "Push Puttichai"),
                episodes = generateEpisodesForThaiLakorn(),
                country = "Thailand",
                countryFlag = "🇹🇭"
            ),
            Drama(
                id = "jp_midnight_lens",
                title = "Kamera Misterius di Shibuya",
                titleAlt = "Tokyo Midnight Lens",
                synopsis = "Seorang fotografer jalanan di Shibuya menemukan kamera antik yang dapat memotret masa depan 10 menit ke depan, mengungkap kejahatan sindikat rahasia sebelum terjadi.",
                posterRes = R.drawable.poster_miliarder_ob_1792425094049,
                genre = "Thriller",
                tags = listOf("Jepang", "J-Drama", "Thriller", "Misteri", "Sub Indo"),
                isSeries = true,
                totalEpisodes = 12,
                releasedEpisodes = 12,
                rating = 4.7,
                viewsCount = "4.9M",
                hasNewEpisode = false,
                releaseSchedule = "Lengkap 12 Episode",
                cast = listOf("Kento Yamazaki", "Tao Tsuchiya"),
                episodes = generateEpisodesForMisteriMovie(),
                country = "Jepang",
                countryFlag = "🇯🇵"
            ),
            Drama(
                id = "us_billionaire_alpha",
                title = "Pesona Sang Alpha Triliuner",
                titleAlt = "The Billionaire Alpha's Vow",
                synopsis = "Gaya khas serial pendek viral ReelShort: Gadis pelayan biasa di Manhattan ternyata adalah belahan jiwa seorang CEO triliuner berdarah bangsawan terkuat di New York.",
                posterRes = R.drawable.poster_ceo_dingin_1792425093218,
                genre = "Romance",
                tags = listOf("Amerika", "Western", "Romance", "Billionaire", "Romantis", "Sub Indo"),
                isSeries = true,
                totalEpisodes = 25,
                releasedEpisodes = 21,
                rating = 4.8,
                viewsCount = "13.6M",
                hasNewEpisode = true,
                releaseSchedule = "Kamis & Minggu 19.00 WIB",
                cast = listOf("Chris Evans", "Lily Collins"),
                episodes = generateEpisodesForCeoDingin(),
                country = "Amerika",
                countryFlag = "🇺🇸"
            )
        )
        _dramas.value = dramaList

        // Initial notifications list
        _notifications.value = listOf(
            DramaNotification(
                id = "notif_1",
                dramaId = "ceo_dingin",
                dramaTitle = "CEO Dingin Jatuh Cinta",
                episodeNumber = 19,
                title = "Episode 19 Baru Saja Rilis!",
                message = "Kevin akhirnya mengetahui alasan Sarah pergi 5 tahun lalu! Tonton pertengkaran emosional dengan Subtitle Indonesia sekarang.",
                timeAgo = "15 menit yang lalu",
                isRead = false,
                posterRes = R.drawable.poster_ceo_dingin_1792425093218,
                type = NotificationType.NEW_EPISODE
            ),
            DramaNotification(
                id = "notif_2",
                dramaId = "miliarder_ob",
                dramaTitle = "Miliarder Menyamar Jadi OB",
                episodeNumber = 25,
                title = "Pembaruan: Episode 25 Tayang!",
                message = "Identitas asli Arga di depan manajer arogan terbongkar saat kartu hitam VIP dijatuhkan! Tonton gratis Sub Indo.",
                timeAgo = "2 jam yang lalu",
                isRead = false,
                posterRes = R.drawable.poster_miliarder_ob_1792425094049,
                type = NotificationType.NEW_EPISODE
            ),
            DramaNotification(
                id = "notif_3",
                dramaId = "ratu_bisnis",
                dramaTitle = "Kembalinya Sang Ratu Bisnis",
                episodeNumber = 14,
                title = "Episode 14 Tersedia Sub Indo",
                message = "Clarissa mengambil alih ruang rapat keluarga! Saham Astra Holdings melambung tinggi.",
                timeAgo = "Kemarin 18:30 WIB",
                isRead = true,
                posterRes = R.drawable.poster_ratu_bisnis_1792425113858,
                type = NotificationType.NEW_EPISODE
            ),
            DramaNotification(
                id = "notif_4",
                dramaId = "misteri_apartemen",
                dramaTitle = "Misteri Apartemen 404",
                episodeNumber = 1,
                title = "Film Pendek Baru: Misteri Apartemen 404",
                message = "Minimovie thriller viral kini telah hadir lengkap berdurasi 45 menit dengan teks terjemahan Bahasa Indonesia jernih.",
                timeAgo = "2 hari yang lalu",
                isRead = true,
                posterRes = R.drawable.poster_ceo_dingin_1792425093218,
                type = NotificationType.NEW_SERIES
            )
        )

        // Seed initial comments
        _comments.value = mapOf(
            "ceo_dingin_19" to listOf(
                DramaComment("c1", "ceo_dingin", 19, "Rina Amelia", "RA", "Gila plot twist banget pas Pak Kevin nemuin kalung Sarah!! 😭❤️", "10 mnt lalu", 142),
                DramaComment("c2", "ceo_dingin", 19, "Dimas Pratama", "DP", "Sub indonya cepet banget update nya, makasih Dramaku!", "25 mnt lalu", 89),
                DramaComment("c3", "ceo_dingin", 19, "Siti Rahma", "SR", "Nggak sabar nunggu episode 20 besok jam 19.00 WIB 🔥", "1 jam lalu", 45)
            ),
            "miliarder_ob_25" to listOf(
                DramaComment("c4", "miliarder_ob", 25, "Budi Santoso", "BS", "Puas banget liat muka manajernya pucat pas tau Mas Arga yang punya gedung wkwk", "1 jam lalu", 230),
                DramaComment("c5", "miliarder_ob", 25, "Anisa Putri", "AP", "Seru pol episode 25 ini! Jangan lupa aktifin lonceng notif ya gaes!", "2 jam lalu", 74)
            )
        )
    }

    private fun generateEpisodesForCeoDingin(): List<Episode> {
        val subtitlesEp19 = listOf(
            SubtitleItem(0, 4, "Kevin", "Sarah... jangan berpura-pura tidak mengenalku lagi.", "Sarah... don't pretend you don't know me anymore."),
            SubtitleItem(5, 9, "Sarah", "Pak Kevin, tolong jaga profesionalitas. Kita hanya rekan kerja di kantor ini.", "Mr. Kevin, please maintain professionalism. We are only colleagues."),
            SubtitleItem(10, 15, "Kevin", "Rekan kerja?! Lalu siapa pria yang memelukmu di lobi kemarin malam?!", "Colleagues?! Then who was the man embracing you in the lobby last night?!"),
            SubtitleItem(16, 21, "Sarah", "Itu bukan urusanmu! Lima tahun lalu kamu yang memutuskan untuk pergi!", "That's none of your business! Five years ago you were the one who walked away!"),
            SubtitleItem(22, 28, "Kevin", "Aku tidak pernah meninggalkanmu karena keinginanku sendiri, Sarah...", "I never left you by my own choice, Sarah..."),
            SubtitleItem(29, 36, "Sarah", "Lalu apa surat pelepasan warisan itu?! Semuanya tertulis jelas!", "Then what was that inheritance waiver letter?! Everything was written clearly!"),
            SubtitleItem(37, 44, "Kevin", "Kakek memalsukan tanda tanganku! Aku mencarimu ke seluruh penjuru negeri!", "Grandfather forged my signature! I searched the entire country for you!"),
            SubtitleItem(45, 52, "Narator", "[Bersambung ke Episode 20: Rahasia Surat Perjanjian]", "[To be continued in Episode 20: The Secret Contract]")
        )

        return (1..24).map { epNum ->
            val isNew = (epNum == 19)
            val isFree = epNum <= 10
            Episode(
                id = "ceo_dingin_ep_$epNum",
                episodeNumber = epNum,
                title = if (epNum == 19) "Kebenaran Surat Wasiat Terbongkar" else "Pertemuan Tak Terduga Bagian $epNum",
                durationSeconds = 95,
                isFree = isFree,
                isNew = isNew,
                previewSnippet = if (epNum == 19) "Kevin menyudutkan Sarah di ruang rapat dan mengungkap kebenaran lima tahun lalu." else "Konflik memanas antara sang pewaris tahta dan asisten barunya.",
                subtitles = subtitlesEp19,
                sceneVisual = DramaSceneVisual(
                    primaryColorHex = 0xFF161A29,
                    secondaryColorHex = 0xFFFF2D55,
                    sceneDescription = "Ruang Kantor Penthouse Lantai 68 - Hujan Malam Hari",
                    moodIcon = "🏢"
                )
            )
        }
    }

    private fun generateEpisodesForMiliarderOB(): List<Episode> {
        val subtitlesEp25 = listOf(
            SubtitleItem(0, 4, "Manajer Reno", "Hei OB kampung! Kamu berani menumpahkan air di sepatu mahalku?!", "Hey village cleaner! You dare spill water on my expensive shoes?!"),
            SubtitleItem(5, 9, "Arga", "Maaf Pak Reno, bapak yang menabrak ember saya saat sedang menelepon.", "Forgive me Mr. Reno, you bumped into my bucket while on the phone."),
            SubtitleItem(10, 16, "Manajer Reno", "Berani membantah?! Bersihkan dengan dasimu atau kupecat hari ini juga!", "Talking back?! Clean it with your tie or you are fired right now!"),
            SubtitleItem(17, 23, "Sekretaris Dirut", "Tunggu sebentar! Pak Reno, kenapa Anda berteriak pada Presiden Komisaris Arga?!", "Wait a moment! Mr. Reno, why are you yelling at Board Chairman Arga?!"),
            SubtitleItem(24, 30, "Manajer Reno", "A... apa?! OB kumal ini... Presiden Komisaris utama Mega Group?!", "Wh... what?! This scrub cleaner is... the supreme Chairman of Mega Group?!"),
            SubtitleItem(31, 38, "Arga", "Pak Reno, mulai besok Anda yang akan menggantikan posisiku mengepel lantai.", "Mr. Reno, starting tomorrow you will take my place mopping these floors."),
            SubtitleItem(39, 48, "Narator", "[Bersambung ke Episode 26: Pembersihan Total Dewan Direksi]", "[To be continued in Episode 26: Total Boardroom Purge]")
        )

        return (1..30).map { epNum ->
            val isNew = (epNum == 25)
            val isFree = epNum <= 12
            Episode(
                id = "miliarder_ob_ep_$epNum",
                episodeNumber = epNum,
                title = if (epNum == 25) "Kartu Hitam VIP Terjatuh di Lantai" else "Penyamaran di Lantai 1 Bagian $epNum",
                durationSeconds = 100,
                isFree = isFree,
                isNew = isNew,
                previewSnippet = if (epNum == 25) "Manajer Reno menuntut ganti rugi, namun sekretaris pusat tiba-tiba membungkuk 90 derajat." else "Arga mengamati gerak-gerik korupsi di balik seragam cleaning service.",
                subtitles = subtitlesEp25,
                sceneVisual = DramaSceneVisual(
                    primaryColorHex = 0xFF14201E,
                    secondaryColorHex = 0xFFFFB800,
                    sceneDescription = "Lobi Marmer Mega Corp - Siang Hari",
                    moodIcon = "👔"
                )
            )
        }
    }

    private fun generateEpisodesForRatuBisnis(): List<Episode> {
        val subtitlesEp14 = listOf(
            SubtitleItem(0, 5, "Paman Felix", "Clarissa! Berani sekali kamu menginjakkan kaki di ruang rapat keluarga kami!", "Clarissa! How dare you step foot inside our family boardroom!"),
            SubtitleItem(6, 11, "Clarissa", "Keluarga kalian? Bukankah nama saya ada di lembar kepemilikan 70% saham?", "Your family? Isn't my name on the 70% controlling share deed?"),
            SubtitleItem(12, 19, "Paman Felix", "Mustahil! Perusahaan ini sudah kami amankan sejak kamu kabur ke Paris!", "Impossible! We secured this company ever since you ran away to Paris!"),
            SubtitleItem(20, 27, "Clarissa", "Kabur? Saya kembali bukan untuk meminta, tapi mengambil semua yang kalian curi.", "Ran away? I returned not to beg, but to reclaim everything you stole."),
            SubtitleItem(28, 38, "Narator", "[Bersambung ke Episode 15: Penangkapan Paman Felix]", "[To be continued in Episode 15: Arrest of Uncle Felix]")
        )

        return (1..18).map { epNum ->
            Episode(
                id = "ratu_bisnis_ep_$epNum",
                episodeNumber = epNum,
                title = if (epNum == 14) "Pengambilalihan Ruang Rapat Direksi" else "Langkah Sang Ratu Bagian $epNum",
                durationSeconds = 90,
                isFree = epNum <= 8,
                isNew = epNum == 14,
                previewSnippet = "Clarissa mengumumkan pembatalan seluruh keputusan direksi lama.",
                subtitles = subtitlesEp14,
                sceneVisual = DramaSceneVisual(
                    primaryColorHex = 0xFF231826,
                    secondaryColorHex = 0xFFE040FB,
                    sceneDescription = "Ruang Rapat Direksi Mewah - Senja Hari",
                    moodIcon = "👑"
                )
            )
        }
    }

    private fun generateEpisodesForMisteriMovie(): List<Episode> {
        val subtitlesMovie = listOf(
            SubtitleItem(0, 5, "Naya", "Apartemen ini murah sekali... padahal di lantai empat dengan pemandangan kota.", "This apartment is so cheap... even on the 4th floor with city views."),
            SubtitleItem(6, 12, "Penjaga Gedung", "Ingat ya neng, kalau jam tiga pagi pintu diketuk, jangan pernah dibuka.", "Remember miss, if the door knocks at 3 AM, never open it."),
            SubtitleItem(13, 20, "Naya", "Kenapa begitu, Pak? Apakah ada tetangga yang sering jahil?", "Why is that, Sir? Is there a prankster neighbor?"),
            SubtitleItem(21, 29, "Penjaga Gedung", "Bukan tetangga... penghuni kamar 404 yang lama belum pernah benar-benar pindah.", "Not a neighbor... the old resident of 404 never truly moved out."),
            SubtitleItem(30, 42, "Naya", "[Suara ketukan pintu: Tok... Tok... Tok... jam 03.15]", "[Knocking on door: Knock... Knock... Knock... 03:15 AM]")
        )

        return listOf(
            Episode(
                id = "misteri_404_full",
                episodeNumber = 1,
                title = "Film Penuh: Misteri Apartemen 404",
                durationSeconds = 120,
                isFree = true,
                isNew = false,
                previewSnippet = "Film pendek misteri horor psikologis berdurasi penuh dengan terjemahan Bahasa Indonesia lengkap.",
                subtitles = subtitlesMovie,
                sceneVisual = DramaSceneVisual(
                    primaryColorHex = 0xFF11141E,
                    secondaryColorHex = 0xFF00E5FF,
                    sceneDescription = "Koridor Apartemen Tua Lampu Berkedip",
                    moodIcon = "🕯️"
                )
            )
        )
    }

    private fun generateEpisodesForDewaPerang(): List<Episode> {
        val subs = listOf(
            SubtitleItem(0, 4, "Tuan Muda Liu", "Gelandangan! Beraninya kamu menginjak aula perjamuan keluarga Liu?!", "Tramp! How dare you step foot inside the Liu family banquet hall?!"),
            SubtitleItem(5, 10, "Ye Chen", "Lepaskan tangan kotormu dari putriku, atau seluruh kota ini akan meratakan keluargamu.", "Take your filthy hands off my daughter, or this entire city will raze your family."),
            SubtitleItem(11, 16, "Tuan Muda Liu", "Hahaha! Membual di depanku?! Pengawal, patahkan kedua kakinya sekarang!", "Hahaha! Boasting before me?! Guards, break both his legs right now!"),
            SubtitleItem(17, 23, "Jenderal Wu", "Berhenti! Siapa yang berani lancang di hadapan Panglima Tertinggi Dewa Perang?!", "Halt! Who dares be insolent before the Supreme God of War?!"),
            SubtitleItem(24, 32, "Tuan Muda Liu", "P... Panglima Tertinggi?! Pria berkain rombeng ini adalah Dewa Perang legendaris?!", "S... Supreme Commander?! This ragged man is the legendary God of War?!"),
            SubtitleItem(33, 44, "Ye Chen", "Keluarga Liu, hukuman kalian baru saja dimulai.", "Liu family, your retribution has only just begun.")
        )
        return (1..32).map { epNum ->
            Episode(
                id = "dewa_perang_ep_$epNum",
                episodeNumber = epNum,
                title = if (epNum == 28) "Dewa Perang Menggetarkan Aula Perjamuan" else "Langkah Sang Dewa Perang Bagian $epNum",
                durationSeconds = 95,
                isFree = epNum <= 10,
                isNew = epNum == 28,
                previewSnippet = "Ye Chen melindungi putrinya dan mengungkap lencana emas Panglima Tertinggi.",
                subtitles = subs,
                sceneVisual = DramaSceneVisual(
                    primaryColorHex = 0xFF2A1010,
                    secondaryColorHex = 0xFFFF2D55,
                    sceneDescription = "Gerbang Markas Militer & Aula Perjamuan Mewah",
                    moodIcon = "⚔️"
                )
            )
        }
    }

    private fun generateEpisodesForKoreaIntern(): List<Episode> {
        val subs = listOf(
            SubtitleItem(0, 5, "Direktur Park", "Kim Min-jun, anak magang bodoh sepertimu tidak berhak memberikan usulan!", "Kim Min-jun, a foolish intern like you has no right to make proposals!"),
            SubtitleItem(6, 12, "Min-jun", "Direktur Park, bukankah proposal ini yang akan membuat perusahaan bangkrut dalam 3 bulan?", "Director Park, isn't this the very proposal that will bankrupt the company in 3 months?"),
            SubtitleItem(13, 20, "Direktur Park", "A... apa katamu?! Dari mana kamu tahu kalkulasi rahasia itu?!", "Wh... what did you say?! How do you know that secret calculation?!"),
            SubtitleItem(21, 29, "Min-jun", "(Dalam hati: Karena di kehidupanku yang lalu, kamu mengkambinghitamkanku untuk kebangkrutan ini.)", "(Inner monologue: Because in my past life, you framed me for this collapse.)"),
            SubtitleItem(30, 42, "Presiden Grup", "Min-jun, mulai besok Anda resmi diangkat menjadi penasihat strategi utama dewan direksi!", "Min-jun, effective tomorrow you are appointed Chief Strategy Advisor to the board!")
        )
        return (1..20).map { epNum ->
            Episode(
                id = "korea_intern_ep_$epNum",
                episodeNumber = epNum,
                title = if (epNum == 16) "Kembalinya Sang CEO Jenius di Tubuh Magang" else "Langkah Balas Dendam Chaebol Ep $epNum",
                durationSeconds = 90,
                isFree = epNum <= 8,
                isNew = epNum == 16,
                previewSnippet = "Min-jun menggunakan pengetahuan masa depan untuk membalikkan posisi direksi korup.",
                subtitles = subs,
                sceneVisual = DramaSceneVisual(
                    primaryColorHex = 0xFF101B2B,
                    secondaryColorHex = 0xFF00E5FF,
                    sceneDescription = "Gedung Pencakar Langit Gangnam Seoul - Malam Hari",
                    moodIcon = "🏢"
                )
            )
        }
    }

    private fun generateEpisodesForThaiLakorn(): List<Episode> {
        val subs = listOf(
            SubtitleItem(0, 5, "Nyonya Malai", "Prim! Kamu pikir dengan kembali ke pulau ini kamu bisa merebut warisan ayahmu?!", "Prim! You think coming back to this island lets you claim your father's inheritance?!"),
            SubtitleItem(6, 12, "Prim", "Nyonya Malai, saya tidak datang untuk merebut, saya datang mengambil hak sah saya.", "Lady Malai, I did not come to seize, I came to take what is legally mine."),
            SubtitleItem(13, 22, "Nyonya Malai", "Sombong sekali! Pengacara kami sudah mengesahkan seluruh surat wasiat!", "How arrogant! Our lawyers have already certified the entire will!"),
            SubtitleItem(23, 35, "Prim", "Surat wasiat palsu itu? Hakim tinggi kepulauan sudah membatalkannya pagi ini.", "That forged will? The island high court annulled it this morning.")
        )
        return (1..24).map { epNum ->
            Episode(
                id = "thai_lakorn_ep_$epNum",
                episodeNumber = epNum,
                title = if (epNum == 20) "Pengambilalihan Pulau Mutiara Andaman" else "Intrik Warisan Pulau Ep $epNum",
                durationSeconds = 95,
                isFree = epNum <= 10,
                isNew = epNum == 20,
                previewSnippet = "Prim membongkar konspirasi surat wasiat palsu di hadapan seluruh tetua pulau.",
                subtitles = subs,
                sceneVisual = DramaSceneVisual(
                    primaryColorHex = 0xFF1D2610,
                    secondaryColorHex = 0xFFFFD700,
                    sceneDescription = "Vila Mewah Tepi Pantai Phuket Thailand",
                    moodIcon = "🏝️"
                )
            )
        }
    }

    // Interactive Actions with Firestore synchronization
    fun toggleSubscription(dramaId: String) {
        val current = _subscribedDramaIds.value
        val drama = _dramas.value.find { it.id == dramaId } ?: return
        val isNowSubscribed = !current.contains(dramaId)

        if (isNowSubscribed) {
            _subscribedDramaIds.value = current + dramaId
            NotificationHelper.sendEpisodeNotification(
                context = context,
                dramaId = dramaId,
                dramaTitle = drama.title,
                episodeNumber = drama.releasedEpisodes,
                episodeTitle = "Lonceng Diaktifkan",
                customMessage = "Anda sekarang mengikuti '${drama.title}'. Pemberitahuan episode baru akan dikirim secara otomatis!"
            )
        } else {
            _subscribedDramaIds.value = current - dramaId
        }

        // Sync with Firestore if user is authenticated
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val docRef = db.collection("users").document(uid).collection("subscriptions").document(dramaId)
            if (isNowSubscribed) {
                val data = mapOf(
                    "dramaId" to dramaId,
                    "title" to drama.title,
                    "releaseSchedule" to drama.releaseSchedule,
                    "subscribedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.WRITE, docRef.path)
                }
            } else {
                docRef.delete().addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.DELETE, docRef.path)
                }
            }
        }
    }

    fun toggleBookmark(dramaId: String) {
        val current = _bookmarkedDramaIds.value
        val drama = _dramas.value.find { it.id == dramaId }
        val isNowBookmarked = !current.contains(dramaId)

        _bookmarkedDramaIds.value = if (isNowBookmarked) {
            current + dramaId
        } else {
            current - dramaId
        }

        // Sync with Firestore if user is authenticated
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val docRef = db.collection("users").document(uid).collection("bookmarks").document(dramaId)
            if (isNowBookmarked) {
                val data = mapOf(
                    "dramaId" to dramaId,
                    "title" to (drama?.title ?: dramaId),
                    "addedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.WRITE, docRef.path)
                }
            } else {
                docRef.delete().addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.DELETE, docRef.path)
                }
            }
        }
    }

    fun toggleLikeEpisode(episodeId: String) {
        val current = _likedEpisodeIds.value
        _likedEpisodeIds.value = if (current.contains(episodeId)) {
            current - episodeId
        } else {
            current + episodeId
        }
    }

    fun updateWatchProgress(dramaId: String, episodeNumber: Int) {
        val map = _watchHistory.value.toMutableMap()
        map[dramaId] = episodeNumber
        _watchHistory.value = map

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val docRef = db.collection("users").document(uid).collection("history").document(dramaId)
            val data = mapOf(
                "dramaId" to dramaId,
                "lastEpisode" to episodeNumber,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).addOnFailureListener { error ->
                handleFirestoreError(error, OperationType.WRITE, docRef.path)
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun addComment(
        dramaId: String,
        episodeNumber: Int,
        text: String,
        userName: String = auth.currentUser?.displayName ?: "Penonton Setia"
    ) {
        val key = "${dramaId}_$episodeNumber"
        val existing = _comments.value[key] ?: emptyList()
        val commentId = "c_${System.currentTimeMillis()}"
        val newComment = DramaComment(
            id = commentId,
            dramaId = dramaId,
            episodeNumber = episodeNumber,
            userName = userName,
            avatarInitials = userName.take(2).uppercase(),
            text = text,
            timeAgo = "Baru saja",
            likesCount = 1,
            isLiked = true
        )
        val updatedMap = _comments.value.toMutableMap()
        updatedMap[key] = listOf(newComment) + existing
        _comments.value = updatedMap

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val commentsRef = db.collection("comments").document(commentId)
            val payload = mapOf(
                "userId" to uid,
                "userName" to userName,
                "dramaId" to dramaId,
                "episodeNumber" to episodeNumber,
                "text" to text,
                "likesCount" to 1,
                "createdAt" to FieldValue.serverTimestamp()
            )
            commentsRef.set(payload).addOnFailureListener { error ->
                handleFirestoreError(error, OperationType.CREATE, commentsRef.path)
            }
        }
    }

    fun updateSubtitleConfig(language: SubtitleLanguage? = null, fontSizeSp: Int? = null, opacity: Float? = null) {
        _subtitleConfig.update { cur ->
            cur.copy(
                language = language ?: cur.language,
                fontSizeSp = fontSizeSp ?: cur.fontSizeSp,
                backgroundOpacity = opacity ?: cur.backgroundOpacity
            )
        }
    }

    fun triggerSimulateNewEpisodeRelease(dramaId: String) {
        val drama = _dramas.value.find { it.id == dramaId } ?: return
        val nextEpNumber = drama.releasedEpisodes + 1
        val episodeTitle = "Episode $nextEpNumber: Pertaruhan Terakhir"

        _dramas.update { list ->
            list.map { d ->
                if (d.id == dramaId) {
                    d.copy(
                        releasedEpisodes = nextEpNumber,
                        hasNewEpisode = true
                    )
                } else d
            }
        }

        val newNotif = DramaNotification(
            id = "notif_${System.currentTimeMillis()}",
            dramaId = dramaId,
            dramaTitle = drama.title,
            episodeNumber = nextEpNumber,
            title = "Episode $nextEpNumber Rilis: ${drama.title}",
            message = "Episode terbaru '$episodeTitle' sudah tayang dengan Subtitle Indonesia lengkap! Tonton sekarang.",
            timeAgo = "Baru saja",
            isRead = false,
            posterRes = drama.posterRes,
            type = NotificationType.NEW_EPISODE
        )
        _notifications.update { listOf(newNotif) + it }

        NotificationHelper.sendEpisodeNotification(
            context = context,
            dramaId = dramaId,
            dramaTitle = drama.title,
            episodeNumber = nextEpNumber,
            episodeTitle = episodeTitle
        )
    }
}
