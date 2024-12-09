package edu.sungshin.ecopath

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreatePostActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var buttonPost: Button
    private lateinit var progressBar: ProgressBar
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        // UI 요소 초기화
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        buttonPost = findViewById(R.id.buttonPost)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        // 게시글 작성 버튼 클릭 리스너
        buttonPost.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val content = editTextContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // 프로그레스바 표시
                    runOnUiThread { progressBar.visibility = View.VISIBLE }

                    savePostToFirestore(title, content)

                    runOnUiThread {
                        Toast.makeText(this@CreatePostActivity, "게시물이 성공적으로 업로드되었습니다.", Toast.LENGTH_SHORT).show()
                        finish() // 작성 완료 후 액티비티 종료
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@CreatePostActivity, "업로드에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    // 프로그레스바 숨기기
                    runOnUiThread { progressBar.visibility = View.GONE }
                }
            }
        }
    }

    private suspend fun savePostToFirestore(title: String, content: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            runOnUiThread {
                Toast.makeText(this, "사용자가 로그인되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("ecopath").child("UserAccount").child(uid)
        val snapshot = databaseRef.get().await()

        Log.d("CreatePostActivity", "Snapshot Data: ${snapshot.value}")
        val usernameFromDB = snapshot.child("id").value as? String ?: "익명 사용자"

        val postId = System.currentTimeMillis().toString()
        val post = hashMapOf(
            "postId" to postId,
            "title" to title,
            "content" to content,
            "id" to usernameFromDB,
            "postOwnerUid" to uid,
            "timestamp" to Timestamp.now(),
            "likes" to 0,
            "commentCount" to 0
        )

        firestore.collection("posts")
            .document(postId)
            .set(post)
            .await()
    }
}
