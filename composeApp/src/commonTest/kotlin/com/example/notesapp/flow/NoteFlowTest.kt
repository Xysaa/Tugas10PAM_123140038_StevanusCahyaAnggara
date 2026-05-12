package com.example.notesapp.flow

import app.cash.turbine.test
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// ----------------------------------------------------------------
// ReactiveNoteRepository — fake repository yang mendukung test Flow
// reaktif (mengeluarkan nilai baru saat data berubah).
// ----------------------------------------------------------------
class ReactiveNoteRepository : NoteRepository {

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
        val noteBaru = note.copy(id = idCounter++)
        _catatan.update { it + noteBaru }
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
// Flow Test menggunakan Turbine
// Min. 2 test cases — menguji perilaku Flow secara asinkron
// ================================================================
class NoteFlowTest {

    private lateinit var repository: ReactiveNoteRepository

    @BeforeTest
    fun setUp() {
        repository = ReactiveNoteRepository()
    }

    // ----------------------------------------------------------------
    // TC-01: Flow catatan emit list kosong saat database kosong
    // ----------------------------------------------------------------
    @Test
    fun `TC-01 Flow getAllNotes emit list kosong saat database kosong`() = runTest {
        // Arrange: repository dalam kondisi kosong (dari setUp)

        // Act & Assert: gunakan Turbine untuk memeriksa emisi Flow
        repository.getAllNotes().test {
            // Emisi pertama harus berupa list kosong
            val emisiPertama = awaitItem()
            assertTrue(
                emisiPertama.isEmpty(),
                "Flow harus emit list kosong saat belum ada data"
            )

            // Selesaikan pengujian Flow
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----------------------------------------------------------------
    // TC-02: Flow catatan emit data terbaru setelah insert
    // ----------------------------------------------------------------
    @Test
    fun `TC-02 Flow getAllNotes emit data terbaru setelah catatan diinsert`() = runTest {
        // Arrange: persiapkan data yang akan diinsert
        val catatanBaru = Note(
            judul    = "Catatan Baru",
            isi      = "Isi catatan baru",
            kategori = "Umum"
        )

        // Act & Assert: pantau Flow sambil melakukan insert
        repository.getAllNotes().test {
            // Emisi 1: list kosong (initial state)
            val emisiAwal = awaitItem()
            assertTrue(emisiAwal.isEmpty(), "Emisi pertama harus kosong")

            // Insert catatan baru — Flow harus emit ulang
            repository.insertNote(catatanBaru)

            // Emisi 2: list dengan catatan yang baru diinsert
            val emisiSetelahInsert = awaitItem()
            assertEquals(1, emisiSetelahInsert.size, "Harus ada 1 catatan setelah insert")
            assertEquals(
                "Catatan Baru",
                emisiSetelahInsert[0].judul,
                "Judul catatan harus sesuai"
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----------------------------------------------------------------
    // TC-03: Flow emit urutan emisi yang benar setelah beberapa operasi
    // ----------------------------------------------------------------
    @Test
    fun `TC-03 Flow emit perubahan secara berurutan setelah insert dan delete`() = runTest {
        // Arrange: siapkan dua catatan
        val catatan1 = Note(judul = "Pertama",  isi = "Isi 1", kategori = "Umum")
        val catatan2 = Note(judul = "Kedua",     isi = "Isi 2", kategori = "Pekerjaan")

        repository.getAllNotes().test {
            // Assert emisi awal (kosong)
            assertTrue(awaitItem().isEmpty())

            // Act: insert catatan pertama
            repository.insertNote(catatan1)
            assertEquals(1, awaitItem().size, "Setelah insert pertama: 1 catatan")

            // Act: insert catatan kedua
            repository.insertNote(catatan2)
            val emisiDua = awaitItem()
            assertEquals(2, emisiDua.size, "Setelah insert kedua: 2 catatan")

            // Act: hapus catatan pertama
            val idPertama = emisiDua.first { it.judul == "Pertama" }.id
            repository.deleteNote(idPertama)
            val emisiSetelahHapus = awaitItem()
            assertEquals(1, emisiSetelahHapus.size, "Setelah hapus: harus 1 catatan tersisa")
            assertEquals("Kedua", emisiSetelahHapus[0].judul)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----------------------------------------------------------------
    // TC-04: Flow searchNotes emit hasil pencarian yang benar
    // ----------------------------------------------------------------
    @Test
    fun `TC-04 Flow searchNotes emit hanya catatan yang sesuai query`() = runTest {
        // Arrange: isi data awal sebelum subscribe ke Flow pencarian
        repository.insertNote(Note(judul = "Belajar Kotlin", isi = "KMP bagus",   kategori = "Umum"))
        repository.insertNote(Note(judul = "Resep Masak",    isi = "Cara masak",  kategori = "Pribadi"))
        repository.insertNote(Note(judul = "Tips Android",   isi = "Pakai Kotlin", kategori = "Pekerjaan"))

        // Act & Assert: subscribe ke Flow pencarian "Kotlin"
        repository.searchNotes("Kotlin").test {
            val hasil = awaitItem()

            // Assert: harus menemukan 2 catatan yang mengandung "Kotlin"
            assertEquals(
                2,
                hasil.size,
                "Flow search harus emit 2 catatan yang mengandung 'Kotlin'"
            )
            assertTrue(
                hasil.all { it.judul.contains("Kotlin", true) || it.isi.contains("Kotlin", true) },
                "Semua hasil harus relevan dengan query 'Kotlin'"
            )

            cancelAndIgnoreRemainingEvents()
        }
    }
}
