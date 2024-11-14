package edu.sungshin.ecopath

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null
)
//댓글 데이터를 담는 파일