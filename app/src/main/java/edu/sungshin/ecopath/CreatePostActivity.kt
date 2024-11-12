package edu.sungshin.ecopath


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CreatePostActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var buttonPost: Button
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        buttonPost = findViewById(R.id.buttonPost)

        buttonPost.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val content = editTextContent.text.toString().trim()
            val username = "사용자 이름" // 실제 로그인된 사용자 이름을 사용할 수 있습니다.

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore에 게시글 저장
            val post = hashMapOf(
                "title" to title,
                "content" to content,
                "username" to username
            )

            firestore.collection("posts")
                .add(post) // Firestore에 문서 추가
                .addOnSuccessListener { documentReference ->
                    // 문서 ID를 가져와서 postId로 설정
                    val postId = documentReference.id
                    val newPost = Post(postId, username, title, content) // postId 포함된 Post 객체 생성

                    Toast.makeText(this, "게시물이 성공적으로 업로드되었습니다.", Toast.LENGTH_SHORT).show()
                    finish() // 게시물 업로드 후 액티비티 종료
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "업로드에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
