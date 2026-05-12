package com.example.notesapp.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Implementasi DatabaseDriverFactory untuk platform Android.
 * Menggunakan AndroidSqliteDriver dengan SQLite bawaan Android.
 *
 * @param context Android Context, diperlukan oleh AndroidSqliteDriver
 */
actual class DatabaseDriverFactory(private val context: Context) {
    /**
     * Membuat AndroidSqliteDriver dengan nama file database "notes.db".
     */
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = NoteDatabase.Schema,
            context = context,
            name = "notes.db"
        )
    }
}
