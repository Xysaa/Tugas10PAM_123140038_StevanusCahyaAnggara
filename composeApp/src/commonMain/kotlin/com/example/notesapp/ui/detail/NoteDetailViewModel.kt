package com.example.notesapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepository
import com.example.notesapp.data.validation.NoteValidator
import com.example.notesapp.data.validation.ValidationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class untuk state layar detail/form catatan.
 */
sealed class NoteDetailUiState {
    /** State awal saat layar baru dibuka (form kosong untuk tambah baru) */
    object Idle : NoteDetailUiState()
    /** State saat data catatan sedang dimuat (untuk mode edit) */
    object Loading : NoteDetailUiState()
    /** State saat data catatan berhasil dimuat untuk diedit */
    data class Ready(val note: Note) : NoteDetailUiState()
    /** State saat operasi simpan berhasil dilakukan */
    object SaveSuccess : NoteDetailUiState()
    /** State saat terjadi kesalahan */
    data class Error(val pesan: String) : NoteDetailUiState()
}

/**
 * ViewModel untuk layar tambah/edit catatan (NoteDetailScreen).
 *
 * @param repository  Sumber data catatan (disuntikkan oleh Koin)
 * @param validator   Validator input catatan (disuntikkan oleh Koin)
 */
class NoteDetailViewModel(
    private val repository: NoteRepository,
    private val validator: NoteValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteDetailUiState>(NoteDetailUiState.Idle)
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    /**
     * Memuat data catatan yang akan diedit berdasarkan id.
     * Dipanggil saat layar edit dibuka.
     *
     * @param noteId Id catatan yang akan diedit
     */
    fun muatNote(noteId: Long) {
        viewModelScope.launch {
            _uiState.value = NoteDetailUiState.Loading
            try {
                val note = repository.getNoteById(noteId)
                if (note != null) {
                    _uiState.value = NoteDetailUiState.Ready(note)
                } else {
                    _uiState.value = NoteDetailUiState.Error("Catatan tidak ditemukan")
                }
            } catch (e: Exception) {
                _uiState.value = NoteDetailUiState.Error("Gagal memuat catatan: ${e.message}")
            }
        }
    }

    /**
     * Menyimpan catatan baru ke database setelah validasi.
     *
     * @param judul    Judul catatan
     * @param isi      Isi catatan
     * @param kategori Kategori catatan
     */
    fun simpanNoteBaru(judul: String, isi: String, kategori: String = "Umum") {
        val noteBaru = Note(
            judul    = judul,
            isi      = isi,
            kategori = kategori
        )

        viewModelScope.launch {
            try {
                // Lempar ValidationException jika input tidak valid
                validator.validate(noteBaru)
                repository.insertNote(noteBaru)
                _uiState.value = NoteDetailUiState.SaveSuccess
            } catch (e: ValidationException) {
                _uiState.value = NoteDetailUiState.Error(e.message ?: "Validasi gagal")
            } catch (e: Exception) {
                _uiState.value = NoteDetailUiState.Error("Gagal menyimpan catatan: ${e.message}")
            }
        }
    }

    /**
     * Memperbarui catatan yang sudah ada setelah validasi.
     *
     * @param note Objek catatan dengan data terbaru
     */
    fun perbaruiNote(note: Note) {
        viewModelScope.launch {
            try {
                validator.validate(note)
                repository.updateNote(note)
                _uiState.value = NoteDetailUiState.SaveSuccess
            } catch (e: ValidationException) {
                _uiState.value = NoteDetailUiState.Error(e.message ?: "Validasi gagal")
            } catch (e: Exception) {
                _uiState.value = NoteDetailUiState.Error("Gagal memperbarui catatan: ${e.message}")
            }
        }
    }

    /** Mereset state ke Idle (digunakan setelah error ditampilkan) */
    fun resetState() {
        _uiState.value = NoteDetailUiState.Idle
    }
}
