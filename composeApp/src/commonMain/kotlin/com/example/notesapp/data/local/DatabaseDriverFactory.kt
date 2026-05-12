package com.example.notesapp.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory untuk membuat SQLDriver yang sesuai dengan platform masing-masing.
 * Implementasi actual ada di androidMain dan iosMain.
 */
expect class DatabaseDriverFactory {
    /**
     * Membuat dan mengembalikan SQLDriver yang sesuai platform.
     * - Android: menggunakan AndroidSqliteDriver
     * - iOS: menggunakan NativeSqliteDriver
     */
    fun createDriver(): SqlDriver
}
