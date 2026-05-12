package com.example.notesapp.data.model

import platform.Foundation.NSDate

/**
 * Implementasi actual untuk iOS/Native.
 * Menggunakan NSDate.timeIntervalSince1970 dari Foundation framework iOS.
 */
internal actual fun currentTimeMs(): Long {
    // NSDate.timeIntervalSince1970 mengembalikan detik (Double) sejak epoch
    // Dikalikan 1000 untuk mengubah ke milidetik
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
