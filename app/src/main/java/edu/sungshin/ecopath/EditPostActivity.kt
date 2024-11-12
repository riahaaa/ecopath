package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditPostActivity : AppCompatActivity() {

    private lateinit var postId: String
    private lateinit var postTitle: EditText
    private lateinit var postContent: EditText
    private lateinit var buttonSave: Button
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        postId = intent.getStringExtra("postId") ?: ""

        postTitle = findViewById(R.id.editTextTitle)
        postContent = findViewById(R.id.editTextContent)
        buttonSave = findViewById(R.id.buttonSave)

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
            val newTitle = postTitle.text.toString()
            val newContent = postContent.text.toString()

            val updatedPost = hashMapOf<String, Any>(
                "title" to newTitle,
                "content" to newContent
            )


            firestore.collection("posts").document(postId)
                .update(updatedPost)
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
