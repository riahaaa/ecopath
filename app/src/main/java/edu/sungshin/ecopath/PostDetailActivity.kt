package edu.sungshin.ecopath

import android.os.Bundle
import android.util.Log
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
    private lateinit var backButton: ImageButton
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
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener { onBackPressed() }

        val postId = intent.getStringExtra("postId") ?: ""
        if (postId.isEmpty()) {
            Toast.makeText(this, "잘못된 게시글 ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Firestore에서 해당 게시글의 데이터 불러오기
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("id") ?: "알 수 없음"
                    val title = document.getString("title") ?: "제목 없음"
                    val content = document.getString("content") ?: "내용 없음"
                    val imageUrl = document.getString("imageUrl")

                    textViewUsername.text = username
                    textViewTitle.text = title
                    textViewContent.text = content

                    imageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .into(imageViewPost)
                    } ?: imageViewPost.setImageResource(R.drawable.placeholder)

                    loadPostData(postId)
                    loadComments(postId)

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

        buttonLike.setOnClickListener { increaseLikeCount(postId) }

        buttonSubmitComment.setOnClickListener {
            val commentContent = editTextComment.text.toString().trim()
            val currentUser = auth.currentUser  // 현재 로그인된 사용자 정보 가져오기

            if (currentUser != null && commentContent.isNotEmpty()) {
                val userId = currentUser.uid  // 로그인된 사용자의 UID
                Log.d("DEBUG", "User ID: $userId")  // userId가 null인지 아닌지 확인

                // Realtime Database에서 해당 UID를 사용해 사용자 id 가져오기
                val databaseRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(userId)
                databaseRef.get().addOnSuccessListener { snapshot ->
                    val userName = snapshot.child("id").value.toString()  // Realtime Database에서 "id" 필드 가져오기

                    val newComment = hashMapOf(
                        "username" to userName,  // 조회된 사용자 이름을 댓글 작성자의 username으로 설정
                        "content" to commentContent,
                        "timestamp" to Timestamp.now()
                    )

                    // Firestore에 댓글 저장
                    firestore.collection("posts").document(postId).collection("comments")
                        .add(newComment)
                        .addOnSuccessListener {
                            Toast.makeText(this, "댓글이 작성되었습니다.", Toast.LENGTH_SHORT).show()
                            editTextComment.text.clear()

                            // 새 댓글 리스트에 추가
                            val comment = Comment(userName, commentContent, Timestamp.now())  // 사용자 이름 사용
                            commentList.add(comment)
                            commentAdapter.notifyItemInserted(commentList.size - 1)

                            // 댓글 수 증가
                            firestore.collection("posts").document(postId)
                                .update("commentCount", FieldValue.increment(1))
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "댓글 수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "댓글 작성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "사용자 정보를 가져오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (currentUser == null) {
                    Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }






    }

    private fun increaseLikeCount(postId: String) {
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.getLong("likes") ?: 0
            transaction.update(postRef, "likes", currentLikes + 1)
        }.addOnSuccessListener { loadPostData(postId) }
            .addOnFailureListener { e -> Toast.makeText(this, "공감 실패: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    private fun loadPostData(postId: String) {
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likes = document.getLong("likes") ?: 0
                    textViewLikes.text = "공감 $likes"
                }
            }
    }

    private fun loadComments(postId: String) {
        firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                commentList.clear()
                for (document in documents) {
                    val username = document.getString("username") ?: "익명 사용자"
                    val content = document.getString("content") ?: "내용 없음"
                    val timestamp = document.getTimestamp("timestamp")

                    val comment = Comment(username, content, timestamp)
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글을 불러오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}