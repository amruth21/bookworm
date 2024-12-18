package com.example.bookworm

data class Book(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)