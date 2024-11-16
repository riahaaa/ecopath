package edu.sungshin.ecopath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MyPostsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = "currentUsername" // 현재 로그인한 사용자의 ID나 username을 가져와야 합니다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts) // 새 레이아웃 파일 이름으로 변경

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchPosts() // 내가 쓴 게시글만 불러오기
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .whereEqualTo("author", currentUser) // 현재 사용자가 작성한 게시글만 가져옵니다.
            .get()
            .addOnSuccessListener { result ->
                val posts = mutableListOf<Post>()
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    posts.add(Post(title, content))
                }
                postAdapter = PostAdapter(posts)
                recyclerView.adapter = postAdapter
            }
            .addOnFailureListener { exception ->
                // 에러 처리
            }
    }
}
