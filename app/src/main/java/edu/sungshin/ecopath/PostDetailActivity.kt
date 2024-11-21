package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class PostDetailActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewContent: TextView
    private lateinit var imageViewPost: ImageView
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var textViewLikes: TextView
    private lateinit var buttonLike: Button
    private lateinit var editTextComment: EditText
    private lateinit var buttonSubmitComment: Button
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var backButton: ImageButton  // 추가된 부분
    private val realtimeDatabase = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // UI 요소들 초기화
        textViewUsername = findViewById(R.id.textViewUsername)
        textViewTitle = findViewById(R.id.textViewTitle)
        textViewContent = findViewById(R.id.textViewContent)
        imageViewPost = findViewById(R.id.imageViewPost)
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        editTextComment = findViewById(R.id.editTextComment)
        buttonSubmitComment = findViewById(R.id.buttonSubmitComment)
        textViewLikes = findViewById(R.id.textViewLikes)
        buttonLike = findViewById(R.id.buttonLike)
        backButton = findViewById(R.id.backButton)  // 백 버튼 초기화
        // 백 버튼 클릭 리스너 추가
        backButton.setOnClickListener {
            onBackPressed()  // 뒤로 가기 동작
        }
        // Intent로 전달된 게시글 데이터 받기
        val postId = intent.getStringExtra("postId") ?: ""  // postId가 없을 경우 빈 문자열로 처리
        if (postId.isEmpty()) {
            Toast.makeText(this, "잘못된 게시글 ID", Toast.LENGTH_SHORT).show()
            finish()  // postId가 없으면 액티비티 종료
            return
        }

        // Firestore에서 해당 게시글의 데이터 불러오기
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Firestore에서 받아온 데이터 설정
                    val username = document.getString("id") ?: "알 수 없음" // 'id' 필드를 'username'으로 사용
                    val title = document.getString("title") ?: "제목 없음"
                    val content = document.getString("content") ?: "내용 없음"
                    val imageUrl = document.getString("imageUrl")

                    // 텍스트뷰에 데이터 설정
                    textViewUsername.text = username
                    textViewTitle.text = title
                    textViewContent.text = content
                    // 이미지 URL이 있으면 이미지뷰에 로드
                    imageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(imageViewPost)
                    } ?: imageViewPost.setImageResource(R.drawable.placeholder)
                    // 공감 수 및 댓글 수 가져오기
                    loadPostData(postId)
                    // 댓글 불러오기
                    loadComments(postId)
                    // RecyclerView 설정
                    commentAdapter = CommentAdapter(commentList)
                    recyclerViewComments.layoutManager = LinearLayoutManager(this)
                    recyclerViewComments.adapter = commentAdapter
                } else {
                    Toast.makeText(this, "게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "게시글을 불러오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
                    // 공감 버튼 클릭 리스너
                    buttonLike.setOnClickListener {
                        increaseLikeCount(postId)
                    }


                    // 댓글 작성 버튼 클릭 리스너
        buttonSubmitComment.setOnClickListener {
            val commentContent = editTextComment.text.toString().trim()
            val uid = auth.currentUser?.uid  // 현재 로그인한 사용자 ID

            if (uid != null && commentContent.isNotEmpty()) {
                val newComment = hashMapOf(
                    "id" to uid,  // Firestore에서 가져온 사용자의 uid를 사용
                    "content" to commentContent,
                    "timestamp" to Timestamp.now()
                )

                // 댓글 추가 후 새로 작성된 댓글만 리스트에 추가
                firestore.collection("posts").document(postId).collection("comments")
                    .add(newComment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "댓글이 작성되었습니다.", Toast.LENGTH_SHORT).show()
                        editTextComment.text.clear()

                        // 새로 작성된 댓글만 리스트에 추가
                        val comment = Comment(uid, commentContent, Timestamp.now())
                        commentList.add(comment)
                        commentAdapter.notifyItemInserted(commentList.size - 1)  // 새로 추가된 댓글만 표시

                        // 댓글 수 증가
                        firestore.collection("posts").document(postId)
                            .update("commentCount", FieldValue.increment(1))
                            .addOnSuccessListener {
                                Toast.makeText(this, "댓글 수가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "댓글 수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
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
    // 백 버튼 클릭 시 뒤로 가기 동작
    override fun onBackPressed() {
        super.onBackPressed()  // 기본 뒤로 가기 동작
    }
}
