package com.example.notesapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.notesapp.ui.detail.NoteDetailScreen
import com.example.notesapp.ui.notes.NotesScreen

/**
 * Sealed class sederhana untuk navigasi antar layar.
 * Menggantikan Navigation library agar tetap multiplatform.
 */
sealed class AppDestination {
    /** Layar utama daftar catatan */
    object DaftarCatatan : AppDestination()

    /** Layar tambah catatan baru */
    object BuatCatatan : AppDestination()

    /**
     * Layar edit catatan yang sudah ada.
     * @param noteId Id catatan yang akan diedit
     */
    data class EditCatatan(val noteId: Long) : AppDestination()
}

/**
 * Entry point utama aplikasi.
 *
 * Mengatur navigasi antar layar menggunakan state sederhana.
 * Koin ViewModel tersedia otomatis melalui koinViewModel().
 */
@Composable
fun App() {
    MaterialTheme {
        // State navigasi: lacak layar mana yang sedang aktif
        var destinasiAktif by remember {
            mutableStateOf<AppDestination>(AppDestination.DaftarCatatan)
        }

        when (val dest = destinasiAktif) {
            is AppDestination.DaftarCatatan -> {
                NotesScreen(
                    onNavigateToDetail = { noteId ->
                        destinasiAktif = AppDestination.EditCatatan(noteId)
                    },
                    onNavigateToBuat = {
                        destinasiAktif = AppDestination.BuatCatatan
                    }
                )
            }

            is AppDestination.BuatCatatan -> {
                NoteDetailScreen(
                    noteId = null,
                    onNavigateBack = {
                        destinasiAktif = AppDestination.DaftarCatatan
                    }
                )
            }

            is AppDestination.EditCatatan -> {
                NoteDetailScreen(
                    noteId = dest.noteId,
                    onNavigateBack = {
                        destinasiAktif = AppDestination.DaftarCatatan
                    }
                )
            }
        }
    }
}
