package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
data class Post(
    val id: String, // 게시글 ID 추가
    val username: String,
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    @ServerTimestamp val timestamp: Timestamp? =null
)

class PostListActivity : AppCompatActivity() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var buttonCreatePost: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts)
        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        buttonCreatePost = findViewById(R.id.buttonCreatePost)

        // 게시물 리스트 초기화
        val postList = mutableListOf<Post>()
        postAdapter = PostAdapter(postList)
        recyclerViewPosts.adapter = postAdapter

        // Firestore에서 게시물 불러오기(최신순 정렬)
        firestore.collection("posts")
            .orderBy("timestamp" )
            .get()
            .addOnSuccessListener { documents ->
                postList.clear() //중복방지
                for (document in documents) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val username = document.getString("username") ?: "알 수 없음"
                    val postId = document.id // Firestore에서 생성된 문서 ID를 가져옴

                    // Firestore에서 데이터를 가져오면서 Post 객체 생성 (id 포함)
                    postList.add(Post(postId, username, title, content))
                }

                // 데이터가 변경되었음을 알리고 RecyclerView 갱신
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // 오류 처리
                Toast.makeText(this, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        // 게시물 작성 버튼 클릭 리스너 설정
        buttonCreatePost.setOnClickListener {
            Toast.makeText(this, "게시물 작성화면으로 이동", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }
    }
}
