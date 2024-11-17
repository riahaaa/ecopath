package edu.sungshin.ecopath

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.GestureDetector
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
    val imageUrl: String? = null,
    val timestamp: Timestamp? = null,
    val likes: Int = 0,  // 공감 수
    val commentCount: Int = 0  // 댓글 수
)

class PostListActivity : AppCompatActivity() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference // Realtime Database 참조
    private lateinit var buttonCreatePost: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var imageButton: ImageButton
    private lateinit var titleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        // 뷰 초기화
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts)
        recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        buttonCreatePost = findViewById(R.id.buttonCreatePost)
        backButton = findViewById(R.id.backButton)
        imageButton = findViewById(R.id.imageButton)
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
                    postAdapter.notifyDataSetChanged() // 데이터 변경사항을 어댑터에 반영
                }
            }

        // 현재 로그인된 사용자의 uid 가져오기
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Realtime Database에서 사용자 ID 가져오기
            database.child("ecopath").child("UserAccount").child(uid).child("id")
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val username = dataSnapshot.getValue(String::class.java) ?: "알 수 없음2"
                    // Firestore에서 게시물 불러오기 (최신순 정렬)
                    loadPosts(username, postList)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 이름을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
        }

        // 게시물 작성 버튼 클릭 리스너 설정
        buttonCreatePost.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener {
            finish()  // 현재 Activity 종료, 이전 화면으로 돌아감
        }

        // 메뉴 버튼 클릭 리스너
        imageButton.setOnClickListener {
            // 메뉴 화면으로 이동
            val intent = Intent(this, MyContentActivity::class.java)
            startActivity(intent)
        }

        // 아이템 클릭 리스너 추가
        recyclerViewPosts.addOnItemTouchListener(
            RecyclerItemClickListener(this, recyclerViewPosts, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val post = postList[position]
                    val postId = post.postid  // post 객체에서 postId를 가져옴
                    val intent = Intent(this@PostListActivity, PostDetailActivity::class.java)
                    intent.putExtra("username", post.username) // 작성자 이름 (username)
                    intent.putExtra("title", post.title) // 제목
                    intent.putExtra("content", post.content) // 내용
                    intent.putExtra("imageUrl", post.imageUrl) // 이미지 URL (선택 사항)
                    intent.putExtra("postId", postId)  // postId를 전달
                    startActivity(intent)
                }
            })
        )
    }

    // Firestore에서 게시물 불러오기 함수
    private fun loadPosts(username: String, postList: MutableList<Post>) {
        // 먼저 Realtime Database에서 username을 가져오기
        database.child("ecopath").child("UserAccount").child("id")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                val userId = dataSnapshot.getValue(String::class.java) ?: "알 수 없음"  // userId 가져오기

                // Firestore에서 userId로 게시물 불러오기
                firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        postList.clear() // 중복 방지
                        val postsToAdd = mutableListOf<Post>() // 임시 리스트로 데이터를 저장

                        for (document in documents) {
                            val postid = document.id  // Firestore 문서의 ID를 postid로 사용
                            val title = document.getString("title") ?: ""
                            val content = document.getString("content") ?: ""
                            val imageUrl = document.getString("imageUrl")
                            val timestamp = document.getTimestamp("timestamp")
                            val likes = document.getLong("likes")?.toInt() ?: 0 // likes 필드 추가
                            val commentCount = document.getLong("commentCount")?.toInt() ?: 0 // commentCount 필드 추가
                            val username = document.getString("username") ?: "익명 사용자" // username 필드 가져오기

                            // Firestore에서 데이터를 가져오면서 Post 객체 생성
                            val post = Post(postid, username, title, content, imageUrl, timestamp, likes, commentCount)

                            // 각 게시물에 작성자 이름을 추가
                            database.child("ecopath").child("UserAccount").child("id")

                                .get()
                                .addOnSuccessListener { usernameSnapshot ->
                                    val postUsername = usernameSnapshot.getValue(String::class.java) ?: "알 수 없음4"
                                    post.username = postUsername  // 작성자 이름을 추가

                                    postsToAdd.add(post) // 임시 리스트에 추가

                                    // 모든 데이터를 처리한 후 RecyclerView 갱신
                                    if (postsToAdd.size == documents.size()) {
                                        postList.clear() // 기존 리스트 초기화
                                        postList.addAll(postsToAdd) // 새 데이터로 갱신
                                        postAdapter.notifyDataSetChanged() // RecyclerView 갱신
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "작성자 이름을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "사용자 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }




    override fun onResume() {
        super.onResume()
        // 화면으로 돌아올 때마다 게시물 목록을 새로 불러오기
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.child("ecopath").child("UserAccount").child(uid).child("id")
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val username = dataSnapshot.getValue(String::class.java) ?: "알 수 없음"
                    loadPosts(username, postAdapter.getPostList()) // 최신 게시물 목록으로 갱신
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 이름을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

// RecyclerItemClickListener 추가
open class RecyclerItemClickListener(
    context: Context,
    recyclerView: RecyclerView,
    private val listener: OnItemClickListener
) : RecyclerView.OnItemTouchListener {

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val childView = rv.findChildViewUnder(e.x, e.y)
        if (childView != null && gestureDetector.onTouchEvent(e)) {
            listener.onItemClick(childView, rv.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
