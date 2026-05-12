package com.example.notesapp.di

import com.example.notesapp.data.local.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Modul Koin khusus Android untuk menyediakan platform-specific dependencies.
 *
 * Menyuntikkan Android Context ke dalam DatabaseDriverFactory
 * agar AndroidSqliteDriver dapat dibuat dengan benar.
 */
val androidModule = module {
    // Sediakan DatabaseDriverFactory dengan Android Context dari Koin
    single { DatabaseDriverFactory(androidContext()) }
}
