package com.example.notesapp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * Implementasi DatabaseDriverFactory untuk platform iOS.
 * Menggunakan NativeSqliteDriver dengan SQLite bawaan iOS.
 */
actual class DatabaseDriverFactory {
    /**
     * Membuat NativeSqliteDriver dengan nama file database "notes.db".
     */
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = NoteDatabase.Schema,
            name = "notes.db"
        )
    }
}
