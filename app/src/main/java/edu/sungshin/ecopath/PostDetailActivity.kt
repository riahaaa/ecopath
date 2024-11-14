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
    private lateinit var textViewLikes: TextView // 공감 수 TextView
    private lateinit var buttonLike: Button // 공감 버튼
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
        textViewLikes = findViewById(R.id.textViewLikes)
        buttonLike = findViewById(R.id.buttonLike)

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
        //공감수 댓글수 가져오기
        loadPostData(postId)

        // 댓글 불러오기
        loadComments(postId)
        //공감버튼 클릭 리스너
        buttonLike.setOnClickListener{
            increaseLikeCount(postId)
        }

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
    // 공감 수 증가
    private fun increaseLikeCount(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.getLong("likes") ?: 0
            transaction.update(postRef, "likes", currentLikes + 1)
        }.addOnSuccessListener {
            loadPostData(postId) // 공감 수를 다시 로드하여 갱신
        }.addOnFailureListener { e ->
            Toast.makeText(this, "공감 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 댓글 개수 업데이트
    private fun updateCommentCount(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentCommentCount = snapshot.getLong("commentCount") ?: 0
            transaction.update(postRef, "commentCount", currentCommentCount + 1)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "댓글 수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 게시글 데이터(공감 수와 댓글 수) 불러오기
    private fun loadPostData(postId: String) {
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likes = document.getLong("likes") ?: 0
                    val commentCount = document.getLong("commentCount") ?: 0
                    textViewLikes.text = "공감 $likes"
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
