package com.example.notesapp.util

/**
 * Konstanta test tag untuk semua elemen UI yang diuji.
 *
 * Digunakan dengan Modifier.testTag(TestTags.XXX) pada Composable
 * dan composeTestRule.onNodeWithTag(TestTags.XXX) pada UI test.
 */
object TestTags {
    /** Tag untuk LazyColumn daftar catatan di NotesScreen */
    const val DAFTAR_CATATAN = "daftar_catatan"

    /** Tag untuk setiap item kartu catatan (digunakan dengan index) */
    const val ITEM_CATATAN = "item_catatan"

    /** Tag untuk input judul di form tambah/edit catatan */
    const val INPUT_JUDUL = "input_judul"

    /** Tag untuk input isi di form tambah/edit catatan */
    const val INPUT_ISI = "input_isi"

    /** Tag untuk input atau selector kategori */
    const val INPUT_KATEGORI = "input_kategori"

    /** Tag untuk FAB atau tombol tambah catatan baru */
    const val TOMBOL_TAMBAH = "tombol_tambah"

    /** Tag untuk tombol simpan di form tambah/edit catatan */
    const val TOMBOL_SIMPAN = "tombol_simpan"

    /** Tag untuk tombol hapus di kartu atau detail catatan */
    const val TOMBOL_HAPUS = "tombol_hapus"

    /** Tag untuk komponen search bar di layar daftar */
    const val SEARCH_BAR = "search_bar"

    /** Tag untuk chip filter kategori */
    const val CHIP_KATEGORI = "chip_kategori"

    /** Tag untuk tampilan saat daftar catatan kosong */
    const val STATE_KOSONG = "state_kosong"

    /** Tag untuk indikator loading */
    const val LOADING_INDICATOR = "loading_indicator"

    /** Tag untuk pesan error */
    const val PESAN_ERROR = "pesan_error"
}
