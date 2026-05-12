package com.example.notesapp.data.validation

import com.example.notesapp.data.model.Note

/**
 * Exception yang dilempar ketika validasi catatan gagal.
 *
 * @param pesan Deskripsi kesalahan validasi dalam Bahasa Indonesia
 */
class ValidationException(pesan: String) : Exception(pesan)

/**
 * Kelas untuk memvalidasi input catatan sebelum disimpan ke database.
 *
 * Aturan validasi:
 * - Judul tidak boleh kosong atau hanya spasi
 * - Judul maksimal 200 karakter
 * - Isi tidak boleh kosong atau hanya spasi
 * - Kategori tidak boleh kosong atau hanya spasi
 */
class NoteValidator {

    companion object {
        /** Panjang maksimum karakter yang diperbolehkan untuk judul */
        const val MAKS_PANJANG_JUDUL = 200
    }

    /**
     * Memeriksa apakah catatan valid berdasarkan semua aturan validasi.
     *
     * @param note Catatan yang akan diperiksa
     * @return true jika valid, false jika ada aturan yang dilanggar
     */
    fun isValid(note: Note): Boolean {
        return note.judul.isNotBlank()
                && note.judul.length <= MAKS_PANJANG_JUDUL
                && note.isi.isNotBlank()
                && note.kategori.isNotBlank()
    }

    /**
     * Memvalidasi catatan dan melempar [ValidationException] jika tidak valid.
     * Mengecek setiap aturan secara berurutan dan memberikan pesan yang spesifik.
     *
     * @param note Catatan yang akan divalidasi
     * @throws ValidationException jika ada aturan validasi yang dilanggar
     */
    fun validate(note: Note) {
        // Validasi judul tidak boleh kosong
        if (note.judul.isBlank()) {
            throw ValidationException("Judul catatan tidak boleh kosong")
        }

        // Validasi panjang judul
        if (note.judul.length > MAKS_PANJANG_JUDUL) {
            throw ValidationException(
                "Judul catatan terlalu panjang (maks. $MAKS_PANJANG_JUDUL karakter)"
            )
        }

        // Validasi isi tidak boleh kosong
        if (note.isi.isBlank()) {
            throw ValidationException("Isi catatan tidak boleh kosong")
        }

        // Validasi kategori tidak boleh kosong
        if (note.kategori.isBlank()) {
            throw ValidationException("Kategori catatan tidak boleh kosong")
        }
    }
}
