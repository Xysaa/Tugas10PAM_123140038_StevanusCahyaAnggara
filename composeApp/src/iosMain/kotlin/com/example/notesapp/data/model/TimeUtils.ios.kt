package com.example.notesapp.data.model

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * Implementasi actual untuk iOS/Native.
 * Menggunakan NSDate.timeIntervalSince1970 dari Foundation framework iOS.
 * Properti ini mengembalikan detik (Double) sejak Unix epoch (1 Jan 1970).
 */
internal actual fun currentTimeMs(): Long {
    // timeIntervalSince1970 mengembalikan Double dalam satuan detik
    // Kalikan 1000 untuk konversi ke milidetik
    return (NSDate().timeIntervalSince1970 * 1000.0).toLong()
}
