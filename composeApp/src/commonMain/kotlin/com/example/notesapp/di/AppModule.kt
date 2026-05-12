package com.example.notesapp.di

import com.example.notesapp.data.local.DatabaseDriverFactory
import com.example.notesapp.data.local.NoteDatabase
import com.example.notesapp.data.repository.NoteRepository
import com.example.notesapp.data.repository.NoteRepositoryImpl
import com.example.notesapp.data.validation.NoteValidator
import com.example.notesapp.ui.detail.NoteDetailViewModel
import com.example.notesapp.ui.notes.NotesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Modul Koin untuk layer data.
 *
 * Mendefinisikan cara membuat:
 * - NoteDatabase: singleton, dibuat sekali dan digunakan ulang
 * - NoteRepository: singleton, menggunakan interface agar mudah di-mock
 * - NoteValidator: factory, dibuat baru setiap kali dibutuhkan
 */
val dataModule = module {

    // Buat NoteDatabase sebagai singleton menggunakan DatabaseDriverFactory
    // get() akan mencari DatabaseDriverFactory dari Koin container
    single {
        NoteDatabase(get<DatabaseDriverFactory>().createDriver())
    }

    // Bind implementasi ke interface NoteRepository (Dependency Inversion)
    // UI dan test hanya bergantung pada interface, bukan implementasi konkret
    single<NoteRepository> {
        NoteRepositoryImpl(get())
    }

    // NoteValidator dibuat sebagai factory (instance baru setiap kali dibutuhkan)
    factory {
        NoteValidator()
    }
}

/**
 * Modul Koin untuk layer ViewModel.
 *
 * Menggunakan viewModel DSL agar Koin mengelola lifecycle ViewModel
 * sesuai dengan Android ViewModel lifecycle.
 */
val viewModelModule = module {

    // ViewModel untuk layar daftar catatan
    viewModel {
        NotesViewModel(
            repository = get(),
            validator  = get()
        )
    }

    // ViewModel untuk layar tambah/edit catatan
    viewModel {
        NoteDetailViewModel(
            repository = get(),
            validator  = get()
        )
    }
}

/** Kumpulan semua modul yang akan didaftarkan ke Koin */
val allModules = listOf(dataModule, viewModelModule)
