package edu.sungshin.ecopath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MyCommentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = "currentUsername" // 현재 로그인한 사용자의 ID나 username을 가져와야 합니다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_comments) // 새 레이아웃 파일 이름으로 변경

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchComments() // 내가 쓴 댓글만 불러오기
    }

    private fun fetchComments() {
        firestore.collection("comments")
            .whereEqualTo("username", currentUser) // 현재 사용자가 작성한 댓글만 가져옵니다.
            .get()
            .addOnSuccessListener { result ->
                val comments = mutableListOf<Comment>()
                for (document in result) {
                    val content = document.getString("content") ?: ""
                    comments.add(Comment(content, currentUser))
                }
                commentAdapter = CommentAdapter(comments)
                recyclerView.adapter = commentAdapter
            }
            .addOnFailureListener { exception ->
                // 에러 처리
            }
    }
}
