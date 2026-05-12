package com.example.notesapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform