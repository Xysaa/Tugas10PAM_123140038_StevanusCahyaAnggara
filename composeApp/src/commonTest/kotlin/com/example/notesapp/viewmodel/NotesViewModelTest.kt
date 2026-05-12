package com.example.notesapp.viewmodel

import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepository
import com.example.notesapp.data.validation.NoteValidator
import com.example.notesapp.ui.notes.NotesUiState
import com.example.notesapp.ui.notes.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// ----------------------------------------------------------------
// SpyNoteRepository — fake repository yang merekam pemanggilan fungsi.
// Digunakan sebagai pengganti MockK di commonTest (cross-platform).
// ----------------------------------------------------------------
class SpyNoteRepository : NoteRepository {

    private val _catatan = MutableStateFlow<List<Note>>(emptyList())
    private var idCounter = 1L

    // Variabel untuk merekam apakah fungsi dipanggil (spy behavior)
    var insertDipanggil = false
    var deleteDipanggil = false
    var updateDipanggil = false
    var idYangDihapus: Long? = null
    var noteYangDisimpan: Note? = null

    override fun getAllNotes(): Flow<List<Note>> =
        _catatan.map { it.sortedByDescending { n -> n.tanggalDiubah } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        _catatan.map { list ->
            list.filter { it.judul.contains(query, true) || it.isi.contains(query, true) }
        }

    override fun getNotesByKategori(kategori: String): Flow<List<Note>> =
        _catatan.map { list -> list.filter { it.kategori == kategori } }

    override suspend fun getNoteById(id: Long): Note? = _catatan.value.find { it.id == id }

    override suspend fun insertNote(note: Note) {
        // Catat bahwa fungsi ini dipanggil
        insertDipanggil = true
        noteYangDisimpan = note
        _catatan.update { it + note.copy(id = idCounter++) }
    }

    override suspend fun updateNote(note: Note) {
        updateDipanggil = true
        _catatan.update { list -> list.map { if (it.id == note.id) note else it } }
    }

    override suspend fun deleteNote(id: Long) {
        // Catat id yang dihapus untuk verifikasi
        deleteDipanggil = true
        idYangDihapus = id
        _catatan.update { list -> list.filter { it.id != id } }
    }

    override fun getAllKategori(): Flow<List<String>> =
        _catatan.map { it.map { n -> n.kategori }.distinct().sorted() }

    fun reset() {
        _catatan.value = emptyList()
        idCounter = 1L
        insertDipanggil = false
        deleteDipanggil = false
        updateDipanggil = false
        idYangDihapus = null
        noteYangDisimpan = null
    }
}

// ================================================================
// Unit Test: NotesViewModel
// Min. 4 test cases — semua menggunakan pola AAA
// ================================================================
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: SpyNoteRepository
    private lateinit var validator: NoteValidator
    private lateinit var viewModel: NotesViewModel

    @BeforeTest
    fun setUp() {
        // Arrange global: ganti Main dispatcher dengan test dispatcher
        Dispatchers.setMain(testDispatcher)
        fakeRepository = SpyNoteRepository()
        validator      = NoteValidator()
        viewModel      = NotesViewModel(fakeRepository, validator)
    }

    @AfterTest
    fun tearDown() {
        // Kembalikan dispatcher ke kondisi semula setelah setiap test
        Dispatchers.resetMain()
    }

    // ----------------------------------------------------------------
    // TC-01: State awal adalah Loading kemudian beralih ke Success
    // ----------------------------------------------------------------
    @Test
    fun `TC-01 state awal Loading kemudian berubah menjadi Success`() = runTest(testDispatcher) {
        // Arrange: viewModel sudah dibuat di setUp dengan repository kosong

        // Act: tunggu semua coroutine selesai
        advanceUntilIdle()

        // Assert: state akhir harus Success (meski list kosong)
        val stateAkhir = viewModel.uiState.value
        assertIs<NotesUiState.Success>(stateAkhir, "State harus Success setelah inisialisasi")
        assertTrue(
            (stateAkhir as NotesUiState.Success).notes.isEmpty(),
            "Daftar catatan harus kosong saat repository kosong"
        )
    }

    // ----------------------------------------------------------------
    // TC-02: tambahNote memanggil repository.insertNote
    // ----------------------------------------------------------------
    @Test
    fun `TC-02 tambahNote memanggil insertNote di repository`() = runTest(testDispatcher) {
        // Arrange: pastikan spy dalam kondisi bersih
        fakeRepository.reset()
        // Buat ulang viewModel dengan repository yang sudah di-reset
        viewModel = NotesViewModel(fakeRepository, validator)
        advanceUntilIdle()

        // Act: panggil tambahNote pada ViewModel
        viewModel.tambahNote(
            judul    = "Judul Test",
            isi      = "Isi test yang cukup panjang",
            kategori = "Umum"
        )
        advanceUntilIdle()

        // Assert: repository.insertNote harus terpanggil
        assertTrue(fakeRepository.insertDipanggil, "insertNote di repository harus dipanggil")
        assertEquals(
            "Judul Test",
            fakeRepository.noteYangDisimpan?.judul,
            "Catatan yang disimpan harus memiliki judul yang benar"
        )
    }

    // ----------------------------------------------------------------
    // TC-03: hapusNote memanggil repository.deleteNote dengan id yang benar
    // ----------------------------------------------------------------
    @Test
    fun `TC-03 hapusNote memanggil deleteNote dengan id yang benar`() = runTest(testDispatcher) {
        // Arrange: tambah catatan dulu agar ada yang bisa dihapus
        viewModel = NotesViewModel(fakeRepository, validator)
        advanceUntilIdle()
        viewModel.tambahNote("Catatan Hapus", "Isi catatan", "Umum")
        advanceUntilIdle()

        // Ambil id catatan yang baru saja ditambahkan
        val state = viewModel.uiState.value as NotesUiState.Success
        val idTarget = state.notes.first().id
        fakeRepository.deleteDipanggil = false // reset flag sebelum act

        // Act: hapus catatan berdasarkan id
        viewModel.hapusNote(idTarget)
        advanceUntilIdle()

        // Assert: deleteNote harus terpanggil dengan id yang benar
        assertTrue(fakeRepository.deleteDipanggil, "deleteNote di repository harus dipanggil")
        assertEquals(
            idTarget,
            fakeRepository.idYangDihapus,
            "Id yang dihapus harus sesuai dengan id yang diminta"
        )
    }

    // ----------------------------------------------------------------
    // TC-04: cariNote mengupdate state dengan hasil pencarian
    // ----------------------------------------------------------------
    @Test
    fun `TC-04 cariNote memfilter daftar catatan berdasarkan query`() = runTest(testDispatcher) {
        // Arrange: reset repository dan buat ViewModel baru
        fakeRepository.reset()
        viewModel = NotesViewModel(fakeRepository, validator)
        advanceUntilIdle()

        // Tambah catatan melalui ViewModel (bukan langsung ke repo)
        // agar lifecycle coroutine test dispatcher berjalan dengan benar
        viewModel.tambahNote("Belajar Android",   "Materi Jetpack Compose",    "Pekerjaan")
        advanceUntilIdle()
        viewModel.tambahNote("Resep Masak",       "Cara membuat nasi goreng",  "Pribadi")
        advanceUntilIdle()
        viewModel.tambahNote("Kotlin Coroutines", "Materi Coroutines",         "Umum")
        advanceUntilIdle()

        // Verifikasi data awal sudah masuk (3 catatan)
        val stateAwal = viewModel.uiState.value
        assertIs<NotesUiState.Success>(stateAwal)
        assertEquals(
            3,
            (stateAwal as NotesUiState.Success).notes.size,
            "Data awal harus 3 catatan sebelum pencarian"
        )

        // Act: cari catatan dengan kata kunci "Kotlin"
        viewModel.cariNote("Kotlin")
        advanceUntilIdle()

        // Assert: hanya catatan yang mengandung "Kotlin" yang muncul
        val state = viewModel.uiState.value
        assertIs<NotesUiState.Success>(state)
        val hasilCari = (state as NotesUiState.Success).notes
        assertEquals(2, hasilCari.size, "Harus ada 2 catatan mengandung 'Kotlin'")
        assertEquals("Kotlin", state.queryPencarian, "Query pencarian harus tersimpan di state")
        assertTrue(
            hasilCari.all { it.judul.contains("Kotlin", true) || it.isi.contains("Kotlin", true) },
            "Semua hasil harus mengandung kata kunci 'Kotlin'"
        )
    }

    // ----------------------------------------------------------------
    // TC-05: tambahNote dengan input tidak valid tidak memanggil insertNote
    // ----------------------------------------------------------------
    @Test
    fun `TC-05 tambahNote dengan judul kosong menghasilkan state Error`() = runTest(testDispatcher) {
        // Arrange
        fakeRepository.reset()
        viewModel = NotesViewModel(fakeRepository, validator)
        advanceUntilIdle()

        // Act: coba tambah catatan dengan judul kosong (tidak valid)
        viewModel.tambahNote(judul = "", isi = "Isi ada", kategori = "Umum")
        advanceUntilIdle()

        // Assert: insertNote tidak boleh dipanggil karena validasi gagal
        assertTrue(
            !fakeRepository.insertDipanggil,
            "insertNote tidak boleh dipanggil untuk input tidak valid"
        )
        assertIs<NotesUiState.Error>(
            viewModel.uiState.value,
            "State harus Error saat input tidak valid"
        )
    }
}
