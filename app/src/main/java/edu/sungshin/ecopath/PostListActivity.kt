package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
    val postid: String = "", // 게시글 고유 ID
    var username: String = "", // 게시글 작성자 ID를 저장할 필드
    val title: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null,
    val likes: Int = 0,  // 공감 수
    val commentCount: Int = 0,  // 댓글 수
    val id: String?="" //작성자 UID (이메일)
)

class PostListActivity : AppCompatActivity() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var buttonCreatePost: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var titleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        // 뷰 초기화
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts)
        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        buttonCreatePost = findViewById(R.id.buttonCreatePost)
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)

        // 게시물 리스트 초기화
        val postList = mutableListOf<Post>()
        postAdapter = PostAdapter(postList)
        recyclerViewPosts.adapter = postAdapter

        // 실시간 리스너로 데이터 변화 감지
        firestore.collection("posts")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postList.clear()
                    for (document in snapshots.documents) {
                        val post = document.toObject(Post::class.java)
                        if (post != null) {
                            postList.add(post)
                        }
                    }
                    postAdapter.notifyDataSetChanged()
                }
            }

        // 게시물 작성 버튼 클릭 리스너 설정
        buttonCreatePost.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener {
            finish()
        }


    }

    // Firestore에서 게시물 불러오기 함수
    private fun loadPosts(username: String, postList: MutableList<Post>) {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                val postsToAdd = mutableListOf<Post>()

                for (document in documents) {
                    val postid = document.id
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val timestamp = document.getTimestamp("timestamp")
                    val likes = document.getLong("likes")?.toInt() ?: 0
                    val commentCount = document.getLong("commentCount")?.toInt() ?: 0
                    val username = document.getString("id") ?: "익명 사용자"
                    val id = document.getString("id") ?: ""

                    val post = Post(postid, username, title, content, timestamp, likes, commentCount)
                    postsToAdd.add(post)
                }

                postList.addAll(postsToAdd)
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onResume() {
        super.onResume()
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.child("ecopath").child("UserAccount").child(uid).child("id")
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val username = dataSnapshot.getValue(String::class.java) ?: "알 수 없음"
                    loadPosts(username, postAdapter.getPostList())
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 이름을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}


