package edu.sungshin.ecopath

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class CreatePostActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var buttonPost: Button
    private lateinit var imageView: ImageView
    private lateinit var buttonSelectImage: Button
    private var selectedImageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance() // Firebase Storage 인스턴스 추가

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        buttonPost = findViewById(R.id.buttonPost)
        imageView = findViewById(R.id.imageView)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)

        // 이미지 선택 버튼 클릭 리스너
        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // 게시글 작성 버튼 클릭 리스너
        buttonPost.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val content = editTextContent.text.toString().trim()
            val username = auth.currentUser?.displayName ?: "익명 사용자"

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 이미지가 선택된 경우 업로드 후 게시글 저장
            selectedImageUri?.let { uri ->
                uploadImageToStorage(uri) { imageUrl ->
                    savePostToFirestore(title, content, username, imageUrl)
                }
            } ?: run {
                savePostToFirestore(title, content, username, null) // 이미지 없이 게시글 저장
            }
        }
    }

    // 갤러리에서 이미지 선택 후 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imageView.setImageURI(selectedImageUri) // 선택한 이미지 미리보기
        }
    }

    // Firebase Storage에 이미지 업로드
    private fun uploadImageToStorage(uri: Uri, onSuccess: (String) -> Unit) {
        val fileName = "images/${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child(fileName)
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
            }
    }

    // Firestore에 게시글 저장
    private fun savePostToFirestore(title: String, content: String, username: String, imageUrl: String?) {
        val post = hashMapOf(
            "title" to title,
            "content" to content,
            "username" to username,
            "imageUrl" to imageUrl, // 이미지 URL 추가
            "timestamp" to Timestamp.now()
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "게시물이 성공적으로 업로드되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 작성 완료 후 액티비티 종료
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "업로드에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

