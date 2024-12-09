package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditPostActivity : AppCompatActivity() {

    private lateinit var postId: String
    private lateinit var postTitle: EditText
    private lateinit var postContent: EditText
    private lateinit var buttonSave: Button
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var backButton: ImageButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)


        postId = intent.getStringExtra("postId") ?: ""

        postTitle = findViewById(R.id.editTextTitle)
        postContent = findViewById(R.id.editTextContent)
        buttonSave = findViewById(R.id.buttonSave)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener { onBackPressed() }



        // 기존 게시글 로드
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    postTitle.setText(document.getString("title"))
                    postContent.setText(document.getString("content"))
                }
            }

        buttonSave.setOnClickListener {
            val newTitle = postTitle.text.toString().trim()
            val newContent = postContent.text.toString().trim()

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedPost = hashMapOf(
                "title" to newTitle,
                "content" to newContent,
                "timestamp" to Timestamp.now() // 수정 시간 업데이트
            )



            firestore.collection("posts").document(postId)
                .set(updatedPost, SetOptions.merge()) // 문서를 병합하면서 업데이트
                .addOnSuccessListener {
                    Toast.makeText(this, "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    finish() // 수정 후 뒤로가기
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "수정 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }



    }
}
