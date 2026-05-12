package com.example.notesapp.data.model

/**
 * Implementasi actual untuk Android/JVM.
 * Menggunakan System.currentTimeMillis() yang tersedia di JVM.
 */
internal actual fun currentTimeMs(): Long = System.currentTimeMillis()
