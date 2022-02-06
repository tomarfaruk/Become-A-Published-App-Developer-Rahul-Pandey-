package com.example.myapplication.models

data class MemoryCard(
    val identifier: Int,
    val imageUri: String? = null,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false,
)