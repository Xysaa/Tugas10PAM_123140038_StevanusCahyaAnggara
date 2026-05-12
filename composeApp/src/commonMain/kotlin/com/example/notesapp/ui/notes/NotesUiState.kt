package com.example.notesapp.ui.notes

import com.example.notesapp.data.model.Note

/**
 * Sealed class yang merepresentasikan semua kemungkinan state
 * pada layar daftar catatan (NotesScreen).
 *
 * Pola ini memastikan UI selalu berada dalam salah satu state
 * yang terdefinisi dengan baik.
 */
sealed class NotesUiState {

    /** State saat data sedang dimuat dari database */
    object Loading : NotesUiState()

    /**
     * State saat data berhasil dimuat dan siap ditampilkan.
     *
     * @param notes           Daftar catatan yang akan ditampilkan
     * @param queryPencarian  Kata kunci pencarian aktif (kosong = tidak ada filter)
     * @param kategoriDipilih Nama kategori yang sedang difilter (null = semua kategori)
     * @param daftarKategori  Semua kategori yang tersedia untuk chip filter
     */
    data class Success(
        val notes: List<Note>,
        val queryPencarian: String = "",
        val kategoriDipilih: String? = null,
        val daftarKategori: List<String> = emptyList()
    ) : NotesUiState()

    /**
     * State saat terjadi kesalahan.
     *
     * @param pesan Pesan kesalahan yang ditampilkan ke pengguna
     */
    data class Error(val pesan: String) : NotesUiState()
}
