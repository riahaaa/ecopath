package edu.sungshin.ecopath

import com.google.firebase.Timestamp

data class Comment(
    val username: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null
)
