package com.example.notesapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepository
import com.example.notesapp.data.validation.NoteValidator
import com.example.notesapp.ui.notes.NotesScreen
import com.example.notesapp.ui.notes.NotesViewModel
import com.example.notesapp.util.TestTags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// ----------------------------------------------------------------
// FakeRepository untuk UI test — independen dari database nyata
// ----------------------------------------------------------------
class FakeRepositoryForUiTest : NoteRepository {

    private val _catatan = MutableStateFlow<List<Note>>(emptyList())
    private var idCounter = 1L

    override fun getAllNotes(): Flow<List<Note>> =
        _catatan.map { it.sortedByDescending { n -> n.tanggalDiubah } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        _catatan.map { list ->
            list.filter { it.judul.contains(query, true) || it.isi.contains(query, true) }
        }

    override fun getNotesByKategori(kategori: String): Flow<List<Note>> =
        _catatan.map { list -> list.filter { it.kategori == kategori } }

    override suspend fun getNoteById(id: Long): Note? =
        _catatan.value.find { it.id == id }

    override suspend fun insertNote(note: Note) {
        _catatan.update { it + note.copy(id = idCounter++) }
    }

    override suspend fun updateNote(note: Note) {
        _catatan.update { list -> list.map { if (it.id == note.id) note else it } }
    }

    override suspend fun deleteNote(id: Long) {
        _catatan.update { list -> list.filter { it.id != id } }
    }

    override fun getAllKategori(): Flow<List<String>> =
        _catatan.map { it.map { n -> n.kategori }.distinct().sorted() }
}

// ================================================================
// UI Test: NotesScreen menggunakan Compose Test
// Min. 3 test cases — menggunakan TestTags untuk menemukan elemen
// ================================================================
@RunWith(AndroidJUnit4::class)
class NotesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ----------------------------------------------------------------
    // TC-01: State kosong menampilkan pesan "Belum ada catatan"
    // ----------------------------------------------------------------
    @Test
    fun `TC-01 state kosong menampilkan teks Belum ada catatan`() {
        // Arrange: buat ViewModel dengan repository kosong
        val fakeRepo  = FakeRepositoryForUiTest()
        val viewModel = NotesViewModel(fakeRepo, NoteValidator())

        // Act: tampilkan NotesScreen dengan ViewModel yang sudah disiapkan
        composeTestRule.setContent {
            NotesScreen(
                onNavigateToDetail = {},
                onNavigateToBuat   = {},
                viewModel          = viewModel
            )
        }

        // Tunggu semua animasi dan loading selesai
        composeTestRule.waitForIdle()

        // Assert: elemen state kosong harus tampil
        composeTestRule
            .onNodeWithTag(TestTags.STATE_KOSONG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Belum ada catatan")
            .assertIsDisplayed()
    }

    // ----------------------------------------------------------------
    // TC-02: Catatan yang sudah ada ditampilkan dalam daftar
    // ----------------------------------------------------------------
    @Test
    fun `TC-02 daftar catatan menampilkan item yang sudah tersimpan`() {
        // Arrange: buat ViewModel dengan data awal
        val fakeRepo  = FakeRepositoryForUiTest()
        val viewModel = NotesViewModel(fakeRepo, NoteValidator())

        // Tambah catatan langsung melalui ViewModel
        viewModel.tambahNote(
            judul    = "Catatan Pertama",
            isi      = "Isi catatan pertama",
            kategori = "Umum"
        )
        viewModel.tambahNote(
            judul    = "Catatan Kedua",
            isi      = "Isi catatan kedua",
            kategori = "Pekerjaan"
        )

        // Act: tampilkan layar
        composeTestRule.setContent {
            NotesScreen(
                onNavigateToDetail = {},
                onNavigateToBuat   = {},
                viewModel          = viewModel
            )
        }
        composeTestRule.waitForIdle()

        // Assert: daftar catatan harus tampil (bukan state kosong)
        composeTestRule
            .onNodeWithTag(TestTags.DAFTAR_CATATAN)
            .assertIsDisplayed()

        // Judul kedua catatan harus terlihat dalam daftar
        composeTestRule
            .onNodeWithText("Catatan Pertama")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Catatan Kedua")
            .assertIsDisplayed()
    }

    // ----------------------------------------------------------------
    // TC-03: Hapus catatan menghilangkan item dari daftar
    // ----------------------------------------------------------------
    @Test
    fun `TC-03 hapus catatan menghilangkan item dari daftar`() {
        // Arrange: tambah satu catatan
        val fakeRepo  = FakeRepositoryForUiTest()
        val viewModel = NotesViewModel(fakeRepo, NoteValidator())
        viewModel.tambahNote(
            judul    = "Catatan Akan Dihapus",
            isi      = "Isi catatan ini",
            kategori = "Umum"
        )

        // Act: tampilkan layar
        composeTestRule.setContent {
            NotesScreen(
                onNavigateToDetail = {},
                onNavigateToBuat   = {},
                viewModel          = viewModel
            )
        }
        composeTestRule.waitForIdle()

        // Pastikan catatan tampil sebelum dihapus
        composeTestRule
            .onNodeWithText("Catatan Akan Dihapus")
            .assertIsDisplayed()

        // Act: tekan tombol hapus pada item pertama (id=1)
        composeTestRule
            .onNodeWithTag("${TestTags.TOMBOL_HAPUS}_1")
            .performClick()

        composeTestRule.waitForIdle()

        // Assert: catatan tidak boleh ada lagi, dan state kosong muncul
        composeTestRule
            .onNodeWithTag(TestTags.STATE_KOSONG)
            .assertIsDisplayed()
    }

    // ----------------------------------------------------------------
    // TC-04: Search bar memfilter catatan yang ditampilkan
    // ----------------------------------------------------------------
    @Test
    fun `TC-04 search bar menampilkan hanya catatan yang sesuai kata kunci`() {
        // Arrange: isi data dengan dua catatan berbeda
        val fakeRepo  = FakeRepositoryForUiTest()
        val viewModel = NotesViewModel(fakeRepo, NoteValidator())
        viewModel.tambahNote("Belajar Compose", "Materi Compose",  "Umum")
        viewModel.tambahNote("Resep Masakan",   "Cara memasak",    "Pribadi")

        // Act: tampilkan layar
        composeTestRule.setContent {
            NotesScreen(
                onNavigateToDetail = {},
                onNavigateToBuat   = {},
                viewModel          = viewModel
            )
        }
        composeTestRule.waitForIdle()

        // Act: ketik kata kunci di search bar
        composeTestRule
            .onNodeWithTag(TestTags.SEARCH_BAR)
            .performTextInput("Compose")

        composeTestRule.waitForIdle()

        // Assert: hanya catatan yang relevan yang tampil
        composeTestRule
            .onNodeWithText("Belajar Compose")
            .assertIsDisplayed()
    }

    // ----------------------------------------------------------------
    // TC-05: FAB tambah catatan bisa diklik
    // ----------------------------------------------------------------
    @Test
    fun `TC-05 tombol tambah FAB dapat diklik dan memicu navigasi`() {
        // Arrange
        val fakeRepo     = FakeRepositoryForUiTest()
        val viewModel    = NotesViewModel(fakeRepo, NoteValidator())
        var navigasiDipanggil = false

        // Act: tampilkan layar, pasang handler navigasi
        composeTestRule.setContent {
            NotesScreen(
                onNavigateToDetail = {},
                onNavigateToBuat   = { navigasiDipanggil = true },
                viewModel          = viewModel
            )
        }
        composeTestRule.waitForIdle()

        // Act: tekan tombol FAB
        composeTestRule
            .onNodeWithTag(TestTags.TOMBOL_TAMBAH)
            .performClick()

        // Assert: callback navigasi harus dipanggil
        assert(navigasiDipanggil) { "Navigasi ke layar buat catatan harus dipanggil saat FAB ditekan" }
    }
}
