package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Post(
    var postid: String = "", // 게시글 고유 ID
    var username: String = "", // 게시글 작성자 ID를 저장할 필드
    val title: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null,
    val likes: Int = 0,  // 공감 수
    val commentCount: Int = 0,  // 댓글 수
    val id: String?=""
)

class PostListActivity : AppCompatActivity() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var lastVisible: DocumentSnapshot? = null
    private var isLoading = false
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
        postAdapter = PostAdapter(postList)
        recyclerViewPosts.adapter = postAdapter

        // 게시물 로드
        loadPosts()

        // 스크롤 리스너 추가 (무한 스크롤)
        recyclerViewPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && !isLoading) { // 끝에 도달하고 로딩 중이 아니면
                    loadMorePosts()
                }
            }
        })

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

    // Firestore에서 첫 게시물 불러오기 함수
    private fun loadPosts() {
        isLoading = true
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear() // 기존 데이터 초기화
                for (document in documents) {
                    Log.d("LoadPosts", "Document ID: ${document.id}, Data: ${document.data}")
                    val post = document.toObject(Post::class.java)
                    post.postid = document.id // Firestore 문서 ID를 postid로 설정
                    postList.add(post)
                }
                lastVisible = documents.documents.lastOrNull() // 마지막 문서 저장
                postAdapter.notifyDataSetChanged()
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    // Firestore에서 추가 게시물 불러오기 함수 (무한 스크롤)
    private fun loadMorePosts() {
        lastVisible?.let { lastDoc ->
            isLoading = true
            db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastDoc) // 마지막 문서부터 시작
                .limit(20)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d("LoadMorePosts", "Document ID: ${document.id}, Data: ${document.data}")
                        val post = document.toObject(Post::class.java)
                        post.postid = document.id // Firestore 문서 ID를 postid로 설정
                        postList.add(post)
                    }
                    lastVisible = documents.documents.lastOrNull() // 마지막 문서 저장
                    postAdapter.notifyDataSetChanged()
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(this, "추가 게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }


    override fun onResume() {
        super.onResume()
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("ecopath").child("UserAccount").child(uid).child("id")
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val username = dataSnapshot.getValue(String::class.java) ?: "알 수 없음"
                    loadPosts() // 사용자 이름을 받는 곳이 없으므로 바로 게시물 로딩
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 이름을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
