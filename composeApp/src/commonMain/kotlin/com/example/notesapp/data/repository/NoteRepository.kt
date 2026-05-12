package com.example.notesapp.data.repository

import com.example.notesapp.data.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Interface repository untuk operasi CRUD catatan.
 *
 * Menggunakan interface agar mudah di-mock saat pengujian
 * dan mendukung prinsip Dependency Inversion (DIP).
 */
interface NoteRepository {

    /**
     * Mengambil semua catatan sebagai Flow yang reaktif.
     * Setiap perubahan data akan otomatis di-emit.
     */
    fun getAllNotes(): Flow<List<Note>>

    /**
     * Mencari catatan berdasarkan kata kunci pada judul atau isi.
     *
     * @param query Kata kunci pencarian
     */
    fun searchNotes(query: String): Flow<List<Note>>

    /**
     * Mengambil catatan berdasarkan kategori tertentu.
     *
     * @param kategori Nama kategori yang ingin difilter
     */
    fun getNotesByKategori(kategori: String): Flow<List<Note>>

    /**
     * Mengambil satu catatan berdasarkan id-nya.
     *
     * @param id Identitas unik catatan
     * @return Note jika ditemukan, null jika tidak ada
     */
    suspend fun getNoteById(id: Long): Note?

    /**
     * Menyimpan catatan baru ke dalam database.
     *
     * @param note Objek catatan yang akan disimpan
     */
    suspend fun insertNote(note: Note)

    /**
     * Memperbarui catatan yang sudah ada di database.
     *
     * @param note Objek catatan dengan data terbaru (id harus valid)
     */
    suspend fun updateNote(note: Note)

    /**
     * Menghapus catatan dari database berdasarkan id.
     *
     * @param id Identitas unik catatan yang akan dihapus
     */
    suspend fun deleteNote(id: Long)

    /**
     * Mengambil daftar semua nama kategori yang ada di database.
     * Digunakan untuk menampilkan chip filter kategori.
     */
    fun getAllKategori(): Flow<List<String>>
}
