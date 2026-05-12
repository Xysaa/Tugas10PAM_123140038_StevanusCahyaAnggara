package com.example.notesapp.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepository
import com.example.notesapp.data.validation.NoteValidator
import com.example.notesapp.data.validation.ValidationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * ViewModel untuk layar daftar catatan (NotesScreen).
 *
 * Bertanggung jawab untuk:
 * - Mengambil dan menampilkan daftar catatan
 * - Mengelola pencarian dan filter kategori
 * - Mengelola operasi tambah, ubah, dan hapus catatan
 *
 * @param repository  Sumber data catatan (disuntikkan oleh Koin)
 * @param validator   Validator input catatan (disuntikkan oleh Koin)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val repository: NoteRepository,
    private val validator: NoteValidator
) : ViewModel() {

    /** StateFlow internal yang bisa diubah dari dalam ViewModel */
    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)

    /** StateFlow publik yang diobservasi oleh UI (read-only) */
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    /** StateFlow untuk menyimpan query pencarian aktif */
    private val _queryPencarian = MutableStateFlow("")

    /** StateFlow untuk menyimpan kategori yang sedang difilter */
    private val _kategoriDipilih = MutableStateFlow<String?>(null)

    init {
        // Mulai mengobservasi data saat ViewModel dibuat
        observeNotes()
    }

    /**
     * Mengobservasi perubahan notes dan kategori secara reaktif.
     * Akan otomatis re-emit saat query pencarian atau kategori berubah.
     */
    private fun observeNotes() {
        viewModelScope.launch {
            // Gabungkan query pencarian dan kategori yang dipilih
            combine(_queryPencarian, _kategoriDipilih) { query, kategori ->
                Pair(query, kategori)
            }.flatMapLatest { (query, kategori) ->
                // Pilih sumber data yang sesuai berdasarkan filter aktif
                when {
                    query.isNotBlank() -> repository.searchNotes(query)
                    kategori != null   -> repository.getNotesByKategori(kategori)
                    else               -> repository.getAllNotes()
                }
            }.combine(repository.getAllKategori()) { notes, kategoriList ->
                // Gabungkan data catatan dengan daftar kategori
                NotesUiState.Success(
                    notes            = notes,
                    queryPencarian   = _queryPencarian.value,
                    kategoriDipilih  = _kategoriDipilih.value,
                    daftarKategori   = kategoriList
                )
            }.catch { throwable ->
                // Tangkap error dan tampilkan ke UI
                emit(NotesUiState.Error(throwable.message ?: "Terjadi kesalahan"))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Menambahkan catatan baru ke database setelah memvalidasi input.
     *
     * @param judul    Judul catatan baru
     * @param isi      Isi catatan baru
     * @param kategori Kategori catatan baru
     */
    fun tambahNote(judul: String, isi: String, kategori: String = "Umum") {
        val noteBaru = Note(
            judul    = judul,
            isi      = isi,
            kategori = kategori
        )

        // Validasi input sebelum menyimpan
        if (!validator.isValid(noteBaru)) {
            _uiState.value = NotesUiState.Error("Input catatan tidak valid")
            return
        }

        viewModelScope.launch {
            try {
                repository.insertNote(noteBaru)
            } catch (e: ValidationException) {
                _uiState.value = NotesUiState.Error(e.message ?: "Validasi gagal")
            } catch (e: Exception) {
                _uiState.value = NotesUiState.Error("Gagal menyimpan catatan: ${e.message}")
            }
        }
    }

    /**
     * Mengubah catatan yang sudah ada di database.
     *
     * @param note Objek catatan dengan data terbaru (id harus valid)
     */
    fun ubahNote(note: Note) {
        // Validasi sebelum update
        if (!validator.isValid(note)) {
            _uiState.value = NotesUiState.Error("Input catatan tidak valid")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateNote(note)
            } catch (e: Exception) {
                _uiState.value = NotesUiState.Error("Gagal mengubah catatan: ${e.message}")
            }
        }
    }

    /**
     * Menghapus catatan dari database berdasarkan id.
     *
     * @param id Identitas unik catatan yang akan dihapus
     */
    fun hapusNote(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNote(id)
            } catch (e: Exception) {
                _uiState.value = NotesUiState.Error("Gagal menghapus catatan: ${e.message}")
            }
        }
    }

    /**
     * Mencari catatan berdasarkan kata kunci.
     * Akan otomatis mereset filter kategori saat pencarian aktif.
     *
     * @param query Kata kunci yang akan dicari
     */
    fun cariNote(query: String) {
        _queryPencarian.value = query
        // Reset filter kategori saat pengguna sedang mencari
        if (query.isNotBlank()) {
            _kategoriDipilih.value = null
        }
    }

    /**
     * Memfilter catatan berdasarkan kategori tertentu.
     * Akan mereset filter jika kategori yang sama dipilih ulang.
     *
     * @param kategori Nama kategori yang dipilih, null untuk menampilkan semua
     */
    fun filterByKategori(kategori: String?) {
        _kategoriDipilih.value = kategori
        // Reset pencarian saat filter kategori aktif
        _queryPencarian.value = ""
    }
}
