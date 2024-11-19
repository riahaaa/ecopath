package edu.sungshin.ecopath

import com.google.firebase.Timestamp

data class Comment(
    val username: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null
)
//댓글 데이터를 담는 파일