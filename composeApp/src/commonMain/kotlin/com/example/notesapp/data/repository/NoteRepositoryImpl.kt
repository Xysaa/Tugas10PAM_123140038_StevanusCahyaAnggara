package com.example.notesapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.notesapp.data.local.NoteDatabase
import com.example.notesapp.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementasi konkret dari NoteRepository yang menggunakan SQLDelight
 * sebagai sumber data lokal.
 *
 * @param database Instance NoteDatabase yang di-generate oleh SQLDelight
 */
class NoteRepositoryImpl(
    private val database: NoteDatabase
) : NoteRepository {

    /** Referensi ke queries yang di-generate SQLDelight dari file Note.sq */
    private val queries = database.noteQueries

    /**
     * Mengambil semua catatan sebagai Flow reaktif.
     * Data diurutkan dari yang paling baru diubah (lihat Note.sq).
     */
    override fun getAllNotes(): Flow<List<Note>> {
        return queries.getAllNotes()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                // Konversi dari NoteEntity (SQLDelight) ke Note (domain model)
                list.map { entity -> entity.toNote() }
            }
    }

    /**
     * Mencari catatan berdasarkan kata kunci.
     * Pencarian dilakukan pada kolom judul dan isi secara bersamaan.
     */
    override fun searchNotes(query: String): Flow<List<Note>> {
        return queries.searchNotes(query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toNote() } }
    }

    /**
     * Mengambil catatan yang termasuk dalam kategori tertentu.
     */
    override fun getNotesByKategori(kategori: String): Flow<List<Note>> {
        return queries.getNotesByKategori(kategori)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toNote() } }
    }

    /**
     * Mengambil satu catatan berdasarkan id-nya.
     * Dieksekusi di Dispatchers.IO agar tidak memblokir main thread.
     */
    override suspend fun getNoteById(id: Long): Note? {
        return withContext(Dispatchers.Default) {
            queries.getNoteById(id).executeAsOneOrNull()?.toNote()
        }
    }

    /**
     * Menyimpan catatan baru ke dalam database.
     * id diabaikan karena AUTOINCREMENT akan men-generate id baru.
     */
    override suspend fun insertNote(note: Note) {
        withContext(Dispatchers.Default) {
            queries.insertNote(
                judul          = note.judul,
                isi            = note.isi,
                kategori       = note.kategori,
                tanggal_dibuat = note.tanggalDibuat,
                tanggal_diubah = note.tanggalDiubah
            )
        }
    }

    /**
     * Memperbarui data catatan yang sudah ada.
     * Menggunakan id catatan untuk menentukan baris yang diperbarui.
     */
    override suspend fun updateNote(note: Note) {
        withContext(Dispatchers.Default) {
            queries.updateNote(
                judul          = note.judul,
                isi            = note.isi,
                kategori       = note.kategori,
                tanggal_diubah = System.currentTimeMillis(),
                id             = note.id
            )
        }
    }

    /**
     * Menghapus catatan dari database berdasarkan id.
     */
    override suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.Default) {
            queries.deleteNote(id)
        }
    }

    /**
     * Mengambil semua nama kategori yang unik dari database.
     * Digunakan untuk menampilkan chip filter di layar utama.
     */
    override fun getAllKategori(): Flow<List<String>> {
        return queries.getAllKategori()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }
}

// ----------------------------------------------------------------
// Extension function: konversi NoteEntity → Note (domain model)
// NoteEntity adalah kelas yang di-generate otomatis oleh SQLDelight
// ----------------------------------------------------------------
private fun com.example.notesapp.data.local.NoteEntity.toNote(): Note {
    return Note(
        id            = id,
        judul         = judul,
        isi           = isi,
        kategori      = kategori,
        tanggalDibuat = tanggal_dibuat,
        tanggalDiubah = tanggal_diubah
    )
}
