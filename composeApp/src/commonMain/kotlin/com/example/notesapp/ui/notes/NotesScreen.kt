package com.example.notesapp.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.components.CategoryChip
import com.example.notesapp.ui.components.NoteCard
import com.example.notesapp.ui.components.SearchBar
import com.example.notesapp.util.TestTags
import org.koin.compose.viewmodel.koinViewModel

/**
 * Layar utama yang menampilkan daftar semua catatan.
 *
 * Fitur:
 * - Daftar catatan dengan kartu yang bisa diklik
 * - Search bar untuk mencari catatan
 * - Chip filter kategori
 * - FAB untuk menambah catatan baru
 * - State kosong saat belum ada catatan
 *
 * @param onNavigateToDetail Dipanggil saat pengguna ingin melihat/edit catatan
 * @param onNavigateToBuat   Dipanggil saat pengguna menekan FAB tambah
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToBuat: () -> Unit,
    viewModel: NotesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catatan Saya",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            // Tombol mengambang untuk menambah catatan baru
            FloatingActionButton(
                onClick = onNavigateToBuat,
                modifier = Modifier.testTag(TestTags.TOMBOL_TAMBAH)
            ) {
                Text(text = "+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tampilkan konten sesuai state
            when (val state = uiState) {

                is NotesUiState.Loading -> {
                    // Indikator loading di tengah layar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestTags.LOADING_INDICATOR),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is NotesUiState.Error -> {
                    // Tampilan pesan error
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestTags.PESAN_ERROR),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.pesan,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is NotesUiState.Success -> {
                    // Search bar
                    SearchBar(
                        query = state.queryPencarian,
                        onQueryChange = { viewModel.cariNote(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Chip filter kategori (ditampilkan jika ada kategori)
                    if (state.daftarKategori.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Chip "Semua" untuk mereset filter
                            item {
                                CategoryChip(
                                    nama = "Semua",
                                    dipilih = state.kategoriDipilih == null,
                                    onClick = { viewModel.filterByKategori(null) }
                                )
                            }
                            // Chip untuk setiap kategori yang ada
                            items(state.daftarKategori) { kategori ->
                                CategoryChip(
                                    nama = kategori,
                                    dipilih = state.kategoriDipilih == kategori,
                                    onClick = { viewModel.filterByKategori(kategori) }
                                )
                            }
                        }
                    }

                    // Tampilkan daftar catatan atau state kosong
                    if (state.notes.isEmpty()) {
                        // State kosong saat tidak ada catatan
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(TestTags.STATE_KOSONG),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📝",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Text(
                                    text = "Belum ada catatan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tekan + untuk menambah catatan baru",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Daftar catatan
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag(TestTags.DAFTAR_CATATAN)
                        ) {
                            items(
                                items = state.notes,
                                key = { note -> note.id }
                            ) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onNavigateToDetail(note.id) },
                                    onHapus = { viewModel.hapusNote(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
