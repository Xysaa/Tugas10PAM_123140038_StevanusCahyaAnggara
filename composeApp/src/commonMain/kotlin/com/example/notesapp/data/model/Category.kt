package com.example.notesapp.data.model

/**
 * Data class yang merepresentasikan kategori/tag untuk catatan.
 *
 * @param id    Identitas unik kategori
 * @param nama  Nama kategori yang ditampilkan ke pengguna
 * @param warna Warna hex untuk tampilan chip kategori, default hijau Material
 */
data class Category(
    val id: Long = 0,
    val nama: String,
    val warna: String = "#4CAF50"
) {
    companion object {
        /** Daftar kategori default yang tersedia di aplikasi */
        val DEFAULT_CATEGORIES = listOf(
            Category(id = 1, nama = "Umum",     warna = "#4CAF50"),
            Category(id = 2, nama = "Pekerjaan", warna = "#2196F3"),
            Category(id = 3, nama = "Pribadi",   warna = "#FF9800"),
            Category(id = 4, nama = "Ide",       warna = "#9C27B0"),
            Category(id = 5, nama = "Penting",   warna = "#F44336")
        )
    }
}
