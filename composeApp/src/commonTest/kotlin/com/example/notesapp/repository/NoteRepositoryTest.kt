package com.example.notesapp.repository

import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ----------------------------------------------------------------
// FakeNoteRepository — implementasi in-memory untuk keperluan test.
// Digunakan agar test tidak bergantung pada SQLite driver platform.
// ----------------------------------------------------------------
class FakeNoteRepository : NoteRepository {

    /** Penyimpanan data in-memory menggunakan StateFlow agar reaktif */
    private val _catatan = MutableStateFlow<List<Note>>(emptyList())

    /** Counter untuk men-generate id otomatis (simulasi AUTOINCREMENT) */
    private var idCounter = 1L

    override fun getAllNotes(): Flow<List<Note>> =
        _catatan.map { list -> list.sortedByDescending { it.tanggalDiubah } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        _catatan.map { list ->
            list.filter { note ->
                note.judul.contains(query, ignoreCase = true) ||
                note.isi.contains(query, ignoreCase = true)
            }
        }

    override fun getNotesByKategori(kategori: String): Flow<List<Note>> =
        _catatan.map { list -> list.filter { it.kategori == kategori } }

    override suspend fun getNoteById(id: Long): Note? =
        _catatan.value.find { it.id == id }

    override suspend fun insertNote(note: Note) {
        val noteBaru = note.copy(id = idCounter++)
        _catatan.update { list -> list + noteBaru }
    }

    override suspend fun updateNote(note: Note) {
        _catatan.update { list ->
            list.map { if (it.id == note.id) note else it }
        }
    }

    override suspend fun deleteNote(id: Long) {
        _catatan.update { list -> list.filter { it.id != id } }
    }

    override fun getAllKategori(): Flow<List<String>> =
        _catatan.map { list -> list.map { it.kategori }.distinct().sorted() }

    /** Fungsi helper untuk mengisi data awal dalam test */
    suspend fun isiDataAwal(vararg notes: Note) {
        notes.forEach { insertNote(it) }
    }

    /** Fungsi helper untuk mengosongkan semua data */
    fun kosongkan() {
        _catatan.value = emptyList()
        idCounter = 1L
    }
}

// ================================================================
// Unit Test: NoteRepository
// Min. 5 test cases — semua menggunakan pola AAA (Arrange-Act-Assert)
// ================================================================
class NoteRepositoryTest {

    // Objek repository yang digunakan di semua test
    private lateinit var repository: FakeNoteRepository

    @BeforeTest
    fun setUp() {
        // Arrange: buat instance baru sebelum setiap test
        repository = FakeNoteRepository()
    }

    // ----------------------------------------------------------------
    // TC-01: insertNote berhasil menyimpan catatan
    // ----------------------------------------------------------------
    @Test
    fun `TC-01 insertNote berhasil menyimpan catatan baru`() = runTest {
        // Arrange: siapkan data catatan baru
        val catatanBaru = Note(
            judul    = "Judul Test",
            isi      = "Isi catatan test",
            kategori = "Umum"
        )

        // Act: simpan catatan ke repository
        repository.insertNote(catatanBaru)

        // Assert: pastikan catatan berhasil tersimpan
        val semuaCatatan = repository.getAllNotes().first()
        assertEquals(1, semuaCatatan.size, "Harus ada tepat 1 catatan setelah insert")
        assertEquals("Judul Test", semuaCatatan[0].judul)
        assertEquals("Isi catatan test", semuaCatatan[0].isi)
    }

    // ----------------------------------------------------------------
    // TC-02: getAllNotes mengembalikan semua catatan sebagai Flow
    // ----------------------------------------------------------------
    @Test
    fun `TC-02 getAllNotes mengembalikan semua catatan yang tersimpan`() = runTest {
        // Arrange: isi database dengan beberapa catatan
        repository.isiDataAwal(
            Note(judul = "Catatan A", isi = "Isi A", kategori = "Umum"),
            Note(judul = "Catatan B", isi = "Isi B", kategori = "Pekerjaan"),
            Note(judul = "Catatan C", isi = "Isi C", kategori = "Pribadi")
        )

        // Act: ambil semua catatan dari repository
        val semuaCatatan = repository.getAllNotes().first()

        // Assert: harus ada 3 catatan
        assertEquals(3, semuaCatatan.size, "Harus ada tepat 3 catatan")
    }

    // ----------------------------------------------------------------
    // TC-03: deleteNote menghapus catatan yang benar
    // ----------------------------------------------------------------
    @Test
    fun `TC-03 deleteNote menghapus catatan berdasarkan id dengan benar`() = runTest {
        // Arrange: isi 2 catatan, lalu ambil id catatan pertama
        repository.isiDataAwal(
            Note(judul = "Akan Dihapus", isi = "Isi 1", kategori = "Umum"),
            Note(judul = "Tetap Ada",    isi = "Isi 2", kategori = "Umum")
        )
        val sebelumHapus = repository.getAllNotes().first()
        val idCatatanDihapus = sebelumHapus.first { it.judul == "Akan Dihapus" }.id

        // Act: hapus catatan pertama berdasarkan id
        repository.deleteNote(idCatatanDihapus)

        // Assert: hanya 1 catatan tersisa, dan bukan catatan yang dihapus
        val sesudahHapus = repository.getAllNotes().first()
        assertEquals(1, sesudahHapus.size, "Harus tersisa 1 catatan setelah hapus")
        assertEquals("Tetap Ada", sesudahHapus[0].judul)
        assertNull(
            sesudahHapus.find { it.id == idCatatanDihapus },
            "Catatan yang dihapus tidak boleh ada lagi"
        )
    }

    // ----------------------------------------------------------------
    // TC-04: updateNote mengubah data catatan dengan benar
    // ----------------------------------------------------------------
    @Test
    fun `TC-04 updateNote mengubah data catatan yang sudah ada`() = runTest {
        // Arrange: simpan catatan awal, lalu ambil id-nya
        repository.insertNote(Note(judul = "Judul Lama", isi = "Isi Lama", kategori = "Umum"))
        val catatanAwal = repository.getAllNotes().first().first()

        // Act: perbarui catatan dengan data baru
        val catatanDiperbarui = catatanAwal.copy(
            judul    = "Judul Baru",
            isi      = "Isi Baru",
            kategori = "Pekerjaan"
        )
        repository.updateNote(catatanDiperbarui)

        // Assert: data catatan harus berubah sesuai yang diperbarui
        val setelahUpdate = repository.getNoteById(catatanAwal.id)
        assertNotNull(setelahUpdate, "Catatan harus masih ada setelah update")
        assertEquals("Judul Baru",   setelahUpdate.judul,    "Judul harus berubah")
        assertEquals("Isi Baru",     setelahUpdate.isi,      "Isi harus berubah")
        assertEquals("Pekerjaan",    setelahUpdate.kategori, "Kategori harus berubah")
    }

    // ----------------------------------------------------------------
    // TC-05: searchNotes mengembalikan hasil yang sesuai query
    // ----------------------------------------------------------------
    @Test
    fun `TC-05 searchNotes mengembalikan catatan yang sesuai kata kunci`() = runTest {
        // Arrange: isi data dengan berbagai catatan
        repository.isiDataAwal(
            Note(judul = "Belajar Kotlin",  isi = "Materi KMP",       kategori = "Umum"),
            Note(judul = "Resep Masakan",   isi = "Ayam goreng enak", kategori = "Pribadi"),
            Note(judul = "Meeting Project", isi = "Diskusi Kotlin",   kategori = "Pekerjaan")
        )

        // Act: cari catatan dengan kata kunci "Kotlin"
        val hasilPencarian = repository.searchNotes("Kotlin").first()

        // Assert: harus menemukan 2 catatan (judul dan isi sama-sama diperiksa)
        assertEquals(2, hasilPencarian.size, "Harus ditemukan 2 catatan mengandung 'Kotlin'")
        assertTrue(
            hasilPencarian.any { it.judul == "Belajar Kotlin" },
            "Catatan 'Belajar Kotlin' harus ditemukan lewat judul"
        )
        assertTrue(
            hasilPencarian.any { it.judul == "Meeting Project" },
            "Catatan 'Meeting Project' harus ditemukan lewat isi"
        )
    }

    // ----------------------------------------------------------------
    // TC-06: getAllNotes mengembalikan list kosong jika belum ada data
    // ----------------------------------------------------------------
    @Test
    fun `TC-06 getAllNotes mengembalikan list kosong saat database kosong`() = runTest {
        // Arrange: repository sudah kosong (dari setUp)

        // Act: ambil semua catatan
        val hasil = repository.getAllNotes().first()

        // Assert: harus mengembalikan list kosong, bukan null atau error
        assertTrue(hasil.isEmpty(), "Harus mengembalikan list kosong saat belum ada data")
    }

    // ----------------------------------------------------------------
    // TC-07: getNotesByKategori hanya mengembalikan catatan kategori itu
    // ----------------------------------------------------------------
    @Test
    fun `TC-07 getNotesByKategori hanya mengembalikan catatan dengan kategori yang sesuai`() = runTest {
        // Arrange: isi data dengan berbagai kategori
        repository.isiDataAwal(
            Note(judul = "Tugas Kantor",   isi = "Isi 1", kategori = "Pekerjaan"),
            Note(judul = "Diari Pribadi",  isi = "Isi 2", kategori = "Pribadi"),
            Note(judul = "Sprint Review",  isi = "Isi 3", kategori = "Pekerjaan")
        )

        // Act: filter khusus kategori "Pekerjaan"
        val hasilFilter = repository.getNotesByKategori("Pekerjaan").first()

        // Assert: hanya 2 catatan dengan kategori "Pekerjaan" yang muncul
        assertEquals(2, hasilFilter.size, "Harus ada 2 catatan berkategori Pekerjaan")
        assertTrue(
            hasilFilter.all { it.kategori == "Pekerjaan" },
            "Semua hasil harus berkategori Pekerjaan"
        )
    }
}
