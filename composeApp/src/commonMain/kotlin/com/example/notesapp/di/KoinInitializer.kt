package com.example.notesapp.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Menginisialisasi Koin dengan semua modul yang dibutuhkan.
 *
 * Fungsi ini dipanggil sekali saat aplikasi pertama kali dibuka,
 * biasanya dari MainActivity.onCreate() pada Android.
 *
 * @param appDeclaration Konfigurasi tambahan (opsional), digunakan untuk
 *                       menyuntikkan platform-specific dependencies seperti
 *                       Android Context pada DatabaseDriverFactory
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return startKoin {
        // Jalankan konfigurasi platform-specific terlebih dahulu
        appDeclaration()
        // Daftarkan semua modul aplikasi
        modules(allModules)
    }
}
