package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class PostDetailActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewContent: TextView
    private lateinit var imageViewPost: ImageView // 이미지 뷰를 추가
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var editTextComment: EditText
    private lateinit var buttonSubmitComment: Button
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        textViewUsername = findViewById(R.id.textViewUsername)
        textViewTitle = findViewById(R.id.textViewTitle)
        textViewContent = findViewById(R.id.textViewContent)
        imageViewPost = findViewById(R.id.imageViewPost)
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        editTextComment = findViewById(R.id.editTextComment)
        buttonSubmitComment = findViewById(R.id.buttonSubmitComment)

        // Intent로 전달된 게시글 데이터 받기
        val postId = intent.getStringExtra("postId") ?: ""
        val username = intent.getStringExtra("username") ?: "알 수 없음"
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val content = intent.getStringExtra("content") ?: "내용 없음"
        val imageUrl = intent.getStringExtra("imageUrl") // 이미지 URL 받기

        // 텍스트뷰에 데이터 설정
        textViewUsername.text = username
        textViewTitle.text = title
        textViewContent.text = content

        // 이미지 URL이 있으면 이미지뷰에 로드
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(imageViewPost)
        } ?: imageViewPost.setImageResource(R.drawable.placeholder) // URL이 없으면 기본 이미지

        // RecyclerView 설정
        commentAdapter = CommentAdapter(commentList)
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.adapter = commentAdapter

        // 댓글 불러오기
        loadComments(postId)

        // 댓글 작성 버튼 클릭 리스너
        buttonSubmitComment.setOnClickListener {
            val commentContent = editTextComment.text.toString().trim()
            val authorId = auth.currentUser?.uid ?: "알 수 없음"

            Log.d("PostDetailActivity", "Auth UID: $authorId") // UID 로그 추가

            if (commentContent.isNotEmpty()) {
                val newComment = hashMapOf(
                    "authorId" to authorId,
                    "content" to commentContent,
                    "timestamp" to Timestamp.now()
                )
                firestore.collection("posts").document(postId).collection("comments")
                    .add(newComment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "댓글이 작성되었습니다.", Toast.LENGTH_SHORT).show()
                        editTextComment.text.clear()
                        loadComments(postId) // 새로 작성된 댓글을 표시하기 위해 다시 로드
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "댓글 작성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Firestore에서 댓글 불러오기
    private fun loadComments(postId: String) {
        firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                commentList.clear()
                for (document in documents) {
                    val comment = document.toObject(Comment::class.java)
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글을 불러오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
