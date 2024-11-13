package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Post(
    val id: String, // 게시글 ID
    val username: String, // 게시글 작성자 ID를 저장할 필드
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Timestamp? = null
)

class PostListActivity : AppCompatActivity() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference // Realtime Database 참조
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

        // 현재 로그인된 사용자의 uid 가져오기
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Realtime Database에서 사용자 ID 가져오기
            database.child("ecopath").child("UserAccount").child(uid).child("id")
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val userId = dataSnapshot.getValue(String::class.java) ?: "알 수 없음"

                    // Firestore에서 게시물 불러오기 (최신순 정렬)
                    loadPosts(userId, postList)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 ID를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
        }

        // 게시물 작성 버튼 클릭 리스너 설정
        buttonCreatePost.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }
    }

    // Firestore에서 게시물 불러오기 함수
    private fun loadPosts(userId: String, postList: MutableList<Post>) {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear() // 중복 방지
                for (document in documents) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val postId = document.id
                    val imageUrl = document.getString("imageUrl")
                    val timestamp = document.getTimestamp("timestamp")

                    // Firestore에서 데이터를 가져오면서 Post 객체 생성 (작성자 ID로 userId 사용)
                    postList.add(Post(postId, userId, title, content, imageUrl, timestamp))
                }

                // 데이터가 변경되었음을 알리고 RecyclerView 갱신
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}
