package com.example.notesapp.data.model

/**
 * Helper multiplatform untuk mendapatkan waktu saat ini dalam milidetik.
 * Menggantikan System.currentTimeMillis() yang hanya tersedia di JVM/Android.
 * Di Kotlin Multiplatform, kita menggunakan kotlin.time.TimeSource atau
 * platform.posix.time (iOS). Solusi paling sederhana: gunakan kotlin.time.Clock.
 */
internal fun currentTimeMs(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

/**
 * Data class yang merepresentasikan satu catatan dalam aplikasi.
 *
 * @param id          Identitas unik catatan (auto-generate dari database)
 * @param judul       Judul catatan, tidak boleh kosong
 * @param isi         Isi/konten catatan, tidak boleh kosong
 * @param kategori    Kategori/tag catatan, default "Umum"
 * @param tanggalDibuat  Timestamp (ms) saat catatan pertama kali dibuat
 * @param tanggalDiubah  Timestamp (ms) saat catatan terakhir diubah
 */
data class Note(
    val id: Long = 0,
    val judul: String,
    val isi: String,
    val kategori: String = "Umum",
    val tanggalDibuat: Long = currentTimeMs(),
    val tanggalDiubah: Long = currentTimeMs()
)
